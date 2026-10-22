package com.gymclues.app.data.nutrition

import java.time.LocalDate

data class IntakeSummary(val loggedDays: Int, val calorieDays: Int, val averageCalories: Double?, val averageProtein: Double?,
  val proteinMet: Int, val proteinCompared: Int, val calorieAdherent: Int, val calorieCompared: Int,
  val trainingProtein: Double?, val restProtein: Double?)
fun intakeSummary(days: List<NutritionDay>): IntakeSummary {
  val logged = days.filter { it.entries.isNotEmpty() }
  val proteinGoals = logged.filter { it.total.protein != null && (it.target.protein ?: 0.0) > 0 }
  val calorieGoals = logged.filter { it.total.kcal != null && (it.target.kcal ?: 0.0) > 0 }
  fun average(values: List<Double>) = values.takeIf { it.isNotEmpty() }?.average()
  return IntakeSummary(logged.size, logged.count { it.total.kcal != null }, average(logged.mapNotNull { it.total.kcal }),
    average(logged.mapNotNull { it.total.protein }), proteinGoals.count { it.total.protein!! >= it.target.protein!! }, proteinGoals.size,
    calorieGoals.count { kotlin.math.abs(it.total.kcal!! - it.target.kcal!!) <= it.target.kcal!! * .1 }, calorieGoals.size,
    average(logged.filter { it.training }.mapNotNull { it.total.protein }), average(logged.filterNot { it.training }.mapNotNull { it.total.protein }))
}
fun weightAverage(logs: List<WeightLog>, end: LocalDate, days: Long): Double? = logs.filter {
  !it.deleted && LocalDate.parse(it.date) in end.minusDays(days - 1)..end
}.map { it.kg }.takeIf { it.isNotEmpty() }?.average()
fun weightTrend(logs: List<WeightLog>, end: LocalDate, days: Long): Double? {
  val current = weightAverage(logs, end, days) ?: return null
  val previous = weightAverage(logs, end.minusDays(days), days) ?: return null
  return current - previous
}
