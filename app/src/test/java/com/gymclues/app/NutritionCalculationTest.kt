package com.gymclues.app

import com.gymclues.app.data.nutrition.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class NutritionCalculationTest {
  private fun food() = FoodEntity(ownerId = "owner", name = "Label food", basisQuantity = 100.0, basisUnit = "g", nutrients = Nutrients(120.0, 25.0, 2.0, 1.0))
  private fun entry(qty: Double): DiaryEntry { val f = food(); return DiaryEntry(ownerId = "owner", date = "2026-03-08", meal = "Lunch", foodId = f.id, snapshot = f.snapshot(), quantity = qty, unit = "g", canonicalPerUnit = 1.0) }

  @Test fun gramScalingUsesLabelCaloriesAndKeepsPrecision() {
    val n = entry(150.0).total
    assertEquals(180.0, n.kcal!!, 1e-9); assertEquals(37.5, n.protein!!, 1e-9)
    assertEquals(3.0, n.carbs!!, 1e-9); assertEquals(1.5, n.fat!!, 1e-9)
    assertEquals(30.0, entry(25.0).total.kcal!!, 1e-9)
    assertEquals(240.0, entry(200.0).total.kcal!!, 1e-9)
    assertNotEquals(25 * 4 + 2 * 4 + 9.0, food().nutrients.kcal!!, .01)
  }
  @Test fun noInventedVolumeConversions() {
    assertFalse(portions(food()).any { it.label == "cup" || it.label == "ml" })
    val explicit = food().copy(portions = listOf(FoodPortion("cup", 158.0)))
    assertEquals(158.0, portions(explicit).first { it.label == "cup" }.amount, 0.0)
    assertEquals(28.349523125, portions(food()).first { it.label == "oz" }.amount, 1e-10)
  }
  @Test fun weightsAndWaterRoundTrip() {
    assertEquals(45.359237, kilograms(100.0, "lb"), 1e-9)
    assertEquals(100.0, weightIn(kilograms(100.0, "lb"), "lb"), 1e-9)
    assertEquals(236.5882365, waterMl(8.0, "fl oz"), 1e-9)
  }
  @Test fun invalidQuantitiesAndDatesAreRejected() {
    listOf(0.0, -1.0, Double.MIN_VALUE, Double.NaN, Double.POSITIVE_INFINITY, 1_000_000.0).forEach { value ->
      assertThrows(IllegalArgumentException::class.java) { validQuantity(value) }
    }
    assertThrows(Exception::class.java) { validDate("2026-02-30") }
    assertThrows(IllegalArgumentException::class.java) { validateEntry(entry(-5.0)) }
    assertThrows(IllegalArgumentException::class.java) { validateFood(food().copy(nutrients = Nutrients(-1.0))) }
  }
  @Test fun unknownDoesNotBecomeZeroInTotals() {
    val n = nutritionTotal(listOf(Nutrients(120.0, null, 5.0, 3.0), Nutrients(50.0, 2.0, 0.0, 1.0)))
    assertEquals(170.0, n.kcal!!, 0.0); assertNull(n.protein); assertEquals(5.0, n.carbs!!, 0.0)
    assertEquals(0.0, nutritionTotal(emptyList()).kcal!!, 0.0)
  }
  @Test fun restGoalsAndUnloggedDaysAreHonest() {
    val goal = NutritionGoal(ownerId = "owner", effectiveDate = "2026-01-01", daily = Nutrients(2000.0, 100.0, 200.0, 50.0),
      rest = Nutrients(1700.0, 90.0, 160.0, 50.0), separateRest = true)
    val logged = NutritionDay("2026-03-08", listOf(entry(150.0)), goal, false, emptyList(), emptyList())
    assertEquals(1520.0, logged.remaining!!, 0.0)
    assertEquals(2000.0, logged.copy(training = true).target.kcal!!, 0.0)
    val summary = intakeSummary(listOf(logged, logged.copy(date = "2026-03-09", entries = emptyList())))
    assertEquals(1, summary.loggedDays); assertEquals(180.0, summary.averageCalories!!, 0.0)
  }
  @Test fun rankingPrioritizesRelevantHistoryThenCustomAndFavorites() {
    val f = food(); val cached = f.copy(id = "c", source = "off", sourceId = "c", name = "Chicken cached")
    val custom = f.copy(id = "u", sourceId = "u", name = "Chicken custom")
    val recent = cached.copy(id = "r", sourceId = "r", name = "Chicken recent")
    val history = entry(1.0).copy(foodId = "r")
    val ranked = rankedFoods(listOf(cached, custom, recent, cached, f.copy(name = "Rice")), listOf(history), "chicken")
    assertEquals(listOf("r", "u", "c"), ranked.map { it.id })
    assertEquals(1, rankedFoods(listOf(custom.copy(favorite = true), cached), emptyList(), "", "Favorites").size)
    val usage = listOf(FoodUsage(cached.id, 100, 1), FoodUsage(recent.id, 2, 5))
    assertEquals(listOf(cached.id, recent.id), rankedFoods(listOf(recent, cached), emptyList(), "chicken", "Frequent", usage).map { it.id })
  }
  @Test fun weeklyWeightTrendUsesOnlyRecordedMeasurements() {
    val end = LocalDate.parse("2026-03-15")
    val logs = listOf(WeightLog(ownerId = "o", date = "2026-03-02", kg = 80.0), WeightLog(ownerId = "o", date = "2026-03-12", kg = 79.0))
    assertEquals(-1.0, weightTrend(logs, end, 7)!!, 0.0)
    assertNull(weightTrend(logs, end, 30))
  }
}
