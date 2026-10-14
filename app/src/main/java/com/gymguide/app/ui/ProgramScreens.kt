package com.gymguide.app.ui
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymguide.app.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

private val longFmt = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d")
private fun current(list: List<ProgramAssignmentEntity>): ProgramAssignmentEntity? { val t = LocalDate.now().toString()
  return list.filter { !it.deleted && (it.endDate.isBlank() || it.endDate > t) }.maxByOrNull { it.startDate } }

@Composable fun ProgramsScreen(app: AppContainer, nav: NavHostController) {
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val cur = current(assignments)
  val scope = rememberCoroutineScope()
  var assigning by remember { mutableStateOf<WorkoutProgram?>(null) }
  assigning?.let { AssignDialog(it, { assigning = null }) { start -> scope.launch { app.programs.assign(it.id, start) }; assigning = null } }
  LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
    item { Text("Workout programs", style = MaterialTheme.typography.headlineSmall)
      Muted("Pick one program at a time. Its sessions appear on your home calendar.") }
    items(app.programs.programs, key = { it.id }) { p ->
      val mine = cur?.programId == p.id
      Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(if (mine) 1.5.dp else 1.dp, if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(p.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            AssistChip({}, label = { Text(p.split) })
          }
          Muted("${p.author} · ${p.level} · ${p.duration}")
          if (mine) Text(if (cur!!.startDate > LocalDate.now().toString()) "Assigned. Starts ${LocalDate.parse(cur.startDate).format(longFmt)}" else "Your current program",
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
          Text(p.description, style = MaterialTheme.typography.bodyMedium)
          Muted("Cycle: ${p.schedule}")
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ nav.navigate(Routes.program(p.id)) }, Modifier.weight(1f)) { Text("Details") }
            Button({ assigning = p }, Modifier.weight(1f)) { Text(if (mine) "Change start" else "Assign to me") }
          }
        }
      }
    }
    if (cur != null) item { TextButton({ scope.launch { app.programs.stop() } }, Modifier.fillMaxWidth()) { Text("Stop current program") } }
  }
}

@Composable private fun AssignDialog(p: WorkoutProgram, dismiss: () -> Unit, start: (LocalDate) -> Unit) {
  val today = LocalDate.now()
  val options = listOf("Today" to today, "Tomorrow" to today.plusDays(1), "Next Monday" to today.with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
  var pick by remember { mutableIntStateOf(1) }
  AlertDialog(onDismissRequest = dismiss, title = { Text("Start ${p.name}") },
    text = { Column { Muted("Day 1 of the cycle (${p.days.first().session}) falls on the start date.")
      options.forEachIndexed { i, (label, d) -> Row(Modifier.fillMaxWidth().clickable { pick = i }, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(pick == i, { pick = i }); Text("$label  (${d.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))})") } } } },
    confirmButton = { TextButton({ start(options[pick].second) }) { Text("Assign") } },
    dismissButton = { TextButton(dismiss) { Text("Cancel") } })
}

@Composable fun ProgramDetail(app: AppContainer, id: String, nav: NavHostController) {
  val p = app.programs.find(id) ?: return NotFound("Program not found.")
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  var assigning by remember { mutableStateOf(false) }
  if (assigning) AssignDialog(p, { assigning = false }) { start -> scope.launch { app.programs.assign(p.id, start) }; assigning = false }
  LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
    item { Text(p.name, style = MaterialTheme.typography.headlineSmall); Muted("${p.author} · ${p.level} · ${p.duration}") }
    item { Text(p.description) }
    item { Button({ assigning = true }, Modifier.fillMaxWidth().height(52.dp)) { Text("Assign to me") } }
    item { SectionTitle("Cycle (${p.cycleLength} days)") }
    items(p.days, key = { it.day }) { d ->
      ListItem(headlineContent = { Text("Day ${d.day}: ${d.session}") },
        supportingContent = { Text(if (d.training) "${d.items.count { it.phase != "Warm-up" }} exercises" else "No prescribed resistance workout") },
        trailingContent = { if (d.training) Icon(Icons.Filled.ChevronRight, null) },
        modifier = Modifier.clickable(enabled = d.training) { nav.navigate(Routes.programDay("${p.id}_${d.day}", null)) })
    }
    item { SectionTitle("Progression"); Text(p.progression) }
    item { SectionTitle("Rest"); Text(p.rest) }
    if (p.notes.isNotEmpty()) item { SectionTitle("Notes from the source"); p.notes.forEach { Text("${it.topic}: ${it.text}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp)) } }
    item { TextButton({ runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.sourceUrl))) } }) { Text("Open original source") } }
  }
}

/** One assigned session: original prescriptions exactly as published, optional swaps labelled as not original. */
@Composable fun ProgramDayScreen(app: AppContainer, planId: String, date: String?, nav: NavHostController) {
  val p = app.programs.find(planId.substringBeforeLast('_'))
  val d = p?.days?.firstOrNull { it.day == planId.substringAfterLast('_').toIntOrNull() }
  if (p == null || d == null) return NotFound("Workout not found.")
  LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
    item { date?.let { Muted(LocalDate.parse(it).format(longFmt)) }
      Text(d.session, style = MaterialTheme.typography.headlineSmall); Muted("${p.name} · day ${d.day} of ${p.cycleLength}") }
    if (!d.training) item { Text("Rest day. No prescribed resistance workout.") }
    else {
      item { Button({ nav.navigate(Routes.runProgram(planId)) }, Modifier.fillMaxWidth().height(52.dp)) {
        Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Start workout") } }
      listOf("Warm-up", "Working").forEach { phase -> val rows = d.items.filter { it.phase == phase }
        if (rows.isNotEmpty()) {
          item { SectionTitle(if (phase == "Warm-up") "Warm-up" else "Workout") }
          items(rows) { r -> Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(r.exercise, fontWeight = FontWeight.Bold)
              Text("${r.sets} sets × ${r.reps}", color = MaterialTheme.colorScheme.primary)
              if (r.repsNote.isNotBlank() && r.repsNote != "Per set") Muted(r.repsNote)
              if (r.rest.isNotBlank() && r.rest != "Not specified") Muted("Rest: ${r.rest}")
              if (r.notes.isNotBlank()) Muted(r.notes)
              if (r.substitution.isNotBlank() && !r.substitution.startsWith("Original")) Muted("Optional gym swap, not the original: ${r.substitution}")
            } } }
        } }
      item { Muted("Progression: ${p.progression}") }
    }
  }
}
