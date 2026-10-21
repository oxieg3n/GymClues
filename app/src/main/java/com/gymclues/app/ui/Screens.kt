package com.gymclues.app.ui
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.R
import com.gymclues.app.data.*
import kotlinx.coroutines.launch

val SPLIT_HELP = mapOf(
  "Full Body" to "Train every major muscle group in one session. Great for beginners.",
  "Push" to "Chest, shoulders, and triceps.", "Pull" to "Back and biceps.", "Legs" to "Quads, hamstrings, glutes, calves.",
  "Upper" to "All upper-body muscles.", "Lower" to "All lower-body muscles and core.",
  "Body-Part Split (Bro Split)" to "One or two muscle groups per day, e.g. Chest Day.")

@Composable fun Home(app: AppContainer, go: (String) -> Unit) {
  val logs by app.workouts.history().collectAsState(emptyList())
  val stats = remember(logs) { workoutStats(logs.map { it.date }) }
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val rest by app.programs.restRules().collectAsState(RestRules.NONE)
  val pStreak = remember(stats, assignments, rest) { programStreak(stats.doneDays, app.programs.programs, assignments, rest, stats.today) }
  val progress = remember(stats, assignments, rest) { programProgress(stats.doneDays, app.programs.programs, assignments, rest, stats.today) }
  BoxWithConstraints(Modifier.fillMaxSize()) {
  Column(Modifier.fillMaxSize()) {
   // Cards fill the screen above Rest Day and scroll if needed.
   Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Box(Modifier.tourTarget("progress")) {
      if (progress != null) ProgramProgressCard(progress!!) { go("programs") }
      else Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TaskAlt, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Program completed", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
              Text("0%", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
          }
          LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth().height(6.dp),
            color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
          TextButton({ go("programs") }) { Text("Pick a program to start tracking") }
        }
      }
    }
    Box(Modifier.tourTarget("streak")) { StreakPanel(stats.copy(streak = pStreak ?: stats.streak), pStreak != null) }
    Box(Modifier.tourTarget("schedule")) { CalendarCard(app, stats, go) }
   }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).tourTarget("rest")) { RestDayButton(app, go, Modifier.fillMaxWidth().height(70.dp)) }
  }
  }
}

@Composable fun ListScreen(title: String, items: List<String>, onAdd: () -> Unit, click: (String) -> Unit) = LazyColumn(Modifier.padding(16.dp)) {
  item { Text(title, style = MaterialTheme.typography.titleLarge) }
  item { TextButton(onAdd, contentPadding = PaddingValues(0.dp)) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text("Add your own exercise") } }
  if (items.isEmpty()) item { Text("No content yet.") }
  items(items) { AppListItem(headlineContent = { Text(it) },
    leadingContent = { IconBadge(Icons.Filled.AccessibilityNew) },
    trailingContent = { Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
    modifier = Modifier.clickable { click(it) }) }
}

@Composable fun ExerciseList(list: List<Exercise>, title: String = "Exercises", click: (String) -> Unit) = LazyColumn(Modifier.padding(16.dp)) {
  item { Text(title, style = MaterialTheme.typography.headlineSmall) }
  if (list.isEmpty()) item { Text("No exercises available with your gym's equipment.") }
  items(list.sortedBy { it.name.lowercase() }) { e -> AppListItem(headlineContent = { Text(e.name) },
    leadingContent = { IconBadge(Icons.Filled.FitnessCenter) },
    trailingContent = { Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
    supportingContent = { Text("${e.difficulty} · ${e.movement} · ${e.primaryMuscles.joinToString()}") },
    modifier = Modifier.clickable { click(e.id) }) }
}

@Composable fun ExerciseDetail(app: AppContainer, e: Exercise, off: Set<String>, nav: NavHostController) {
  val repo = app.catalog
  var busy by remember { mutableStateOf(false) }
  LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    item { Row(verticalAlignment = Alignment.CenterVertically) {
      Text(e.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
      MuscleMapButton(e.muscleMapName.ifBlank { e.name })
    } }
    if (e.choices.isNotEmpty()) {
      item { SectionTitle("Choose a variation") }
      items(e.choices) { choice -> AppListItem(headlineContent = { Text(choice.name) },
        trailingContent = { Icon(Icons.Filled.ChevronRight, null) },
        modifier = Modifier.clickable { nav.navigate(Routes.exercise(choice.exerciseId)) }) }
    }
    if (e.equipmentNotes.isNotBlank()) item { Text(e.equipmentNotes) }
    item { SectionTitle("Equipment"); Muted("Tap for setup and how to use it") }
    item { EquipmentLinks(repo, e) { nav.navigate(Routes.equipment(it, e.id)) } }
    item { e.equipmentIds.mapNotNull(repo::equipment).filter { it.alternateNames.isNotEmpty() }.forEach { q ->
      Muted("${q.name} is also called: ${q.alternateNames.joinToString()}") } }
    if (e.target.isNotBlank()) item { Text("Targets: ${e.target}") }
    if (e.why.isNotBlank()) item { Muted(e.why) }
    item { Text("Works: ${e.primaryMuscles.joinToString()}" + if (e.secondaryMuscles.isNotEmpty()) " (also ${e.secondaryMuscles.joinToString()})" else "") }
    section("Setup", e.setup); section("How to do it", e.steps)
    if (e.breathing.isNotBlank()) item { Text("Breathing: ${e.breathing}") }
    item { ExerciseSafetyGuide(e, repo.trainingGuides) }
    item { AppOutlinedButton({ busy = !busy }) { Text("Machine busy?") } }
    if (busy) { val alts = repo.alternatives(e, off)
      if (alts.isEmpty()) item { Text("No reviewed alternative yet.") }
      items(alts) { a -> AppListItem(headlineContent = { Text(a.name) }, modifier = Modifier.clickable { nav.navigate(Routes.exercise(a.id)) }) } }
  }
}
fun LazyListScope.section(t: String, l: List<String>) { if (l.isEmpty()) return
  item { SectionTitle(t) }
  itemsIndexed(l) { i, s -> InstructionStep(i + 1, s) } }

@Composable fun EquipmentScreen(app: AppContainer, off: Set<String>, nav: NavHostController) {
  val scope = rememberCoroutineScope()
  LazyColumn(Modifier.padding(16.dp)) {
    item { Text("Uncheck anything your gym doesn't have", style = MaterialTheme.typography.titleMedium) }
    item { Muted("Tap equipment for setup diagrams and adjustment instructions.") }
    if (app.catalog.content.equipment.isEmpty()) item { Text("No equipment in the catalog yet.") }
    app.catalog.equipmentGroups.forEach { (type, equipment) ->
    item(key = "type_${type.name}") { SectionTitle(type.title) }
    items(equipment, key = { it.id }) { q -> val on = q.id !in off
      AppListItem(headlineContent = { Text(q.name) },
        modifier = Modifier.clickable { nav.navigate(Routes.equipment(q.id)) },
        leadingContent = { IconBadge(Icons.Filled.FitnessCenter) },
        trailingContent = { Switch(on, { scope.launch { if (it) app.dao.markAvailable(Unavailable(q.id)) else app.dao.markUnavailable(Unavailable(q.id)) } }) }) }
    }
  }
}

