package com.gymclues.app

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class NutritionDatabaseTest {
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private lateinit var db: GymDb
  private lateinit var repo: NutritionRepository
  private val date = LocalDate.parse("2026-03-08")
  @Before fun setup() {
    ctx.getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("userId", "owner-a").commit()
    ctx.deleteDatabase("nutrition-openfoodfacts.db"); ctx.deleteDatabase("nutrition-usda.db")
    db = Room.inMemoryDatabaseBuilder(ctx, GymDb::class.java).allowMainThreadQueries().build()
    repo = NutritionRepository(db, UserSession(ctx, db.dao()), ctx, emptyList(), emptyList())
  }
  @After fun cleanup() { repo.close(); db.close() }
  private suspend fun food() = repo.saveFood(FoodEntity(ownerId = repo.owner, name = "Chicken label", basisQuantity = 100.0, basisUnit = "g", nutrients = Nutrients(120.0, 25.0, 0.0, 1.0)))

  @Test fun diaryCrudSnapshotsOwnershipAndRetrySafety() = runBlocking {
    val food = food()
    val request = newId()
    repeat(2) { repo.log(food, date.toString(), "Lunch", 150.0, FoodPortion("g", 1.0), request) }
    assertEquals(1, repo.diary(date, date).first().size)
    assertEquals(180.0, repo.days(date, date).first().single().total.kcal!!, 0.0)
    repo.saveFood(food.copy(name = "Changed label", nutrients = Nutrients(900.0, 1.0, 0.0, 0.0)))
    repo.editEntry(request, 200.0, "Dinner")
    val entry = repo.diary(date, date).first().single()
    assertEquals("Chicken label", entry.snapshot.name); assertEquals(240.0, entry.total.kcal!!, 0.0)
    repo.deleteFood(food.id); assertNull(repo.food(food.id)); assertEquals(1, repo.diary(date, date).first().size)
    ctx.getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("userId", "owner-b").commit()
    assertTrue(repo.diary(date, date).first().isEmpty())
    try { repo.editEntry(request, 1.0, "Lunch"); fail() } catch (_: IllegalStateException) { }
    ctx.getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("userId", "owner-a").commit()
    repo.deleteEntry(request); assertTrue(repo.diary(date, date).first().isEmpty())
  }
  @Test fun copyMealAndDayAreTransactionalAndIdempotent() = runBlocking {
    val food = food()
    for (meal in listOf("Lunch", "Dinner")) repo.log(food, date.toString(), meal, 1.0, portions(food).first(), newId())
    val token = newId()
    repeat(2) { repo.copyDay(date.toString(), date.plusDays(1).toString(), "Lunch", token) }
    assertEquals(1, repo.diary(date.plusDays(1), date.plusDays(1)).first().size)
    repo.copyDay(date.toString(), date.plusDays(2).toString(), null, newId())
    assertEquals(2, repo.diary(date.plusDays(2), date.plusDays(2)).first().size)
  }
  @Test fun recipesAndSavedMealsKeepHistoricalSnapshots() = runBlocking {
    val f = food()
    val recipe = FoodBundle(ownerId = repo.owner, name = "Meal prep", kind = "recipe", servings = 4.0, instructions = "Cook")
    val item = BundleItem(bundleId = recipe.id, snapshot = f.snapshot(), quantity = 400.0, unit = "g", canonicalPerUnit = 1.0)
    repo.saveBundle(recipe, listOf(item))
    val token = newId(); repeat(2) { repo.logBundle(recipe.id, date.toString(), "Lunch", 1.5, token) }
    var entry = repo.diary(date, date).first().single()
    assertEquals(180.0, entry.total.kcal!!, 0.0); assertEquals(1, entry.snapshot.ingredients.size)
    repo.saveBundle(recipe.copy(servings = 1.0), listOf(item.copy(quantity = 800.0)))
    assertEquals(180.0, repo.diary(date, date).first().single().total.kcal!!, 0.0)
    val meal = recipe.copy(id = newId(), kind = "meal", name = "Usual breakfast", servings = 1.0)
    repo.saveBundle(meal, listOf(item.copy(id = newId(), bundleId = meal.id, quantity = 50.0)))
    repo.logBundle(meal.id, date.toString(), "Breakfast", 2.0, newId())
    assertEquals(300.0, repo.days(date, date).first().single().total.kcal!!, 0.0)
    repo.deleteBundle(recipe.id); assertNull(repo.bundle(recipe.id)); assertEquals(2, repo.diary(date, date).first().size)
  }
  @Test fun failedMultiItemMealRollsBackAllWrites() = runBlocking {
    val f = food(); val meal = FoodBundle(ownerId = repo.owner, name = "Invalid second item", kind = "meal")
    val item = BundleItem(bundleId = meal.id, snapshot = f.snapshot(), quantity = 1.0, unit = "serving", canonicalPerUnit = 100.0)
    repo.saveBundle(meal, listOf(item, item.copy(id = newId(), quantity = 100_000.0)))
    try { repo.logBundle(meal.id, date.toString(), "Lunch", 2.0, newId()); fail() } catch (_: IllegalArgumentException) { }
    assertTrue(repo.diary(date, date).first().isEmpty())
  }
  @Test fun goalsAreVersionedAndWorkoutDaysUseTheExistingCalendar() = runBlocking {
    val old = NutritionGoal(ownerId = repo.owner, effectiveDate = date.minusDays(5).toString(), daily = Nutrients(2100.0, 120.0, 200.0, 60.0),
      rest = Nutrients(1750.0, 100.0, 180.0, 50.0), separateRest = true)
    repo.saveGoal(old)
    assertEquals(1750.0, repo.days(date, date).first().single().target.kcal!!, 0.0)
    db.dao().upsertSession(CompletedSessionEntity("session", repo.owner, "Workout", completedAt = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()))
    assertEquals(2100.0, repo.days(date, date).first().single().target.kcal!!, 0.0)
    repo.saveGoal(old.copy(effectiveDate = date.plusDays(1).toString(), daily = Nutrients(2500.0, 130.0, 200.0, 60.0)))
    assertEquals(2100.0, repo.days(date, date).first().single().target.kcal!!, 0.0)
    assertEquals(2, repo.goals().first().size)
  }
  @Test fun assignedTrainingAndRepeatingRestDaysSelectTargetsAcrossDst() = runBlocking {
    val programs = Json.decodeFromString<ProgramLibrary>(ctx.assets.open("programs.json").bufferedReader().use { it.readText() }).programs
    repo.close(); repo = NutritionRepository(db, UserSession(ctx, db.dao()), ctx, programs, emptyList())
    val program = programs.first { it.days.first().training }
    db.dao().upsertAssignment(ProgramAssignmentEntity(programId = program.id, startDate = date.toString(), ownerId = repo.owner))
    repo.saveGoal(NutritionGoal(ownerId = repo.owner, effectiveDate = date.toString(), separateRest = true,
      daily = Nutrients(2200.0, 150.0, 200.0, 70.0), rest = Nutrients(1900.0, 140.0, 180.0, 60.0)))
    assertEquals(2200.0, repo.days(date, date).first().single().target.kcal!!, 0.0)
    db.dao().upsertRestDay(RestDayEntity(ownerId = repo.owner, weekday = date.dayOfWeek.value, startDate = date.toString()))
    assertEquals(1900.0, repo.days(date, date).first().single().target.kcal!!, 0.0)
    assertTrue(repo.days(date.plusDays(1), date.plusDays(1)).first().single().training)
    assertFalse(repo.days(date.plusDays(7), date.plusDays(7)).first().single().training)
  }
  @Test fun waterWeightDeleteAndOwnerIsolation() = runBlocking {
    val token = newId(); repeat(2) { repo.saveWater(date.toString(), 8.0, "fl oz", token) }
    var water = repo.water(date, date).first().single()
    assertEquals(236.5882365, water.ml, 1e-8)
    repo.editWater(water.id, 500.0); assertEquals(500.0, repo.water(date, date).first().single().ml, 0.0)
    repo.saveWeight(date.toString(), 100.0, "lb"); repo.saveWeight(date.toString(), 99.0, "lb")
    assertEquals(1, repo.weights().first().size)
    val weight = repo.weights().first().single(); repo.deleteWeight(weight.id); assertTrue(repo.weights().first().isEmpty())
    repo.deleteWater(water.id); assertTrue(repo.water(date, date).first().isEmpty())
  }
  @Test fun externalCachesAreSeparatePrivateAndSurviveRepositoryRecreation() = runBlocking {
    val cached = repo.cacheFood(FoodEntity(ownerId = "", name = "External", source = "off", sourceId = "12345678", nutrients = Nutrients(100.0)))
    assertNull(db.nutrition().food(repo.owner, cached.id))
    assertEquals("off", repo.food(cached.id)!!.source)
    assertTrue(db.dao().outbox().isEmpty())
    repo.close(); repo = NutritionRepository(db, UserSession(ctx, db.dao()), ctx, emptyList(), emptyList())
    assertNotNull(repo.food(cached.id))
    ctx.getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("userId", "owner-b").commit()
    assertNull(repo.food(cached.id))
  }
  @Test fun clearingNutritionPreservesWorkoutsAndOtherOwners() = runBlocking {
    db.dao().log(SetLog(date = now(), exerciseId = "lift", setNo = 1, reps = 5, weight = 75.0))
    food(); db.nutrition().putFood(FoodEntity(ownerId = "other", name = "Other user food", nutrients = Nutrients(50.0)))
    repo.deleteAll()
    assertTrue(repo.foods().first().isEmpty()); assertEquals(1, db.dao().history().first().size)
    assertEquals(1, db.nutrition().foods("other").first().size)
  }
  @Test fun cannotOverwriteAnotherOwnersCustomFoodOrSavedMeal() = runBlocking {
    val food = food()
    val meal = FoodBundle(ownerId = repo.owner, name = "Private meal", kind = "meal")
    val item = BundleItem(bundleId = meal.id, snapshot = food.snapshot(), quantity = 1.0, unit = "serving", canonicalPerUnit = 100.0)
    repo.saveBundle(meal, listOf(item))
    ctx.getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("userId", "owner-b").commit()
    try { repo.saveFood(food.copy(ownerId = repo.owner)); fail() } catch (_: IllegalArgumentException) { }
    try { repo.saveBundle(meal.copy(ownerId = repo.owner), listOf(item)); fail() } catch (_: IllegalArgumentException) { }
    assertEquals("owner-a", db.nutrition().foodById(food.id)!!.ownerId)
    assertEquals("owner-a", db.nutrition().bundleById(meal.id)!!.ownerId)
  }
  @Test fun oldCachedFoodsRemainSearchableAndBarcodeWorksOffline() = runBlocking {
    val old = repo.cacheFood(FoodEntity(ownerId = "", name = "Older oats", barcode = "12345678", source = "off", sourceId = "12345678", nutrients = Nutrients(100.0)))
    val cache = Room.databaseBuilder(ctx, FoodCacheDb::class.java, "nutrition-openfoodfacts.db").build()
    try {
      repeat(501) { n -> cache.foods().put(old.copy(id = newId(), name = "Newer product $n", sourceId = "new-$n", barcode = "", updatedAt = old.updatedAt + n + 1)) }
      assertEquals(500, repo.foods().first().size)
      assertEquals(old.id, repo.foods("Older").first().single().id)
      assertEquals(old.id, repo.barcode("12345678")!!.id)
    } finally { cache.close() }
  }
  @Test fun allNutritionRecordsSurviveDatabaseCloseAndReopen() = runBlocking {
    repo.close(); db.close(); ctx.deleteDatabase("nutrition-restart.db")
    db = Room.databaseBuilder(ctx, GymDb::class.java, "nutrition-restart.db").build()
    repo = NutritionRepository(db, UserSession(ctx, db.dao()), ctx, emptyList(), emptyList())
    val food = food()
    repo.log(food, date.toString(), "Lunch", 1.0, portions(food).first(), newId())
    repo.saveWater(date.toString(), 250.0, "ml", newId()); repo.saveWeight(date.toString(), 80.0, "kg")
    repo.saveGoal(NutritionGoal(ownerId = repo.owner, effectiveDate = date.toString()))
    val meal = FoodBundle(ownerId = repo.owner, name = "Breakfast", kind = "meal")
    repo.saveBundle(meal, listOf(BundleItem(bundleId = meal.id, snapshot = food.snapshot(), quantity = 1.0, unit = "serving", canonicalPerUnit = 100.0)))
    repo.close(); db.close()
    db = Room.databaseBuilder(ctx, GymDb::class.java, "nutrition-restart.db").build()
    repo = NutritionRepository(db, UserSession(ctx, db.dao()), ctx, emptyList(), emptyList())
    assertEquals(1, repo.diary(date, date).first().size); assertEquals(1, repo.goals().first().size)
    assertEquals(250.0, repo.water(date, date).first().single().ml, 0.0)
    assertEquals(80.0, repo.weights().first().single().kg, 0.0)
    assertEquals(1, repo.bundle(meal.id)!!.second.size); assertNotNull(repo.food(food.id))
  }
  @Test fun fullMigrationChainPreservesWorkoutAndUserRecords() {
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDb::class.java)
    helper.createDatabase("nutrition-migration.db", 1).apply {
      execSQL("INSERT INTO SetLog(date,exerciseId,setNo,reps,weight,units,notes) VALUES(1,'lift',1,10,100,'lb','history')")
      execSQL("INSERT INTO CustomWorkout(name,exerciseIdsCsv) VALUES('Legacy workout','lift')"); close()
    }
    val migrated = helper.runMigrationsAndValidate("nutrition-migration.db", 7, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
    migrated.query("SELECT notes FROM SetLog").use { assertTrue(it.moveToFirst()); assertEquals("history", it.getString(0)) }
    migrated.query("SELECT name FROM workouts").use { assertTrue(it.moveToFirst()); assertEquals("Legacy workout", it.getString(0)) }
    migrated.query("SELECT COUNT(*) FROM nutrition_diary").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }; migrated.close()
  }
  @Test fun versionSixUpgradePreservesEveryExistingTable() {
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDb::class.java)
    val before = helper.createDatabase("nutrition-six.db", 6)
    before.execSQL("INSERT INTO completed_sessions VALUES('s','owner-a','Legs','',1,1,'LOCAL',0)")
    before.execSQL("INSERT INTO SetLog(date,exerciseId,setNo,reps,weight,units,notes,sessionId,gymId,cardio) VALUES(1,'lift',1,5,10,'kg','','s','',NULL)")
    before.execSQL("INSERT INTO users VALUES('owner-a','Me','','USER','ACTIVE',1,1,'LOCAL')")
    val tables = mutableMapOf<String, Long>()
    before.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'room_master_table' AND name != 'android_metadata'").use { cursor ->
      while (cursor.moveToNext()) { val table = cursor.getString(0); before.query("SELECT COUNT(*) FROM `$table`").use { it.moveToFirst(); tables[table] = it.getLong(0) } }
    }; before.close()
    val migrated = helper.runMigrationsAndValidate("nutrition-six.db", 7, true, MIGRATION_6_7)
    tables.forEach { (table, count) -> migrated.query("SELECT COUNT(*) FROM `$table`").use { it.moveToFirst(); assertEquals(table, count, it.getLong(0)) } }; migrated.close()
  }
}
