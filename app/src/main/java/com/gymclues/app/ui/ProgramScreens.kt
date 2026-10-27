package com.gymclues.app.ui
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
import com.gymclues.app.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

private val longFmt = java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d")
private fun current(list: List<ProgramAssignmentEntity>): ProgramAssignmentEntity? { val t = LocalDate.now().toString()
  return list.filter { !it.deleted && (it.endDate.isBlank() || it.endDate > t) }.maxByOrNull { it.startDate } }

private val TYPE_INFO = mapOf(
  "HIIT" to ("HIIT" to "High-intensity intervals with planned recovery, warm-ups and cool-downs."),
  "BRO" to ("Bro split" to "One or two muscle groups per day, once a week."),
  "PPL" to ("Push / Pull / Legs" to "Pushing, pulling and leg days, usually six days a week."),
  "Upper/Lower" to ("Upper / Lower" to "Alternate upper-body and lower-body days, four days a week."),
  "Full Body" to ("Full body" to "Train everything each session, three days a week."),
  "Arnold" to ("Arnold split" to "Chest and back, shoulders and arms, legs, repeated."),
  "Torso/Limbs" to ("Torso / Limbs" to "Torso days and arm-and-leg days, four days a week."),
  "Push/Pull + Legs Hybrid" to ("PPL + Upper/Lower hybrid" to "A mix of push/pull/legs and upper/lower days."))
private fun typeName(split: String) = TYPE_INFO[split]?.first ?: split

/** Step 1: choose a program type. */
@Composable fun ProgramTypesScreen(app: AppContainer, nav: NavHostController) {
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val cur = current(assignments); val scope = rememberCoroutineScope()
  val groups = app.programs.programs.groupBy { it.split }
  LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
    item { ScreenTitle("Start a Workout", "${app.programs.programs.size} programs \u00b7 ${groups.size} training styles") }
    item {
      AppButton({ nav.navigate(Routes.builder()) }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Custom Workout")
      }
      TextButton({ nav.navigate("styles") }, Modifier.fillMaxWidth()) { Text("Saved workouts") }
    }
    items(groups.keys.toList(), key = { it }) { split ->
      val list = groups.getValue(split); val mine = list.any { it.id == cur?.programId }
      Surface({ nav.navigate(Routes.programType(split)) }, shape = MaterialTheme.shapes.medium,
        color = if (mine) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer, border = informationBorder()) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
          Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(typeName(split), style = MaterialTheme.typography.titleMedium)
            Muted(TYPE_INFO[split]?.second.orEmpty())
            Text("${list.size} programs" + if (mine) " \u00b7 Assigned" else "", style = MaterialTheme.typography.bodySmall,
              color = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }
    if (cur != null) item { TextButton({ scope.launch { app.programs.stop() } }, Modifier.fillMaxWidth()) { Text("Stop current program") } }
  }
}

