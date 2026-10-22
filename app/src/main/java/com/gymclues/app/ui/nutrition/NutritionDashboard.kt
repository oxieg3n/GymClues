package com.gymclues.app.ui.nutrition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import java.time.LocalDate

@Composable fun HomeNutrition(app: AppContainer, go: () -> Unit) {
  val today = LocalDate.now()
  val days by remember(app, today) { app.nutrition.days(today, today) }.collectAsState(emptyList())
  AppCard(Modifier.fillMaxWidth().clickable(onClick = go)) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Nutrition", style = MaterialTheme.typography.titleMedium)
      days.firstOrNull()?.let { day ->
        Text("${amount(day.total.kcal, 0)} / ${amount(day.target.kcal, 0)} kcal")
        Text("Protein ${amount(day.total.protein)} / ${amount(day.target.protein)} g")
        Muted(if (day.goal == null) "Set your nutrition goals" else "${if (day.training) "Workout" else "Rest"} day · Open food diary")
      } ?: Text("Loading your food diary…")
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun NutritionDashboard(app: AppContainer, nav: NavHostController, initialDate: String? = null) {
  var dateText by rememberSaveable { mutableStateOf(initialDate ?: LocalDate.now().toString()) }
  val date = runCatching { LocalDate.parse(dateText) }.getOrDefault(LocalDate.now())
  val days by remember(app, date) { app.nutrition.days(date, date) }.collectAsState(emptyList())
  val day = days.firstOrNull()
  val actions = rememberActions()
  var edit by remember { mutableStateOf<DiaryEntry?>(null) }
  var quantity by remember { mutableStateOf("") }
  var meal by remember { mutableStateOf("Snacks") }
  var copyDate by rememberSaveable { mutableStateOf(date.minusDays(1).toString()) }
  var copyMeal by rememberSaveable { mutableStateOf("Entire day") }
  NutritionPage {
    item { ScreenTitle("Nutrition", "Your food diary · private on this device") }
    item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
      IconButton({ dateText = date.minusDays(1).toString() }) { Icon(Icons.Filled.ChevronLeft, "Previous nutrition day") }
      Text(date.toString(), style = MaterialTheme.typography.titleMedium)
      IconButton({ dateText = date.plusDays(1).toString() }) { Icon(Icons.Filled.ChevronRight, "Next nutrition day") }
    } }
    item { ActionMessage(actions) }
    if (day == null) item { CircularProgressIndicator() } else {
      item { AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${amount(day.total.kcal, 0)} kcal consumed", style = MaterialTheme.typography.headlineSmall)
        Text(if (day.goal == null) "Set a calorie target to see remaining calories" else "${amount(day.remaining, 0)} kcal remaining · ${amount(day.target.kcal, 0)} target")
        NutrientProgress("Calories", day.total.kcal, day.target.kcal, "kcal")
        NutrientProgress("Protein", day.total.protein, day.target.protein)
        NutrientProgress("Carbohydrates", day.total.carbs, day.target.carbs)
        NutrientProgress("Fat", day.total.fat, day.target.fat)
        Muted("${if (day.training) "Workout" else "Rest"} day${if (day.goal?.separateRest == true) " targets" else " · daily targets"}. Exercise calories are kept separate.")
        AppOutlinedButton({ nav.navigate("nutrition/goals/$date") }) { Text("Nutrition goals") }
      } } }
      item { FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppOutlinedButton({ nav.navigate("nutrition/saved/$date/Snacks") }) { Text("Meals & recipes") }
        AppOutlinedButton({ nav.navigate("nutrition/history") }) { Text("Nutrition history") }
        AppOutlinedButton({ nav.navigate("nutrition/health/$date") }) { Text("Weight & water") }
        AppOutlinedButton({ nav.navigate(editorPath(date.toString(), "Snacks", quick = true)) }) { Text("Quick add") }
      } }
      MEALS.forEach { category ->
        val entries = day.entries.filter { it.meal == category }
        item { AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(category, style = MaterialTheme.typography.titleLarge)
          Text("${amount(nutritionTotal(entries.map { it.total }).kcal, 0)} kcal · ${entries.size} foods")
          entries.forEach { entry ->
            Column(Modifier.fillMaxWidth().clickable { edit = entry; quantity = entry.quantity.toString(); meal = entry.meal }.padding(vertical = 8.dp)) {
              Text(entry.snapshot.name, style = MaterialTheme.typography.titleMedium)
              Muted("${amount(entry.quantity)} × ${entry.unit} · ${amount(entry.total.kcal, 0)} kcal · Protein ${amount(entry.total.protein)} g")
              Muted("Tap to edit · ${entry.snapshot.brand.ifBlank { entry.snapshot.source }}")
            }
          }
          AppButton({ nav.navigate(searchPath(date.toString(), category)) }) { Text("Add food to $category") }
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppOutlinedButton({ actions.run(actionKey = "copy-$category") { val count = app.nutrition.copyDay(date.minusDays(1).toString(), date.toString(), category, newId()); actions.message = "$count foods copied" } }, modifier = Modifier.semantics { contentDescription = "Copy yesterday's $category" }, enabled = !actions.busy) { Text("Copy yesterday") }
            if (entries.isNotEmpty()) AppOutlinedButton({ nav.navigate(bundlePath("meal", date.toString(), category)) }) { Text("Save as meal") }
          }
        } } }
      }
      item { SectionTitle("Copy a previous day"); AppTextField(copyDate, { copyDate = it }, Modifier.fillMaxWidth(), label = { Text("Copy from date (YYYY-MM-DD)") }) }
      item { Options(listOf("Entire day") + MEALS, copyMeal) { copyMeal = it } }
      item { AppOutlinedButton({ actions.run(actionKey = "copy-$copyDate-$copyMeal") { val count = app.nutrition.copyDay(copyDate, date.toString(), copyMeal.takeIf { it in MEALS }, newId()); actions.message = "$count foods copied" } }, enabled = !actions.busy) { Text(if (copyMeal == "Entire day") "Copy entire day" else "Copy $copyMeal") } }
      item { Text("Water: ${amount(day.water.sumOf { it.ml }, 0)} / ${amount(day.goal?.waterGoalMl, 0)} ml") }
    }
  }
  edit?.let { entry -> NutritionDialog(onDismissRequest = { if (!actions.busy) edit = null }, title = { Text(entry.snapshot.name) },
    text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text("Serving: ${entry.unit}"); DecimalField("Quantity", quantity) { quantity = it }
      Options(MEALS, meal) { meal = it }
      ActionMessage(actions)
      AppOutlinedButton({ actions.run({ edit = null }) { app.nutrition.repeat(entry.id, date.toString(), meal, newId()) } }, enabled = !actions.busy) { Text("Repeat this food") }
      TextButton({ actions.run({ edit = null }) { app.nutrition.deleteEntry(entry.id) } }, enabled = !actions.busy) { Text("Delete entry") }
    } }, confirmButton = { TextButton({ actions.run({ edit = null }) { app.nutrition.editEntry(entry.id, number(quantity), meal) } }, enabled = !actions.busy) { Text("Save entry") } },
    dismissButton = { TextButton({ edit = null }, enabled = !actions.busy) { Text("Cancel") } }) }
}
