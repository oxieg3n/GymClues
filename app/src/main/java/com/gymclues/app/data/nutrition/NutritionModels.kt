package com.gymclues.app.data.nutrition

import androidx.room.*
import com.gymclues.app.data.newId
import com.gymclues.app.data.now
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Serializable data class Nutrients(val kcal: Double? = null, val protein: Double? = null, val carbs: Double? = null,
  val fat: Double? = null, val fiber: Double? = null, val sugar: Double? = null, val sodium: Double? = null) {
  fun values() = listOf(kcal, protein, carbs, fat, fiber, sugar, sodium)
  fun valid() = values().all { it == null || (it.isFinite() && it in 0.0..1_000_000.0) }
  fun scale(factor: Double): Nutrients {
    require(factor.isFinite() && factor >= 0)
    fun times(v: Double?) = v?.let { BigDecimal.valueOf(it).multiply(BigDecimal.valueOf(factor)).toDouble() }
    return Nutrients(times(kcal), times(protein), times(carbs), times(fat), times(fiber), times(sugar), times(sodium))
  }
  operator fun plus(other: Nutrients): Nutrients {
    fun add(a: Double?, b: Double?) = if (a == null || b == null) null else a + b
    return Nutrients(add(kcal, other.kcal), add(protein, other.protein), add(carbs, other.carbs), add(fat, other.fat),
      add(fiber, other.fiber), add(sugar, other.sugar), add(sodium, other.sodium))
  }
  companion object { val ZERO = Nutrients(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) }
}

@Serializable data class FoodPortion(val label: String, val amount: Double)

/** Nutrients describe basisQuantity of basisUnit, never a guessed cup or food density. */
@Entity(tableName = "nutrition_foods", indices = [Index(value = ["ownerId", "source", "sourceId"], unique = true), Index(value = ["ownerId", "barcode"])])
data class FoodEntity(@PrimaryKey val id: String = newId(), val ownerId: String, val name: String, val brand: String = "",
  val barcode: String = "", val source: String = "user", val sourceId: String = newId(), val sourceUrl: String = "",
  val license: String = "Private user data", val basisQuantity: Double = 1.0, val basisUnit: String = "serving",
  @Embedded val nutrients: Nutrients, val portions: List<FoodPortion> = emptyList(), val favorite: Boolean = false,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = "LOCAL", val deleted: Boolean = false) {
  fun snapshot() = FoodSnapshot(id, name, brand, source, sourceUrl, license, basisQuantity, basisUnit, nutrients)
}

@Serializable data class FoodSnapshot(val foodId: String, val name: String, val brand: String, val source: String,
  val sourceUrl: String, val license: String, val basisQuantity: Double, val basisUnit: String, val nutrients: Nutrients,
  val ingredients: List<RecipeComponent> = emptyList())
@Serializable data class RecipeComponent(val food: FoodSnapshot, val quantity: Double, val unit: String, val canonicalPerUnit: Double = 1.0)

@Entity(tableName = "nutrition_diary", indices = [Index(value = ["ownerId", "date"]), Index(value = ["ownerId", "foodId"])])
data class DiaryEntry(@PrimaryKey val id: String = newId(), val ownerId: String, val date: String, val meal: String,
  val foodId: String, val snapshot: FoodSnapshot, val quantity: Double, val unit: String, val canonicalPerUnit: Double,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = "LOCAL", val deleted: Boolean = false) {
  val total: Nutrients get() = snapshot.nutrients.scale(quantity * canonicalPerUnit / snapshot.basisQuantity)
}

@Entity(tableName = "nutrition_bundles", indices = [Index(value = ["ownerId", "kind"])])
data class FoodBundle(@PrimaryKey val id: String = newId(), val ownerId: String, val name: String, val kind: String,
  val servings: Double = 1.0, val instructions: String = "", val createdAt: Long = now(), val updatedAt: Long = now(),
  val syncState: String = "LOCAL", val deleted: Boolean = false)

@Serializable @Entity(tableName = "nutrition_bundle_items", foreignKeys = [ForeignKey(entity = FoodBundle::class,
  parentColumns = ["id"], childColumns = ["bundleId"], onDelete = ForeignKey.CASCADE)], indices = [Index("bundleId")])