/** Step 2: the programs of one type. */
@OptIn(ExperimentalLayoutApi::class)
@Composable fun ProgramsScreen(app: AppContainer, nav: NavHostController, split: String) {
  val assignments by app.programs.assignments().collectAsState(initial = null)
  val cur = current(assignments.orEmpty())
  val scope = rememberCoroutineScope()
  var assigning by remember { mutableStateOf<WorkoutProgram?>(null) }
  assigning?.let { AssignDialog(it, { assigning = null }) { start -> scope.launch { app.programs.assign(it.id, start) }; assigning = null } }
  LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
    item { ScreenTitle(typeName(split), "Choose a routine. Its sessions become your training calendar.") }
    items(app.programs.programs.filter { it.split == split }, key = { it.id }) { p ->
      val mine = cur?.programId == p.id
      val assigned = isProgramAssigned(assignments.orEmpty(), p.id)
      Surface(shape = MaterialTheme.shapes.medium, color = if (mine) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer, border = informationBorder()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(p.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
          }
          Muted(p.author)
          Muted("${p.level} \u00b7 ${p.duration}")
          if (mine) Text(if (cur!!.startDate > LocalDate.now().toString()) "Assigned. Starts ${LocalDate.parse(cur.startDate).format(longFmt)}" else "Your current program",
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
          Text(p.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
          Muted("Cycle: ${p.schedule}")
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppOutlinedButton({ nav.navigate(Routes.program(p.id)) }, Modifier.weight(1f)) { Text("Details") }
            AppTonalButton({ assigning = p }, Modifier.weight(1f), enabled = assignments != null && !assigned) { Text(if (assigned) "Already Assigned" else "Assign to me") }
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
  val assignments by app.programs.assignments().collectAsState(initial = null)
  val assigned = isProgramAssigned(assignments.orEmpty(), p.id)
  val ctx = LocalContext.current
  val scope = rememberCoroutineScope()
  var assigning by remember { mutableStateOf(false) }
  if (assigning) AssignDialog(p, { assigning = false }) { start -> scope.launch { app.programs.assign(p.id, start) }; assigning = false }
  LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
    item { Text(p.name, style = MaterialTheme.typography.headlineSmall); Muted("${p.author} · ${p.level} · ${p.duration}") }
    item { Text(p.description) }
    item { AppButton({ assigning = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = assignments != null && !assigned) { Text(if (assigned) "Already Assigned" else "Assign to me") } }
    item { SectionTitle("Cycle (${p.cycleLength} days)") }
    items(p.days, key = { it.day }) { d ->
      AppListItem(headlineContent = { Text("Day ${d.day}: ${d.session}") },
        supportingContent = { Text(if (d.training) if (p.split == "HIIT") "${d.items.size} timed blocks" else "${d.items.count { it.phase != "Warm-up" }} exercises" else "No prescribed workout") },
        trailingContent = { if (d.training) Icon(Icons.Filled.ChevronRight, null) },
        modifier = Modifier.clickable(enabled = d.training) { nav.navigate(Routes.programDay("${p.id}_${d.day}", null)) })
    }
    item { SectionTitle("Progression"); Text(p.progression) }
    item { SectionTitle("Rest"); Text(p.rest) }
    if (p.notes.isNotEmpty()) item { SectionTitle("Program notes"); p.notes.forEach { Text("${it.topic}: ${it.text}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp)) } }
    item { TextButton({ runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.sourceUrl))) } }) { Text("Open original source") } }
  }
}

/** One assigned session: original prescriptions exactly as published, optional swaps labelled as not original. */
@Composable fun ProgramDayScreen(app: AppContainer, planId: String, date: String?, nav: NavHostController) {
  val p = app.programs.find(planId.substringBeforeLast('_'))
  val d = p?.days?.firstOrNull { it.day == planId.substringAfterLast('_').toIntOrNull() }
  if (p == null || d == null) return NotFound("Workout not found.")
  val sessions by app.workouts.sessions().collectAsState(emptyList())
  val assignedDate = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
  val completed = assignedDate?.let { assignedCompleted(AssignedDay(p, d, it), sessions) } == true
  LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
    item { date?.let { Muted(LocalDate.parse(it).format(longFmt)) }
      Text(d.session, style = MaterialTheme.typography.headlineSmall); Muted("${p.name} · day ${d.day} of ${p.cycleLength}") }
    if (!d.training) item { Text("Rest day. No prescribed workout.") }
    else {
      item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppButton({ nav.navigate(Routes.runProgram(planId, date)) }, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = !completed) {
          Icon(if (completed) Icons.Filled.Check else Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text(if (completed) "Completed" else "Start Workout") }
        if (completed) AppOutlinedButton({ nav.navigate("programs") }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Start a 2nd Workout") }
      } }
      listOf("Warm-up", "Working", "Cool-down").forEach { phase -> val rows = d.items.filter { it.phase == phase }
        if (rows.isNotEmpty()) {
          item { SectionTitle(if (phase == "Working") "Workout" else phase) }
          items(rows) { r -> Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer, border = informationBorder()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.exercise, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                MuscleMapButton(r.exercise)
              }
              Text(if (r.durationSec > 0) "${r.durationSec}s" else "${r.sets} sets \u00d7 ${r.reps}", color = MaterialTheme.colorScheme.primary)
              if (r.repsNote.isNotBlank() && r.repsNote != "Per set") Muted(r.repsNote)
              if (r.rest.isNotBlank() && r.rest != "Not specified") Muted("Rest: ${r.rest}")
              if (r.notes.isNotBlank()) Muted(r.notes)
              if (r.substitution.isNotBlank() && !r.substitution.startsWith("Original")) Muted("Optional gym swap, not the original: ${r.substitution}")
              app.catalog.byName(r.exercise)?.let { exercise ->
                TextButton({ nav.navigate(Routes.exercise(exercise.id)) }) { Text("Exercise guide") }
              }
              ExerciseSafetyButton(r.exercise)
            } } }
        } }
      item { Muted("Progression: ${p.progression}") }
    }
  }
}
