package com.gymclues.app.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gymclues.app.data.AppContainer
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.flow.first

@Composable fun NutritionGoalsScreen(app: AppContainer, date: String) {
  val actions = rememberActions()
  var loaded by rememberSaveable { mutableStateOf(false) }
  var daily by rememberSaveable { mutableStateOf(List(4) { "0" }) }
  var rest by rememberSaveable { mutableStateOf(List(4) { "0" }) }
  var separate by rememberSaveable { mutableStateOf(false) }
  var purpose by rememberSaveable { mutableStateOf("Custom") }
  var weightUnit by rememberSaveable { mutableStateOf("kg") }
  var foodUnit by rememberSaveable { mutableStateOf("g") }
  var water by rememberSaveable { mutableStateOf("0") }
  var proxy by rememberSaveable { mutableStateOf(app.nutritionSettings.getString("usdaProxy", "").orEmpty()) }
  var deleting by remember { mutableStateOf(false) }
  LaunchedEffect(date) { if (!loaded) {
    app.nutrition.goals().first().filter { it.effectiveDate <= date }.maxByOrNull { it.effectiveDate }?.let { g ->
      daily = g.daily.values().take(4).map { (it ?: 0.0).toString() }; rest = g.rest.values().take(4).map { (it ?: 0.0).toString() }
      separate = g.separateRest; purpose = g.purpose; weightUnit = g.weightUnit; foodUnit = g.foodUnit; water = g.waterGoalMl.toString()
    }; loaded = true
  } }
  NutritionPage {
    item { ScreenTitle("Nutrition goals", "Manual targets effective from $date. Earlier goal versions remain available for historical comparisons.") }
    item { Options(listOf("Lose weight", "Maintain weight", "Gain muscle/weight", "Custom"), purpose) { purpose = it } }
    item { Text("Daily / workout-day targets", style = MaterialTheme.typography.titleMedium) }
    listOf("Calories (kcal)", "Protein (g)", "Carbohydrates (g)", "Fat (g)").forEachIndexed { i, label ->
      item { DecimalField(label, daily[i]) { v -> daily = daily.toMutableList().also { it[i] = v } } }
    }
    item { Row { Checkbox(separate, { separate = it }, modifier = Modifier.semantics { contentDescription = "Separate workout and rest-day targets" }); Text("Use separate workout and rest-day targets") } }
    if (separate) {
      item { Text("Rest-day targets", style = MaterialTheme.typography.titleMedium) }
      listOf("Rest calories (kcal)", "Rest protein (g)", "Rest carbohydrates (g)", "Rest fat (g)").forEachIndexed { i, label ->
        item { DecimalField(label, rest[i]) { v -> rest = rest.toMutableList().also { it[i] = v } } }
      }
    }
    item { Text("Weight units"); Options(listOf("kg", "lb"), weightUnit) { weightUnit = it } }
    item { Text("Preferred food weight units"); Options(listOf("g", "oz"), foodUnit) { foodUnit = it } }
    item { DecimalField("Daily water goal (ml, 0 = no goal)", water) { water = it } }
    item { ActionMessage(actions) }
    item { AppButton({ actions.run {
      fun targets(values: List<String>): Nutrients { val n = values.map(::number); return Nutrients(n[0], n[1], n[2], n[3]) }
      app.nutrition.saveGoal(NutritionGoal(ownerId = app.nutrition.owner, effectiveDate = date, daily = targets(daily), rest = targets(rest),
        separateRest = separate, purpose = purpose, weightUnit = weightUnit, foodUnit = foodUnit, waterGoalMl = number(water)))
      actions.message = "Nutrition goals saved"
    } }, enabled = loaded && !actions.busy) { Text("Save nutrition goals") } }
    item { SectionTitle("Food data sources"); Text("Open Food Facts is enabled without a key. Its public, contributed product data is licensed ODbL 1.0 / DbCL 1.0. Verify foods against their labels.") }
    item { AppTextField(proxy, { proxy = it }, Modifier.fillMaxWidth(), label = { Text("USDA HTTPS proxy URL (optional)") }) }
    item { AppOutlinedButton({ actions.run {
      require(proxy.isBlank() || UsdaFoodProvider.validProxy(proxy)) { "Enter an HTTPS base URL without credentials or a query" }
      app.nutrition.configureUsda(proxy); app.nutritionSettings.edit().putString("usdaProxy", proxy.takeIf { it.isNotBlank() }).apply()
      actions.message = if (proxy.isBlank()) "USDA disabled. Open Food Facts remains available." else "USDA proxy configured. Your server must add its private API key."
    } }, enabled = !actions.busy) { Text("Save food provider configuration") } }
    item { Text("Nutrition, water and weight are private local records. No diary or weight history is sent to food providers or the GymClues community. Search text and barcode lookups are sent only when requested.") }
    item { TextButton({ deleting = true }, enabled = !actions.busy) { Text("Delete all my nutrition data") } }
  }
  if (deleting) NutritionDialog(onDismissRequest = { deleting = false }, title = { Text("Delete nutrition data?") },
    text = { Text("Permanently delete your food diary, custom foods, saved meals, recipes, goals, weight and water records, and provider caches for this user. Workouts are preserved.") },
    confirmButton = { TextButton({ actions.run({ deleting = false }) { app.nutrition.deleteAll(); actions.message = "Your nutrition data was deleted" } }, enabled = !actions.busy) { Text("Delete nutrition data") } },
    dismissButton = { TextButton({ deleting = false }, enabled = !actions.busy) { Text("Cancel") } })
}
