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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
  val streak = remember(stats, assignments, rest) { programStreak(stats.doneDays, app.programs.programs, assignments, rest, stats.today) }
  val progress = remember(stats, assignments, rest) { programProgress(stats.doneDays, app.programs.programs, assignments, rest, stats.today) }
  val today = scheduledDay(app.programs.programs, assignments, stats.today, rest)
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val wide = maxWidth >= 700.dp
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      ScreenTitle("Today", stats.today.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")))
      if (wide) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
          Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TodayTraining(today, rest.isRest(stats.today), stats.today in stats.doneDays, go)
            com.gymclues.app.ui.nutrition.HomeNutrition(app) { go("nutrition") }
            Box(Modifier.tourTarget("schedule")) { CalendarCard(app, stats, go) }
          }
          Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.tourTarget("streak")) { StreakPanel(stats.copy(streak = streak ?: stats.streak), streak != null) }
            Box(Modifier.tourTarget("progress")) { progress?.let { ProgramProgressCard(it) { go("programs") } }
              ?: ProgramChoice() }
            Box(Modifier.tourTarget("rest")) { RestDayButton(app, go, Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
          }
        }
      } else {
        TodayTraining(today, rest.isRest(stats.today), stats.today in stats.doneDays, go)
        com.gymclues.app.ui.nutrition.HomeNutrition(app) { go("nutrition") }
        Box(Modifier.tourTarget("streak")) { StreakPanel(stats.copy(streak = streak ?: stats.streak), streak != null) }
        Box(Modifier.tourTarget("progress")) { progress?.let { ProgramProgressCard(it) { go("programs") } } ?: ProgramChoice() }
        Box(Modifier.tourTarget("schedule")) { CalendarCard(app, stats, go) }
        Box(Modifier.tourTarget("rest")) { RestDayButton(app, go, Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
      }
    }
  }
}

@Composable private fun ProgramChoice() {
  val border = informationBorder()
  Surface(shape = if (border != null) MaterialTheme.shapes.medium else RectangleShape,
    color = if (border != null) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent, border = border) {
  Column(Modifier.fillMaxWidth().padding(if (border != null) PaddingValues(20.dp) else PaddingValues(vertical = 4.dp)), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text("Program progress", style = MaterialTheme.typography.titleMedium)
    Muted("No program assigned. Choose a routine to build your training schedule.")
  }
  }
}

@Composable private fun TodayTraining(a: AssignedDay?, restDay: Boolean, done: Boolean, go: (String) -> Unit) {
  val training = a != null && a.day.training && !a.userRest
  AppCard(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Today's training", style = MaterialTheme.typography.titleMedium)
        if (done) Text("Logged today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
      }
      Text(when { training -> a!!.day.session; restDay || a?.userRest == true || a != null -> "Recovery day"; else -> "Your next session starts here" },
        style = MaterialTheme.typography.headlineSmall)
      if (a != null) Muted(a.program.name)
      when {
        training -> {
          val rows = a!!.day.items.filter { it.phase == "Working" }
          val seconds = a.day.items.sumOf { it.durationSec }
          Text(if (seconds > 0) "${a.day.items.size} timed blocks \u00b7 ${seconds / 60} min planned"
            else "${rows.size} exercises \u00b7 ${rows.sumOf { it.setCount }} working sets", style = MaterialTheme.typography.bodyMedium)
          AppButton({ go(Routes.runProgram(planId(a))) }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
            Icon(Icons.Filled.PlayArrow, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Start today's workout")
          }
          TextButton({ go(Routes.programDay(planId(a), a.date.toString())) }, Modifier.fillMaxWidth()) { Text("Preview session") }
        }
        a != null || restDay -> {
          Muted(if (a?.userRest == true) "Your program shifts back so you keep every session." else "Keep today easy. Your next training session is in the schedule.")
          AppOutlinedButton({ go("week") }, Modifier.fillMaxWidth()) { Text("View schedule") }
        }
        else -> {
          Muted("Follow a structured program, or build a workout from your exercise library.")
          AppButton({ go("programs") }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Choose a program") }
          TextButton({ go(Routes.builder()) }, Modifier.fillMaxWidth()) { Text("Create a custom workout") }
        }
      }
    }
  }
}

@Composable fun ListScreen(title: String, items: List<String>, onAdd: () -> Unit, click: (String) -> Unit) = LazyColumn(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
  item { ScreenTitle(title) }
  item { TextButton(onAdd, contentPadding = PaddingValues(0.dp)) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text("Add your own exercise") } }
  if (items.isEmpty()) item { Text("No content yet.") }
  items(items) { AppListItem(headlineContent = { Text(it) },
    leadingContent = { IconBadge(Icons.Filled.AccessibilityNew) },
    trailingContent = { Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
    modifier = Modifier.clickable { click(it) }) }
}

@Composable fun ExerciseList(list: List<Exercise>, title: String = "Exercises", click: (String) -> Unit) = LazyColumn(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
  item { ScreenTitle(title, "${list.size} exercises") }
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
  LazyColumn(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
  LazyColumn(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
    item { ScreenTitle("Equipment", "${app.catalog.content.equipment.size} setup guides. Uncheck anything your gym doesn't have.") }
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

