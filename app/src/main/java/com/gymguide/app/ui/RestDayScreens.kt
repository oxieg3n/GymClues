package com.gymguide.app.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymguide.app.data.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale

/** Wide, short red button under the home calendar. */
@Composable fun RestDayButton(app: AppContainer, go: (String) -> Unit) {
  val rest by app.programs.restRules().collectAsState(RestRules.NONE)
  val scope = rememberCoroutineScope()
  var open by remember { mutableStateOf(false) }
  val today = LocalDate.now(); val isRest = rest.isRest(today)
  Button({ open = true }, Modifier.fillMaxWidth().height(40.dp), shape = MaterialTheme.shapes.medium, contentPadding = PaddingValues(horizontal = 16.dp),
    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFD32F2F), contentColor = androidx.compose.ui.graphics.Color.White)) {
    Icon(Icons.Filled.Bedtime, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (isRest) "Rest Day (today is off)" else "Rest Day") }
  if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text("Rest day") },
    text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Muted("Your program moves back so you don't miss a session.")
      if (isRest) OutlinedButton({ scope.launch { app.programs.removeRestDate(today) }; open = false }, Modifier.fillMaxWidth()) { Text("Undo today's rest day") }
      else Button({ scope.launch { app.programs.addRestDate(today) }; open = false }, Modifier.fillMaxWidth()) { Text("Set Today as Rest Day") }
      OutlinedButton({ open = false; go("restdays") }, Modifier.fillMaxWidth()) { Text("Plan Rest Day") } } },
    confirmButton = {}, dismissButton = { TextButton({ open = false }) { Text("Cancel") } })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun RestDaysScreen(app: AppContainer) {
  val rules by app.programs.restDays().collectAsState(emptyList())
  val scope = rememberCoroutineScope()
  val today = LocalDate.now(); val t = today.toString()
  val weekly = rules.filter { it.weekday != 0 && (it.endDate.isBlank() || it.endDate > t) }.map { it.weekday }.toSet()
  val singles = rules.filter { it.weekday == 0 && it.startDate >= t }.sortedBy { it.startDate }
  var picking by remember { mutableStateOf(false) }
  var warn by remember { mutableStateOf(false) }
  if (picking) {
    val st = rememberDatePickerState(selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long) = !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isBefore(today) })
    DatePickerDialog({ picking = false }, confirmButton = { TextButton({
      st.selectedDateMillis?.let { m -> scope.launch { app.programs.addRestDate(Instant.ofEpochMilli(m).atZone(ZoneOffset.UTC).toLocalDate()) } }; picking = false },
      enabled = st.selectedDateMillis != null) { Text("Add") } }, dismissButton = { TextButton({ picking = false }) { Text("Cancel") } }) { DatePicker(st) }
  }
  LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item { Text("Rest days", style = MaterialTheme.typography.headlineSmall)
      Muted("Your program skips these days and picks up on the next one.") }
    item { SectionTitle("Every week") }
    item { FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      DayOfWeek.values().forEach { d -> FilterChip(d.value in weekly, { scope.launch { warn = !app.programs.setWeekly(d.value, d.value !in weekly) } },
        { Text(d.getDisplayName(TextStyle.SHORT, Locale.getDefault())) }) } } }
    if (warn) item { Text("Keep at least one training day.", color = MaterialTheme.colorScheme.error) }
    item { Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
      SectionTitle("Single days"); Spacer(Modifier.weight(1f))
      FilledTonalButton({ picking = true }) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text("Add date") } } }
    if (singles.isEmpty()) item { Muted("No upcoming rest days planned.") }
    items(singles, key = { it.id }) { r -> val d = LocalDate.parse(r.startDate)
      ListItem(headlineContent = { Text(d.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d"))) },
        trailingContent = { IconButton({ scope.launch { app.programs.removeRestDate(d) } }) { Icon(Icons.Filled.Delete, "Remove rest day") } }) }
  }
}