data class BundleItem(@PrimaryKey val id: String = newId(), val bundleId: String, val snapshot: FoodSnapshot,
  val quantity: Double, val unit: String, val canonicalPerUnit: Double) {
  val total: Nutrients get() = snapshot.nutrients.scale(quantity * canonicalPerUnit / snapshot.basisQuantity)
}

@Entity(tableName = "nutrition_goals", indices = [Index(value = ["ownerId", "effectiveDate"], unique = true)])
data class NutritionGoal(@PrimaryKey val id: String = newId(), val ownerId: String, val effectiveDate: String,
  @Embedded(prefix = "daily_") val daily: Nutrients = Nutrients(0.0, 0.0, 0.0, 0.0),
  @Embedded(prefix = "rest_") val rest: Nutrients = Nutrients(0.0, 0.0, 0.0, 0.0), val separateRest: Boolean = false,
  val purpose: String = "Custom", val weightUnit: String = "kg", val foodUnit: String = "g", val waterGoalMl: Double = 0.0,
  val updatedAt: Long = now(), val syncState: String = "LOCAL")

@Entity(tableName = "nutrition_weights", indices = [Index(value = ["ownerId", "date"], unique = true)])
data class WeightLog(@PrimaryKey val id: String = newId(), val ownerId: String, val date: String, val kg: Double,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = "LOCAL", val deleted: Boolean = false)

@Entity(tableName = "nutrition_water", indices = [Index(value = ["ownerId", "date"])])
data class WaterLog(@PrimaryKey val id: String = newId(), val ownerId: String, val date: String, val ml: Double,
  val createdAt: Long = now(), val updatedAt: Long = now(), val syncState: String = "LOCAL", val deleted: Boolean = false)

class NutritionConverters {
  private val json = Json { ignoreUnknownKeys = true }
  @TypeConverter fun fromSnapshot(v: FoodSnapshot): String = json.encodeToString(v)
  @TypeConverter fun toSnapshot(v: String): FoodSnapshot = json.decodeFromString(v)
  @TypeConverter fun fromPortions(v: List<FoodPortion>): String = json.encodeToString(v)
  @TypeConverter fun toPortions(v: String): List<FoodPortion> = json.decodeFromString(v)
}

val MEALS = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
fun stableNutritionId(value: String): String = UUID.nameUUIDFromBytes(value.toByteArray(Charsets.UTF_8)).toString()
fun validDate(value: String) { require(LocalDate.parse(value).toString() == value) { "Use a valid local date" } }
fun validQuantity(value: Double) { require(value.isFinite() && value in .000001..100_000.0) { "Enter a quantity between 0.000001 and 100,000" } }
fun nutritionTotal(values: List<Nutrients>) = values.fold(Nutrients.ZERO, Nutrients::plus)
fun kilograms(value: Double, unit: String): Double { require(unit in listOf("kg", "lb")); return if (unit == "lb") value * .45359237 else value }
fun weightIn(kg: Double, unit: String) = if (unit == "lb") kg / .45359237 else kg
fun waterMl(value: Double, unit: String): Double { require(unit in listOf("ml", "fl oz")); return if (unit == "fl oz") value * 29.5735295625 else value }
fun portions(food: FoodEntity): List<FoodPortion> = (listOf(FoodPortion("${food.basisQuantity} ${food.basisUnit}", food.basisQuantity)) +
  when (food.basisUnit) { "g" -> listOf(FoodPortion("g", 1.0), FoodPortion("oz", 28.349523125))
    "ml" -> listOf(FoodPortion("ml", 1.0), FoodPortion("fl oz", 29.5735295625)); else -> emptyList() } + food.portions).distinctBy { it.label }

data class NutritionDay(val date: String, val entries: List<DiaryEntry>, val goal: NutritionGoal?, val training: Boolean,
  val water: List<WaterLog>, val weights: List<WeightLog>) {
  val total get() = nutritionTotal(entries.map { it.total })
  val target get() = goal?.let { if (it.separateRest && !training) it.rest else it.daily } ?: Nutrients.ZERO
  val remaining get() = total.kcal?.let { target.kcal?.minus(it) }
}
