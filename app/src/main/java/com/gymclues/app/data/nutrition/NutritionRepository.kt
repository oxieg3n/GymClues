package com.gymclues.app.data.nutrition

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.gymclues.app.data.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId

class NutritionRepository(private val db: GymDb, private val session: UserSession, context: Context,
  private val programs: List<WorkoutProgram>, providers: List<FoodProvider> = listOf(OpenFoodFactsProvider())) {
  var providers = providers; private set
  fun configureUsda(url: String) { providers = providers.filterNot { it.source == "usda" } + if (url.isBlank()) emptyList() else listOf(UsdaFoodProvider(url)) }
  private val ctx = context.applicationContext
  val dao = db.nutrition()
  val owner get() = session.userId
  private val offCache = lazy { Room.databaseBuilder(ctx, FoodCacheDb::class.java, "nutrition-openfoodfacts.db").build() }
  private val usdaCache = lazy { Room.databaseBuilder(ctx, FoodCacheDb::class.java, "nutrition-usda.db").build() }
  private fun cache(source: String) = when (source) { "off" -> offCache.value.foods(); "usda" -> usdaCache.value.foods(); else -> null }
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  fun foods(query: String = ""): Flow<List<FoodEntity>> {
    val word = query.trim().split(Regex("\\s+")).firstOrNull().orEmpty().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    val pattern = "%$word%"
    val user = owner
    return dao.usage(user).flatMapLatest { usage ->
      val used = (usage.take(100) + usage.sortedByDescending { it.lastUsed }.take(100)).map { it.foodId }.distinct()
      combine(dao.foods(user), offCache.value.foods().foods(user, pattern, used), usdaCache.value.foods().foods(user, pattern, used)) { a, b, c -> a + b + c }
    }
  }
  fun diary(start: LocalDate, end: LocalDate) = dao.diary(owner, start.toString(), end.toString())
  fun recent() = dao.recent(owner)
  fun usage() = dao.usage(owner)
  fun bundles() = dao.bundles(owner)
  fun goals() = dao.goals(owner)
  fun weights() = dao.weights(owner)
  fun water(start: LocalDate, end: LocalDate) = dao.water(owner, start.toString(), end.toString())
  fun days(start: LocalDate, end: LocalDate): Flow<List<NutritionDay>> {
    val schedule = combine(db.dao().assignments(owner), db.dao().restDays(owner), db.dao().sessions(owner)) { a, r, s -> Triple(a, r, s) }
    return combine(diary(start, end), goals(), water(start, end), weights(), schedule) { entries, goals, water, weights, planning ->
      val completed = planning.third.map { java.time.Instant.ofEpochMilli(it.completedAt).atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
      generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.map { date ->
        val assigned = scheduledDay(programs, planning.first, date, RestRules(planning.second))
        val training = date in completed || (assigned != null && assigned.day.training && !assigned.userRest)
        NutritionDay(date.toString(), entries.filter { it.date == date.toString() },
          goals.filter { it.effectiveDate <= date.toString() }.maxByOrNull { it.effectiveDate }, training,
          water.filter { it.date == date.toString() }, weights.filter { it.date == date.toString() })
      }.toList()
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
  }

  suspend fun food(id: String): FoodEntity? = dao.food(owner, id) ?: offCache.value.foods().food(owner, id) ?: usdaCache.value.foods().food(owner, id)
  suspend fun refreshFood(id: String): FoodEntity {
    val existing = food(id) ?: error("Food not found")
    val provider = providers.firstOrNull { it.source == existing.source } ?: error("This provider is not configured")
    return provider.getFoodDetails(existing.sourceId)?.let { cacheFood(it) } ?: error("The provider no longer has nutrition details. Your cached record is unchanged.")
  }
  suspend fun saveFood(food: FoodEntity): FoodEntity {
    require(food.ownerId == owner && food.source == "user") { "Only your custom foods can be edited" }
    dao.foodById(food.id)?.let { require(it.ownerId == owner) { "This food belongs to another user" } }
    validateFood(food)
    val saved = food.copy(name = food.name.trim(), brand = food.brand.trim(), updatedAt = now())
    dao.putFood(saved); return saved
  }
  suspend fun cacheFood(food: FoodEntity): FoodEntity {
    require(food.source in listOf("off", "usda"))
    validateFood(food)
    val id = stableNutritionId("$owner|${food.source}|${food.sourceId}")
    val previous = cache(food.source)!!.food(owner, id)
    val saved = food.copy(id = id, ownerId = owner, favorite = previous?.favorite ?: false)
    cache(food.source)!!.put(saved); return saved
  }
  suspend fun favorite(id: String) { food(id)?.let { f ->
    val updated = f.copy(favorite = !f.favorite, updatedAt = now())
    if (f.source == "user") dao.putFood(updated) else cache(f.source)!!.put(updated)
  } }
  suspend fun deleteFood(id: String) { dao.food(owner, id)?.let { dao.putFood(it.copy(deleted = true, updatedAt = now())) } }

  suspend fun search(query: String): SearchOutcome {
    require(query.trim().length in 2..120) { "Enter at least two characters" }
    val found = mutableListOf<FoodEntity>(); val messages = mutableListOf<String>()
    for (provider in providers) {
      try { provider.searchFoods(query.trim()).forEach { found += cacheFood(it) } }
      catch (e: kotlinx.coroutines.CancellationException) { throw e }
      catch (e: Exception) { messages += "${provider.name}: ${providerMessage(e)}" }
    }
    return SearchOutcome(found.distinctBy { it.id }, messages.joinToString("\n"))
  }
  suspend fun barcode(code: String): FoodEntity? {
    require(code.matches(Regex("(?:[0-9]{8}|[0-9]{12,14})"))) { "Enter an 8, 12, 13 or 14 digit food barcode" }
    (dao.barcode(owner, code) ?: offCache.value.foods().barcode(owner, code) ?: usdaCache.value.foods().barcode(owner, code))?.let { return it }
    var error: Exception? = null
    for (provider in providers) try { provider.lookupBarcode(code)?.let { return cacheFood(it) } }
      catch (e: kotlinx.coroutines.CancellationException) { throw e } catch (e: Exception) { error = e }
    error?.let { throw it }; return null
  }

  suspend fun log(food: FoodEntity, date: String, meal: String, quantity: Double, portion: FoodPortion, actionId: String): String {
    require(food.ownerId == owner && !food.deleted)
    require(portions(food).any { it == portion }) { "Choose a supported serving" }
    val entry = DiaryEntry(actionId, owner, date, meal, food.id, food.snapshot(), quantity, portion.label, portion.amount)
    validateEntry(entry); dao.insertEntry(entry); return entry.id
  }
  suspend fun editEntry(id: String, quantity: Double, meal: String) {
    val entry = dao.entry(owner, id) ?: error("Food entry not found")
    val updated = entry.copy(quantity = quantity, meal = meal, updatedAt = now())
    validateEntry(updated); dao.updateEntry(updated)
  }
  suspend fun deleteEntry(id: String) { dao.entry(owner, id)?.let { dao.updateEntry(it.copy(deleted = true, updatedAt = now())) } }
  suspend fun repeat(id: String, date: String, meal: String, actionId: String) {
    val entry = dao.entry(owner, id) ?: error("Food entry not found")
    val copy = entry.copy(id = actionId, date = date, meal = meal, createdAt = now(), updatedAt = now())
    validateEntry(copy); dao.insertEntry(copy)
  }
  suspend fun copyDay(from: String, to: String, meal: String?, actionId: String): Int = db.withTransaction {
    validDate(from); validDate(to); require(meal == null || meal in MEALS)
    val entries = dao.dayOnce(owner, from).filter { meal == null || it.meal == meal }
    entries.forEach { entry ->
      dao.insertEntry(entry.copy(id = stableNutritionId("$actionId|${entry.id}"), date = to, createdAt = now(), updatedAt = now()))
    }; entries.size
  }
  suspend fun saveBundle(bundle: FoodBundle, items: List<BundleItem>) = db.withTransaction {
    require(bundle.ownerId == owner && bundle.name.isNotBlank() && bundle.name.length <= 200)
    require(bundle.kind in listOf("meal", "recipe")); validQuantity(bundle.servings)
    require(items.isNotEmpty() && items.size <= 200) { "Add at least one ingredient" }
    items.forEach { validateSnapshot(it.snapshot); validQuantity(it.quantity); validQuantity(it.canonicalPerUnit) }
    val previous = dao.bundleById(bundle.id)
    previous?.let { require(it.ownerId == owner) { "This saved meal belongs to another user" } }
    dao.putBundle(bundle.copy(createdAt = previous?.createdAt ?: bundle.createdAt, updatedAt = now())); dao.clearBundle(bundle.id)
    dao.putItems(items.map { it.copy(bundleId = bundle.id) })
  }
  suspend fun bundle(id: String) = dao.bundle(owner, id)?.let { it to dao.bundleItems(id) }
  suspend fun deleteBundle(id: String) { dao.bundle(owner, id)?.let { dao.putBundle(it.copy(deleted = true, updatedAt = now())) } }
  suspend fun logBundle(id: String, date: String, meal: String, servings: Double, actionId: String) = db.withTransaction {
    validDate(date); require(meal in MEALS); validQuantity(servings)
    val (bundle, items) = bundle(id) ?: error("Saved meal or recipe not found")
    require(items.isNotEmpty())
    if (bundle.kind == "recipe") {
      val base = nutritionTotal(items.map { it.total }).scale(1 / bundle.servings)
      val snapshot = FoodSnapshot(id, bundle.name, "", "recipe", "", "Private recipe; ingredient provenance retained", 1.0, "serving", base,
        items.map { RecipeComponent(it.snapshot, it.quantity, it.unit, it.canonicalPerUnit) })
      val entry = DiaryEntry(actionId, owner, date, meal, id, snapshot, servings, "serving", 1.0)
      validateEntry(entry); dao.insertEntry(entry)
    } else items.forEach { item ->
      val entry = DiaryEntry(stableNutritionId("$actionId|${item.id}"), owner, date, meal, item.snapshot.foodId, item.snapshot,
        item.quantity * servings, item.unit, item.canonicalPerUnit)
      validateEntry(entry); dao.insertEntry(entry)
    }
  }
  suspend fun saveGoal(goal: NutritionGoal) {
    require(goal.ownerId == owner); validDate(goal.effectiveDate)
    require(goal.daily.valid() && goal.rest.valid())
    require(listOf(goal.daily, goal.rest).all { n -> listOf(n.kcal, n.protein, n.carbs, n.fat).all { it != null && it in 0.0..20_000.0 } })
    require(goal.waterGoalMl.isFinite() && goal.waterGoalMl in 0.0..20_000.0)
    require(goal.weightUnit in listOf("kg", "lb") && goal.foodUnit in listOf("g", "oz"))
    dao.putGoal(goal.copy(id = stableNutritionId("$owner|goal|${goal.effectiveDate}"), updatedAt = now()))
  }
  suspend fun saveWeight(date: String, value: Double, unit: String) {
    validDate(date); val kg = kilograms(value, unit); require(kg.isFinite() && kg in 1.0..700.0) { "Enter a valid body weight" }
    val id = stableNutritionId("$owner|weight|$date")
    dao.putWeight(WeightLog(id, owner, date, kg, createdAt = dao.weight(owner, id)?.createdAt ?: now()))
  }
  suspend fun deleteWeight(id: String) { dao.weight(owner, id)?.let { dao.putWeight(it.copy(deleted = true, updatedAt = now())) } }
  suspend fun saveWater(date: String, value: Double, unit: String, actionId: String) {
    validDate(date); val ml = waterMl(value, unit); require(ml.isFinite() && ml in .1..10_000.0) { "Enter a valid water quantity" }
    dao.insertWater(WaterLog(actionId, owner, date, ml))
  }
  suspend fun editWater(id: String, ml: Double) {
    require(ml.isFinite() && ml in .1..10_000.0)
    dao.waterEntry(owner, id)?.let { dao.updateWater(it.copy(ml = ml, updatedAt = now())) }
  }
  suspend fun deleteWater(id: String) { dao.waterEntry(owner, id)?.let { dao.updateWater(it.copy(deleted = true, updatedAt = now())) } }
  suspend fun deleteAll() {
    db.withTransaction { dao.clearDiary(owner); dao.clearWater(owner); dao.clearWeights(owner); dao.clearBundles(owner); dao.clearFoods(owner); dao.clearGoals(owner) }
    offCache.value.foods().clear(owner); usdaCache.value.foods().clear(owner)
  }
  fun close() { if (offCache.isInitialized()) offCache.value.close(); if (usdaCache.isInitialized()) usdaCache.value.close() }
}

data class SearchOutcome(val foods: List<FoodEntity>, val message: String)
fun validateFood(food: FoodEntity) {
  require(food.name.isNotBlank() && food.name.length <= 200 && food.brand.length <= 200) { "Enter a food name (up to 200 characters)" }
  validQuantity(food.basisQuantity); require(food.basisUnit in listOf("g", "ml", "serving", "piece", "package", "cup", "tablespoon"))
  require(food.nutrients.valid() && food.nutrients.values().any { it != null }) { "Enter valid nutrition facts; unknown values may stay blank" }
  require(food.barcode.isEmpty() || food.barcode.matches(Regex("[0-9]{8}|[0-9]{12,14}"))) { "Invalid food barcode" }
  food.portions.forEach { require(it.label.isNotBlank()); validQuantity(it.amount) }
}
fun validateSnapshot(snapshot: FoodSnapshot) { validQuantity(snapshot.basisQuantity); require(snapshot.nutrients.valid() && snapshot.name.isNotBlank()) }
fun validateEntry(entry: DiaryEntry) {
  validDate(entry.date); require(entry.meal in MEALS); validateSnapshot(entry.snapshot)
  require(entry.foodId == entry.snapshot.foodId)
  validQuantity(entry.quantity); validQuantity(entry.canonicalPerUnit); require(entry.total.valid()) { "Food quantity exceeds supported nutrition range" }
}
fun rankedFoods(foods: List<FoodEntity>, history: List<DiaryEntry>, query: String, mode: String = "All", usageRecords: List<FoodUsage>? = null): List<FoodEntity> {
  val usage = usageRecords?.associate { it.foodId to it.uses } ?: history.groupingBy { it.foodId }.eachCount()
  val recent = usageRecords?.sortedByDescending { it.lastUsed }?.map { it.foodId } ?: history.sortedByDescending { it.createdAt }.map { it.foodId }.distinct()
  val words = query.trim().lowercase(java.util.Locale.ROOT).split(Regex("\\s+")).filter(String::isNotBlank)
  return foods.filter { f -> !f.deleted && words.all { it in "${f.name} ${f.brand}".lowercase(java.util.Locale.ROOT) } &&
    when (mode) { "Favorites" -> f.favorite; "Recent", "Frequent" -> f.id in usage; "My foods" -> f.source == "user"; else -> true } }
    .distinctBy { "${it.source}|${it.sourceId}" }.sortedWith(compareBy<FoodEntity> {
      when { it.id in usage -> 0; it.source == "user" || it.favorite -> 1; else -> 2 }
    }.thenBy { if (mode == "Frequent") -(usage[it.id] ?: 0) else recent.indexOf(it.id).takeIf { n -> n >= 0 } ?: Int.MAX_VALUE }.thenBy { it.name }).take(50)
}
