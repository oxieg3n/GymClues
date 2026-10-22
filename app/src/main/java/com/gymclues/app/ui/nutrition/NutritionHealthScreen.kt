package com.gymclues.app.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import java.time.LocalDate

@Composable fun NutritionHealthScreen(app: AppContainer, initialDate: String) {
  var dateText by rememberSaveable { mutableStateOf(initialDate) }
  var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
  val date = LocalDate.parse(selectedDate)
  val days by remember(app, date) { app.nutrition.days(date, date) }.collectAsState(emptyList())
  val weights by remember(app) { app.nutrition.weights() }.collectAsState(emptyList())
  var weight by rememberSaveable { mutableStateOf("") }
  var weightUnit by rememberSaveable { mutableStateOf("kg") }
  var water by rememberSaveable { mutableStateOf("250") }
  var waterUnit by rememberSaveable { mutableStateOf("ml") }
  val actions = rememberActions()
  var editingWater by remember { mutableStateOf<WaterLog?>(null) }
  var waterEdit by remember { mutableStateOf("") }
  LaunchedEffect(days.firstOrNull()?.goal?.weightUnit) { days.firstOrNull()?.goal?.weightUnit?.let { unit ->
    weight.toDoubleOrNull()?.let { weight = weightIn(kilograms(it, weightUnit), unit).toString() }; weightUnit = unit
  } }
  NutritionPage {
    item { ScreenTitle("Weight & water", "Optional tracking, stored privately with your nutrition history") }
    item { AppTextField(dateText, { dateText = it }, Modifier.fillMaxWidth(), label = { Text("Date (YYYY-MM-DD)") })
      AppOutlinedButton({ actions.run { validDate(dateText); selectedDate = dateText; weight = weights.firstOrNull { it.date == dateText }?.let { weightIn(it.kg, weightUnit).toString() }.orEmpty() } }) { Text("Select date") } }
    item { ActionMessage(actions) }
    item { SectionTitle("Body weight")
      DecimalField("Weight on $selectedDate", weight) { weight = it }; Options(listOf("kg", "lb"), weightUnit) { unit ->
        weight.toDoubleOrNull()?.let { weight = weightIn(kilograms(it, weightUnit), unit).toString() }; weightUnit = unit
      }
      AppButton({ actions.run { app.nutrition.saveWeight(selectedDate, number(weight), weightUnit); actions.message = "Weigh-in saved" } }, enabled = !actions.busy) { Text("Save weigh-in") }
    }
    item { WeightChart(weights.filter { LocalDate.parse(it.date) in date.minusDays(89)..date }, weightUnit)
      Text("7-day average: ${amount(weightAverage(weights, date, 7)?.let { weightIn(it, weightUnit) })} $weightUnit")
      Text("30-day average: ${amount(weightAverage(weights, date, 30)?.let { weightIn(it, weightUnit) })} $weightUnit")
      Muted("Weigh-ins are optional. Daily fluctuations do not establish changes in body fat.")
    }
    (weights.asReversed().take(90) + weights.filter { it.date == selectedDate }).distinctBy { it.id }.forEach { log -> item {
      Text("${log.date}: ${amount(weightIn(log.kg, weightUnit))} $weightUnit")
      AppOutlinedButton({ selectedDate = log.date; dateText = log.date; weight = weightIn(log.kg, weightUnit).toString() }) { Text("Edit ${log.date} weigh-in") }
      TextButton({ actions.run { app.nutrition.deleteWeight(log.id) } }, enabled = !actions.busy) { Text("Delete ${log.date} weigh-in") }
    } }
    item { SectionTitle("Water · $selectedDate")
      Text("${amount(days.firstOrNull()?.water?.sumOf { it.ml }, 0)} / ${amount(days.firstOrNull()?.goal?.waterGoalMl, 0)} ml")
      DecimalField("Water quantity", water) { water = it }; Options(listOf("ml", "fl oz"), waterUnit) { waterUnit = it }
      AppButton({ actions.run(actionKey = "water-custom") { app.nutrition.saveWater(selectedDate, number(water), waterUnit, newId()) } }, enabled = !actions.busy) { Text("Add water") }
      AppOutlinedButton({ actions.run(actionKey = "water-250") { app.nutrition.saveWater(selectedDate, 250.0, "ml", newId()) } }, enabled = !actions.busy) { Text("Add 250 ml") }
      AppOutlinedButton({ actions.run(actionKey = "water-8") { app.nutrition.saveWater(selectedDate, 8.0, "fl oz", newId()) } }, enabled = !actions.busy) { Text("Add 8 fl oz") }
    }
    days.firstOrNull()?.water?.forEach { log -> item { AppCard(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(16.dp)) { Text("${amount(log.ml, 0)} ml")
        TextButton({ editingWater = log; waterEdit = log.ml.toString() }) { Text("Edit water entry") }
        TextButton({ actions.run { app.nutrition.deleteWater(log.id) } }, enabled = !actions.busy) { Text("Delete water entry") }
      }
    } } }
  }
  editingWater?.let { log -> NutritionDialog(onDismissRequest = { editingWater = null }, title = { Text("Edit water") },
    text = { Column { DecimalField("Milliliters", waterEdit) { waterEdit = it }; ActionMessage(actions) } },
    confirmButton = { TextButton({ actions.run({ editingWater = null }) { app.nutrition.editWater(log.id, number(waterEdit)) } }, enabled = !actions.busy) { Text("Save water") } },
    dismissButton = { TextButton({ editingWater = null }) { Text("Cancel") } }) }
}
