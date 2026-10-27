package com.gymclues.app.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json

const val MAX_SETS = 10
const val MIN_SETS = 1

// ---------------- Workouts list: custom workouts + bundled templates ----------------
@Composable fun Workouts(app: AppContainer, nav: NavHostController) {
  val customs by app.workouts.customWorkouts().collectAsState(emptyList())
  val allItems by app.dao.allWorkoutItems().collectAsState(emptyList())
  val scope = rememberCoroutineScope()
  var confirmDelete by remember { mutableStateOf<WorkoutEntity?>(null) }
  confirmDelete?.let { w -> AlertDialog(onDismissRequest = { confirmDelete = null },
    title = { Text("Delete \"${w.name}\"?") }, text = { Text("Your completed sessions are not affected.") },
    confirmButton = { TextButton({ scope.launch { app.workouts.deleteCustom(w.id) }; confirmDelete = null }) { Text("Delete") } },
    dismissButton = { TextButton({ confirmDelete = null }) { Text("Cancel") } }) }

  LazyColumn(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    item { Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Custom Workouts", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
      AppTonalButton({ nav.navigate(Routes.builder()) }) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text("Create") } } }
    if (customs.isEmpty()) item { Muted("Build your own workout from any exercise in the library.") }
    items(customs, key = { it.id }) { w -> val n = allItems.count { it.workoutId == w.id }
      AppListItem(headlineContent = { Text(w.name) }, supportingContent = { Text("$n exercise${if (n == 1) "" else "s"}") },
        trailingContent = { Row {
          IconButton({ nav.navigate(Routes.builder(w.id)) }) { Icon(Icons.Filled.Edit, "Edit") }
          IconButton({ confirmDelete = w }) { Icon(Icons.Filled.Delete, "Delete") } } },
        modifier = Modifier.clickable { nav.navigate(Routes.runCustom(w.id)) })
    }
    item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
    app.catalog.content.templates.groupBy { it.split }.forEach { (split, ts) ->
      item { Text(split, style = MaterialTheme.typography.titleLarge); SPLIT_HELP[split]?.let { Text(it) } }
      items(ts) { t -> AppListItem(headlineContent = { Text(t.name) }, supportingContent = { Text(t.description) },
        modifier = Modifier.clickable { nav.navigate(Routes.runTemplate(t.id)) }) }
    }
  }
}

// ---------------- Active workout session ----------------
class SetRow { var weight by mutableStateOf(""); var reps by mutableStateOf(""); var done by mutableStateOf(false); var logged by mutableStateOf(false); var saving by mutableStateOf(false)
  var logId by mutableLongStateOf(0); var loggedAt by mutableLongStateOf(0) }

/** One exercise inside today's session. Editing the session never changes the saved workout/template. */
class SessionExercise(val exerciseId: String, val swappedFrom: String?, val reps: String, val restSec: Int, sets: Int, val item: TemplateItem? = null, val tracking: String = "strength", val uid: String = newId()) {
  val cardio = if (tracking in listOf("locomotion", "stairs", "bike", "rower")) CardioDraft(tracking, seconds = item?.durationSec ?: 0) else null
  var cardioError by mutableStateOf(false)
  val sets = mutableStateListOf<SetRow>().apply { repeat(if (cardio != null) 1 else sets.coerceIn(MIN_SETS, MAX_SETS)) { add(SetRow()) } }
}

