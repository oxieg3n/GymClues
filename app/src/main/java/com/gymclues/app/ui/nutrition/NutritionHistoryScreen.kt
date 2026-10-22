package com.gymclues.app.ui.nutrition

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import java.time.*

@Composable internal fun WeightChart(logs: List<WeightLog>, unit: String) {
  if (logs.isEmpty()) { Text("No weigh-ins in this period"); return }
  val color = MaterialTheme.colorScheme.primary
  val values = logs.sortedBy { it.date }
  val min = values.minOf { it.kg }; val max = values.maxOf { it.kg }
  val first = LocalDate.parse(values.first().date).toEpochDay()
  val span = (LocalDate.parse(values.last().date).toEpochDay() - first).coerceAtLeast(1).toFloat()
  Canvas(Modifier.fillMaxWidth().height(160.dp).semantics { contentDescription = "Weight history, ${values.size} weigh-ins. Values are listed below the graph." }) {
    val path = Path()
    values.forEachIndexed { i, log ->
      val x = 8 + (size.width - 16) * (LocalDate.parse(log.date).toEpochDay() - first) / span
      val y = size.height - 8 - (size.height - 16) * ((log.kg - min) / (max - min).coerceAtLeast(1.0)).toFloat()
      if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
      drawCircle(color, 4.dp.toPx(), Offset(x, y))
    }
    drawPath(path, color, style = Stroke(2.dp.toPx()))
  }
  Text("${values.first().date} to ${values.last().date} · ${amount(weightIn(min, unit))}–${amount(weightIn(max, unit))} $unit")
}

@Composable fun NutritionHistoryScreen(app: AppContainer, nav: NavHostController) {
  var period by rememberSaveable { mutableStateOf("7") }
  var endText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
  var endSelected by rememberSaveable { mutableStateOf(endText) }
  val end = LocalDate.parse(endSelected); val start = end.minusDays(period.toLong() - 1)
  val days by remember(app, start, end) { app.nutrition.days(start, end) }.collectAsState(emptyList())
  val sessions by remember(app) { app.workouts.sessions() }.collectAsState(emptyList())
  val logs by remember(app) { app.workouts.history() }.collectAsState(emptyList())
  val weights by remember(app) { app.nutrition.weights() }.collectAsState(emptyList())
  val actions = rememberActions()
  val summary = remember(days) { intakeSummary(days) }
  val completed = sessions.filter { Instant.ofEpochMilli(it.completedAt).atZone(ZoneId.systemDefault()).toLocalDate() in start..end }
  val sessionIds = completed.map { it.id }.toSet()
  val volume = logs.filter { it.sessionId in sessionIds && it.cardio == null && it.units in listOf("kg", "lb") }.sumOf { kilograms(it.weight, it.units) * it.reps }
  val weightUnit = days.lastOrNull()?.goal?.weightUnit ?: "kg"
  NutritionPage {
    item { ScreenTitle("Nutrition history", "$start to $end") }
    item { Options(listOf("7", "30", "90"), period) { period = it }; Text("Days in period") }
    item { AppTextField(endText, { endText = it }, Modifier.fillMaxWidth(), label = { Text("Period ending (YYYY-MM-DD)") })
      AppOutlinedButton({ actions.run { validDate(endText); endSelected = endText } }) { Text("View period") }; ActionMessage(actions) }
    item { AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("${summary.loggedDays} of $period days logged", style = MaterialTheme.typography.titleLarge)
      Text("Average intake: ${amount(summary.averageCalories, 0)} kcal · ${amount(summary.averageProtein)} g protein")
      Text("Protein target met: ${summary.proteinMet} of ${summary.proteinCompared} comparable days")
      Text("Calories within 10% of target: ${summary.calorieAdherent} of ${summary.calorieCompared} comparable days")
      Text("${completed.size} completed workouts · ${completed.map { Instant.ofEpochMilli(it.completedAt).atZone(ZoneId.systemDefault()).toLocalDate() }.distinct().size} workout days")
      Text("Lifting volume: ${amount(volume, 0)} kg × reps (completed sessions only)")
      Text("Average protein: workout days ${amount(summary.trainingProtein)} g · rest days ${amount(summary.restProtein)} g")
      Muted("Averages exclude unlogged days and missing nutrient totals. Training days follow your program/rest calendar or a completed workout. These observations do not establish cause and effect.")
    } } }
    item { SectionTitle("Calories by day")
      val color = MaterialTheme.colorScheme.primary
      val maximum = days.mapNotNull { it.total.kcal }.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
      Canvas(Modifier.fillMaxWidth().height(140.dp).semantics { contentDescription = "Daily calorie chart. Gaps are unlogged or incomplete days; a dot represents a logged zero. Exact values follow." }) {
        val step = size.width / days.size.coerceAtLeast(1)
        days.forEachIndexed { index, day -> if (day.entries.isNotEmpty()) day.total.kcal?.let { value ->
          if (value == 0.0) drawCircle(color, 2.dp.toPx(), Offset(step * (index + .5f), size.height - 3))
          else { val h = (value / maximum * (size.height - 6)).toFloat(); drawRect(color, Offset(step * index + 1, size.height - h), Size((step - 2).coerceAtLeast(1f), h)) }
        } }
      }
      Muted("Gaps = no food logged or incomplete calories. A dot = a logged zero.")
    }
    item { SectionTitle("Weight trend"); WeightChart(weights.filter { LocalDate.parse(it.date) in start..end }, weightUnit)
      Text("Weekly average change: ${amount(weightTrend(weights, end, 7)?.let { weightIn(it, weightUnit) })} $weightUnit")
      Text("Monthly average change: ${amount(weightTrend(weights, end, 30)?.let { weightIn(it, weightUnit) })} $weightUnit")
      Muted("Each change compares two adjacent periods with recorded weigh-ins. Short-term fluctuations are not a measure of fat change.")
      AppOutlinedButton({ nav.navigate("nutrition/health/$end") }) { Text("Manage weight & water") }
    }
    days.reversed().forEach { day -> item {
      AppOutlinedButton({ nav.navigate("nutrition?date=${day.date}") }) { Text(day.date) }
      if (day.entries.isEmpty()) Text("No food logged") else NutrientValues(day.total)
      Text("Water ${amount(day.water.sumOf { it.ml }, 0)} ml · ${if (day.training) "Workout" else "Rest"} day")
    } }
  }
}
