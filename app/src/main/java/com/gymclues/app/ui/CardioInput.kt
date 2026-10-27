package com.gymclues.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymclues.app.data.*

class CardioDraft(val tracking: String, initial: CardioMetrics? = null, seconds: Int = 0) {
  var time by mutableStateOf(initial?.minutes?.let { minutes ->
    val sec = (minutes * 60).toLong()
    if (kotlin.math.abs(minutes * 60 - sec) < 0.000001) "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}" else minutes.toString()
  } ?: if (seconds > 0) "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}" else "")
  var speed by mutableStateOf(initial?.speed?.let(::cardioNumber).orEmpty())
  var incline by mutableStateOf(initial?.incline?.let(::cardioNumber).orEmpty())
  var floors by mutableStateOf(initial?.floors?.toString().orEmpty())
  var distance by mutableStateOf(initial?.distance?.let(::cardioNumber).orEmpty())
  var resistance by mutableStateOf(initial?.resistance?.let(::cardioNumber).orEmpty())
  var edited by mutableStateOf(false)
  val distanceUnit = initial?.distanceUnit ?: if (tracking == "rower") "m" else "mi"
  val speedUnit = initial?.speedUnit ?: if (tracking == "stairs") "steps/min" else "mph"
  val hasInput get() = listOf(time, speed, incline, floors, distance, resistance).any { it.isNotBlank() }
  private fun number(s: String) = if (s.isBlank()) null else s.toDoubleOrNull() ?: Double.NaN
  fun metrics(): CardioMetrics? {
    val minutes = cardioMinutes(time) ?: return null
    if (floors.isNotBlank() && floors.toIntOrNull() == null) return null
    return CardioMetrics(minutes, number(speed), number(incline), floors.toIntOrNull(), number(distance),
      number(resistance), distanceUnit, speedUnit).takeIf { it.valid() }
  }
}

@Composable fun CardioInput(draft: CardioDraft, enabled: Boolean = true, showErrors: Boolean = false) {
  @Composable fun field(value: String, change: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    AppTextField(value, change, modifier, label = { Text(label) }, singleLine = true, enabled = enabled,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
  }
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    field(draft.time, { draft.time = it; draft.edited = true }, "Minutes", Modifier.fillMaxWidth())
    Muted("Enter decimal minutes or m:ss (for example 0:20).")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      when (draft.tracking) {
        "locomotion" -> {
          field(draft.speed, { draft.speed = it; draft.edited = true }, "Speed (${draft.speedUnit})", Modifier.weight(1f))
          field(draft.incline, { draft.incline = it; draft.edited = true }, "Incline (%)", Modifier.weight(1f))
        }
        "stairs" -> {
          field(draft.speed, { draft.speed = it; draft.edited = true }, "Speed (steps/min)", Modifier.weight(1f))
          field(draft.floors, { draft.floors = it; draft.edited = true }, "Floors", Modifier.weight(1f))
        }
        else -> {
          field(draft.distance, { draft.distance = it; draft.edited = true }, "Distance (${draft.distanceUnit})", Modifier.weight(1f))
          field(draft.resistance, { draft.resistance = it; draft.edited = true }, "Resistance", Modifier.weight(1f))
        }
      }
    }
    if (draft.tracking == "locomotion") field(draft.distance, { draft.distance = it; draft.edited = true }, "Distance (${draft.distanceUnit})", Modifier.fillMaxWidth())
    if (draft.tracking in listOf("bike", "rower")) Muted("Use the machine's resistance setting; levels vary by model.")
    if (showErrors && draft.metrics() == null) Text("Enter a positive time and valid measurements. Optional fields may be blank; use nonnegative numbers (incline may be negative).", color = MaterialTheme.colorScheme.error)
  }
}