@Composable fun ActiveWorkout(app: AppContainer, kind: String, planId: String, off: Set<String>, nav: NavHostController, date: String? = null,
  navigate: (String) -> Unit = { nav.navigate(it) }, onDone: () -> Unit) {
  val repo = app.catalog
  val ctx = LocalContext.current
  val prefs = remember { ctx.getSharedPreferences("active_workouts", android.content.Context.MODE_PRIVATE) }
  val draftKey = "${app.session.userId}_${kind}_$planId"
  val savedDraft = remember(draftKey) { prefs.getString(draftKey, null)?.let { runCatching { Json.decodeFromString<ActiveDraft>(it) }.getOrNull() } }
  var plan by remember { mutableStateOf<WorkoutPlan?>(null) }
  var loaded by remember { mutableStateOf(false) }
  val session = remember { mutableStateListOf<SessionExercise>() }
  val sessionId = remember(draftKey) { savedDraft?.sessionId ?: newId() }
  val workoutDate = remember(draftKey) { savedDraft?.workoutDate ?: date ?: java.time.LocalDate.now().toString() }
  var editingLayout by remember { mutableStateOf(false) }
  var previousOrder by remember { mutableStateOf<List<String>>(emptyList()) }
  val gymId by app.gyms.activeGymId.collectAsState()
  LaunchedEffect(kind, planId) {
    if (!loaded) {
      plan = if (kind == "program") app.programs.plan(planId) else app.workouts.plan(kind, planId)
      if (savedDraft != null) session.addAll(savedDraft.exercises.map { it.restore() })
      else plan?.items?.forEach { item ->
        val base = repo.exercise(item.exerciseId)
        if (base == null) { if (item.originalName.isNotBlank()) session += SessionExercise(item.exerciseId, null, item.reps, item.restSec, item.sets, item)
          return@forEach }                                                  // skip ids that no longer exist
        val ex = if (repo.usable(base, off)) base else repo.alternatives(base, off).firstOrNull() ?: base
        session += SessionExercise(ex.id, if (ex.id != base.id) base.id else null, item.reps, item.restSec, item.sets, item, ex.tracking).also { se ->
          if (item.durationSec == 0 && ex.timedCardio) se.cardio?.time = item.reps.takeIf { cardioMinutes(it) != null }.orEmpty()
        }
      }
      loaded = true
    }
  }
  LaunchedEffect(loaded, draftKey) { if (loaded) snapshotFlow {
    val persistedOrder = if (editingLayout) previousOrder.mapNotNull { uid -> session.firstOrNull { it.uid == uid } } + session.filter { it.uid !in previousOrder } else session
    ActiveDraft(sessionId, workoutDate, persistedOrder.map { it.draft() }) }
    .collect { prefs.edit().putString(draftKey, Json.encodeToString(ActiveDraft.serializer(), it)).apply() } }
  val p = plan
  if (!loaded) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
  if (p == null) { NotFound("This workout no longer exists."); return }

  val scope = rememberCoroutineScope()
  val snackbar = remember { SnackbarHostState() }
  var rest by remember { mutableIntStateOf(0) }
  LaunchedEffect(rest) { if (rest > 0) { delay(1000); rest-- } }
  var showDone by remember { mutableStateOf(false) }
  var picking by remember { mutableStateOf(false) }
  DisposableEffect(editingLayout) { app.layoutEditing = editingLayout; onDispose { app.layoutEditing = false } }
  var pickingCardio by remember { mutableStateOf(false) }
  var customCardio by remember { mutableStateOf(false) }
  var cardioName by remember { mutableStateOf("") }
  var cardioTracking by remember { mutableStateOf("locomotion") }
  var cardioCreateError by remember { mutableStateOf<String?>(null) }
  var saveAs by remember { mutableStateOf(false) }

  var finishing by remember { mutableStateOf(false) }
  suspend fun log(se: SessionExercise, idx: Int, row: SetRow): Boolean {
    if (se.cardio == null && se.item?.phase == "Warm-up") return true
    if (row.saving) return false
    val metrics = se.cardio?.metrics()
    if (se.cardio != null && metrics == null) { se.cardioError = true; return false }
    row.saving = true
    return try {
      val entry = SetLog(id = row.logId, date = row.loggedAt.takeIf { it > 0 } ?: now(), exerciseId = se.exerciseId, setNo = idx + 1,
        reps = if (metrics != null) 0 else row.reps.toIntOrNull() ?: 0,
        weight = if (metrics != null || se.tracking == "reps") 0.0 else row.weight.toDoubleOrNull() ?: 0.0,
        sessionId = sessionId, gymId = gymId, cardio = metrics)
      if (row.logId > 0) app.workouts.updateSet(entry) else row.logId = app.workouts.logSet(entry)
      row.loggedAt = entry.date
      row.logged = true; row.done = true; true
    } catch (e: Exception) {
      if (e is kotlinx.coroutines.CancellationException) throw e
      snackbar.showSnackbar("Could not save this entry. Please try again."); false
    } finally { row.saving = false }
  }

  var sharing by remember { mutableStateOf(false) }
  var completionError by remember { mutableStateOf(false) }
  suspend fun saveCompleted(share: Boolean) {
    try {
      app.workouts.completeSession(sessionId, p.name, gymId, workoutDate,
        if (kind == "program") planId else "", if (kind == "program") app.programs.find(planId.substringBeforeLast('_'))?.name.orEmpty() else "",
        if (kind == "program") app.programs.find(planId.substringBeforeLast('_'))?.days?.firstOrNull { it.day == planId.substringAfterLast('_').toIntOrNull() }?.session.orEmpty() else "")
      prefs.edit().remove(draftKey).commit()
      if (share) sharing = true else onDone()
    } catch (e: Exception) {
      if (e is kotlinx.coroutines.CancellationException) throw e
      completionError = true
    }
  }
  if (showDone && !sharing) AlertDialog(onDismissRequest = {}, confirmButton = {},
    title = { Text("Workout Completed, Congratulations!", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
    text = { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
      if (completionError) Text("Could not save the completed workout. Your session is still here; retry.", color = MaterialTheme.colorScheme.error)
      AppButton({ scope.launch { saveCompleted(true) } }, Modifier.fillMaxWidth().heightIn(min = 64.dp), shape = MaterialTheme.shapes.medium) {
        Text("Share Your Milestone?", style = MaterialTheme.typography.titleMedium) }
      TextButton({ scope.launch { saveCompleted(false) } }) { Text("Done", style = MaterialTheme.typography.bodyMedium) }
    } })
  if (showDone && sharing) ShareDialog(app, sessionId) { sharing = false }
  if (picking) ExercisePickerSheet(repo, if (pickingCardio) null else repo.suggestionsFor(session.map { it.exerciseId }, off), session.map { it.exerciseId }.toSet(),
    onDismiss = { picking = false }, onCreate = { if (pickingCardio) { picking = false; customCardio = true } else { picking = false; navigate(Routes.addExercise()) } },
    initialCardioOnly = pickingCardio) { picked ->
    picked.forEach { session += SessionExercise(it.id, null, if (it.timedCardio) "" else "10-12", if (it.timedCardio) 0 else 90, if (it.timedCardio) 1 else 3, tracking = it.tracking) }; picking = false }
  if (customCardio) AlertDialog(onDismissRequest = { customCardio = false }, title = { Text("Add Custom Cardio Exercise") },
    text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      AppTextField(cardioName, { cardioName = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
      listOf("locomotion" to "Walking / running", "bike" to "Cycling / elliptical", "rower" to "Rowing", "stairs" to "Stair climbing").forEach { (type, label) ->
        Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(cardioTracking == type, { cardioTracking = type }); Text(label) } }
      cardioCreateError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    } }, confirmButton = { TextButton({ scope.launch {
      try { val (error, exercise) = app.exercises.createCardio(cardioName, cardioTracking)
        cardioCreateError = error
        if (exercise != null) { session += SessionExercise(exercise.id, null, "", 0, 1, tracking = exercise.tracking); customCardio = false; cardioName = "" }
      } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; cardioCreateError = "Could not save exercise. Try again." }
    } }, enabled = cardioName.isNotBlank()) { Text("Save and add") } },
    dismissButton = { TextButton({ customCardio = false }) { Text("Cancel") } })
  if (saveAs) NameDialog("Save as custom workout", p.name + " (my version)", { saveAs = false }) { name ->
    scope.launch { app.workouts.saveCustom(null, name, session.map { TemplateItem(it.exerciseId, it.sets.size, it.cardio?.metrics()?.minutes?.toString() ?: it.reps, it.restSec) })
      saveAs = false; snackbar.showSnackbar("Saved to Custom Workouts") } }

  val activeScroll = rememberLazyListState()
  Box(Modifier.fillMaxSize()) {
    LazyColumn(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
      state = activeScroll,
      contentPadding = PaddingValues(top = 20.dp, bottom = if (editingLayout) 100.dp else 28.dp)) {
      item { ScreenTitle(p.name, p.description)
        AppOutlinedButton({ pickingCardio = true; picking = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Add Cardio") }
        TextButton({ if (editingLayout) { val byId = session.associateBy { it.uid }; val ordered = previousOrder.mapNotNull(byId::get) + session.filter { it.uid !in previousOrder }
            session.clear(); session.addAll(ordered) } else previousOrder = session.map { it.uid }
          editingLayout = !editingLayout }, Modifier.fillMaxWidth()) { Text(if (editingLayout) "Cancel layout" else "Unlock Layout") }
        if (rest > 0) Text("Rest: ${rest}s", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
      if (session.isEmpty()) item { AppCard { Column(Modifier.padding(16.dp)) {
        Text("No exercises in this workout.", fontWeight = FontWeight.Medium); Muted("Tap \"Add exercise\" below to build today's session.") } } }
      itemsIndexed(session, key = { _, se -> se.uid }) { index, se ->
        val ex = repo.exercise(se.exerciseId)
        val exerciseName = workoutMuscleMapName(se.item?.originalName,
          ex?.name ?: repo.displayName(se.exerciseId), se.swappedFrom != null)
        AppCard { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          val it0 = se.item
          if (it0?.phase == "Warm-up") SectionTitle("Warm-up")
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (editingLayout) LayoutHandle(exerciseName,
              { val at = session.indexOf(se); if (at > 0) session.add(at - 1, session.removeAt(at)) },
              { val at = session.indexOf(se); if (at in 0 until session.lastIndex) session.add(at + 1, session.removeAt(at)) }, activeScroll)
            Text(exerciseName + (se.swappedFrom?.let { " (swap for ${repo.displayName(it)})" } ?: ""),
              fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).clickable(enabled = ex != null) { navigate(Routes.exercise(se.exerciseId)) })
            MuscleMapButton(exerciseName)
            RemoveCircleButton("Remove ${ex?.name ?: repo.displayName(se.exerciseId)}") {
              val removed = session.removeAt(index)
              scope.launch {
                val r = snackbar.showSnackbar("Removed ${ex?.name ?: repo.displayName(se.exerciseId)}", actionLabel = "Undo", duration = SnackbarDuration.Short)
                if (r == SnackbarResult.ActionPerformed) session.add(index.coerceAtMost(session.size), removed)
              }
            }
          }
          Muted(if (se.cardio != null) (if ((it0?.durationSec ?: 0) > 0) "Target: ${it0!!.durationSec}s" else "Cardio session") else if (it0 == null) "Target: ${se.sets.size} sets × ${se.reps} reps · rest ${se.restSec}s"
            else "Prescribed: ${it0.setsText.ifBlank { "${it0.sets}" }} sets × ${it0.reps}" + (if (it0.restText.isNotBlank() && it0.restText != "Not specified") " · rest ${it0.restText}" else ""))
          if (it0 != null && it0.repsNote.isNotBlank() && it0.repsNote != "Per set") Muted(it0.repsNote)
          if (it0 != null && it0.notes.isNotBlank()) Muted(it0.notes)
          if (it0 != null && it0.substitution.isNotBlank() && !it0.substitution.startsWith("Original")) Muted("Optional gym swap, not the original: ${it0.substitution}")
          if (ex != null) EquipmentLinks(repo, ex) { navigate(Routes.equipment(it, ex.id)) }
          ExerciseSafetyButton(exerciseName)
          if (se.cardio != null) {
            val row = se.sets.first()
            Muted("Record actual time and settings. Preset times are targets until recorded.")
            CardioInput(se.cardio, enabled = !row.saving, showErrors = se.cardioError)
            AppTonalButton({ scope.launch { if (log(se, 0, row)) rest = se.restSec } },
              enabled = !row.saving) { Text(if (row.logged) "Save cardio changes" else "Record cardio") }
          } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            SetStepButton(add = false, enabled = se.sets.size > MIN_SETS) { if (se.sets.size > MIN_SETS) se.sets.removeAt(se.sets.lastIndex) }
            Text("  ${se.sets.size} sets  ", style = MaterialTheme.typography.titleSmall)
            SetStepButton(add = true, enabled = se.sets.size < MAX_SETS) { if (se.sets.size < MAX_SETS) se.sets.add(SetRow()) }
          }
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          se.sets.forEachIndexed { n, row ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Text("${n + 1}".padStart(2, '0'), Modifier.width(24.dp).heightIn(min = 48.dp).wrapContentHeight(Alignment.CenterVertically).align(Alignment.Bottom), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
              if (se.tracking != "reps") AppTextField(row.weight, { row.weight = it }, Modifier.weight(1f), label = { Text("lb") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
              AppTextField(row.reps, { row.reps = it }, Modifier.weight(1f), label = { Text("reps") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
              Checkbox(row.done, { c -> if (c) scope.launch { if (log(se, n, row)) rest = se.restSec } else row.done = false },
                modifier = Modifier.align(Alignment.Bottom).semantics { contentDescription = "Complete set ${n + 1} of $exerciseName" }, enabled = !row.saving,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary, checkmarkColor = MaterialTheme.colorScheme.onPrimary))
              if (row.logged) TextButton({ scope.launch { log(se, n, row) } }) { Text("Save edit") }
            }
          }
          }
        } }
      }
      item { AppOutlinedButton({ pickingCardio = false; picking = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
        Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Add exercise") } }
      if (kind != "program") item { TextButton({ saveAs = true }, Modifier.fillMaxWidth(), enabled = session.isNotEmpty()) { Text("Save as custom workout") } }
      item { AppButton({ scope.launch {
        finishing = true
        var valid = true
        session.forEach { se ->
          if (se.cardio != null) {
            if (se.cardio.edited && se.cardio.hasInput && !log(se, 0, se.sets.first())) valid = false
          } else se.sets.forEachIndexed { i, row ->
            if (!row.logged && (row.weight.isNotBlank() || row.reps.isNotBlank()) && !log(se, i, row)) valid = false
          }
        }
        finishing = false
        if (valid) showDone = true else snackbar.showSnackbar("Check the entries that could not be recorded.")
      } }, Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = !finishing) { Text("Finish Workout") } }
    }
    if (editingLayout) SaveLayoutButton({
      if (prefs.edit().putString(draftKey, Json.encodeToString(ActiveDraft.serializer(), ActiveDraft(sessionId, workoutDate, session.map { it.draft() }))).commit()) {
        editingLayout = false; scope.launch { snackbar.showSnackbar("Layout saved") }
      } else scope.launch { snackbar.showSnackbar("Could not save layout. Try again.") }
    },
      Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp))
    SnackbarHost(snackbar, Modifier.align(Alignment.TopCenter))
  }
}

@Composable fun NameDialog(title: String, initial: String, dismiss: () -> Unit, save: (String) -> Unit) {
  var name by remember { mutableStateOf(initial) }
  AlertDialog(onDismissRequest = dismiss, title = { Text(title) },
    text = { AppTextField(name, { name = it }, singleLine = true, label = { Text("Workout name") }) },
    confirmButton = { TextButton({ save(name.trim()) }, enabled = name.isNotBlank()) { Text("Save") } },
    dismissButton = { TextButton(dismiss) { Text("Cancel") } })
}

// ---------------- Custom workout builder ----------------
class BuilderItem(val exerciseId: String, sets: Int, reps: String, rest: Int) {
  val key = newId(); var sets by mutableIntStateOf(sets); var reps by mutableStateOf(reps); var rest by mutableStateOf(rest.toString())
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun WorkoutBuilder(app: AppContainer, editId: String?, off: Set<String>, onCreate: () -> Unit, onSaved: () -> Unit) {
  val repo = app.catalog
  var name by rememberSaveable { mutableStateOf("") }
  val items = remember { mutableStateListOf<BuilderItem>() }
  var loaded by remember { mutableStateOf(editId == null) }
  var picking by remember { mutableStateOf(false) }
  var editingLayout by remember { mutableStateOf(false) }
  var saveError by remember { mutableStateOf<String?>(null) }
  DisposableEffect(editingLayout) { app.layoutEditing = editingLayout; onDispose { app.layoutEditing = false } }
  val scope = rememberCoroutineScope()
  val canSave = loaded && name.isNotBlank() && items.isNotEmpty() && items.all { repo.exercise(it.exerciseId)?.timedCardio != true || cardioMinutes(it.reps) != null }
  fun save() { scope.launch {
    try { app.workouts.saveCustom(editId, name, items.map { TemplateItem(it.exerciseId,
      if (repo.exercise(it.exerciseId)?.timedCardio == true) 1 else it.sets, it.reps.ifBlank { "10" }, it.rest.toIntOrNull() ?: 90) }) }
    catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; saveError = "Could not save workout. Your edits are still here; retry."; return@launch }
    kotlinx.coroutines.withContext(Dispatchers.Main.immediate) { editingLayout = false; onSaved() }
  } }
  LaunchedEffect(editId) { if (editId != null && !loaded) {
    app.workouts.customWorkout(editId)?.let { name = it.name }
    app.workouts.customItems(editId).forEach { items += BuilderItem(it.exerciseId, it.sets, it.reps, it.restSec) }
    loaded = true } }

  if (picking) ExercisePickerSheet(repo, null, items.map { it.exerciseId }.toSet(), { picking = false }, { picking = false; onCreate() }) { picked ->
    picked.forEach { items += BuilderItem(it.id, if (it.timedCardio) 1 else 3, if (it.timedCardio) "20" else "10-12", if (it.timedCardio) 0 else 90) }; picking = false }

  val builderScroll = rememberLazyListState()
  Box(Modifier.fillMaxSize()) {
  LazyColumn(Modifier.padding(horizontal = 20.dp), state = builderScroll, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(top = 20.dp, bottom = if (editingLayout) 100.dp else 20.dp)) {
    item { ScreenTitle(if (editId == null) "Create custom workout" else "Edit custom workout", "Build your session from the exercise library.")
      TextButton({ editingLayout = !editingLayout }, Modifier.fillMaxWidth()) { Text(if (editingLayout) "Cancel layout" else "Unlock Layout") }
      saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
    item { AppTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Workout name") }) }
    if (items.isEmpty()) item { Muted("No exercises yet. Add any exercise from the full library.") }
    itemsIndexed(items, key = { _, it -> it.key }) { i, it ->
      val ex = repo.exercise(it.exerciseId)
      AppCard { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(Modifier.weight(1f)) { Text("${i + 1}. ${ex?.name ?: repo.displayName(it.exerciseId)}", fontWeight = FontWeight.Medium)
            Muted(ex?.primaryMuscles?.joinToString() ?: "Unknown exercise") }
          if (editingLayout) LayoutHandle(ex?.name ?: "exercise",
            { val at = items.indexOf(it); if (at > 0) items.add(at - 1, items.removeAt(at)) },
            { val at = items.indexOf(it); if (at in 0 until items.lastIndex) items.add(at + 1, items.removeAt(at)) }, builderScroll)
          RemoveCircleButton("Remove ${ex?.name ?: "exercise"}") { items.removeAt(i) }
        }
        if (ex?.timedCardio == true) {
          AppTextField(it.reps, { v -> it.reps = v }, Modifier.fillMaxWidth(), label = { Text("Minutes (decimal or m:ss)") }, singleLine = true)
        } else FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SetStepButton(false, it.sets > MIN_SETS) { if (it.sets > MIN_SETS) it.sets-- }
            Text("${it.sets} sets", style = MaterialTheme.typography.titleMedium)
            SetStepButton(true, it.sets < MAX_SETS) { if (it.sets < MAX_SETS) it.sets++ }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(it.reps, { v -> it.reps = v }, Modifier.width(88.dp), singleLine = true, label = { Text("reps") })
            AppTextField(it.rest, { v -> it.rest = v.filter(Char::isDigit) }, Modifier.width(80.dp), singleLine = true, label = { Text("rest s") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
          }
        }
      } }
    }
    item { AppOutlinedButton({ picking = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(6.dp)); Text("Add exercises") } }
    if (!editingLayout) item { AppButton(::save, Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = canSave) { Text("Save workout") } }
  }
  if (editingLayout) SaveLayoutButton(::save, Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp), enabled = canSave)
  }
}
