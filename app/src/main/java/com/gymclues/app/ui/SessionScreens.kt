package com.gymclues.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun dateLabel(t: Long) = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(t))

/** Sets of one session in the order the exercises were done. */
internal fun sessionSets(logs: List<SetLog>, id: String) = logs.filter { it.sessionId == id }.sortedWith(compareBy({ it.id }))

/** Finished workouts as a grid. Tap one to see its sets and share it. */
@Composable fun CompletedSessions(app: AppContainer, nav: NavHostController) {
  val sessions by app.workouts.sessions().collectAsState(emptyList())
  val logs by app.workouts.history().collectAsState(emptyList())
  val bySession = remember(logs) { logs.groupBy { it.sessionId } }
  Column(Modifier.fillMaxSize().padding(20.dp)) {
    ScreenTitle("Completed Sessions", "Your finished workouts and recorded measurements.")
    if (sessions.isEmpty()) {
      Spacer(Modifier.height(12.dp))
      AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Icon(Icons.Filled.History, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
          Text("No completed sessions yet", style = MaterialTheme.typography.titleMedium)
          Muted("Finish a workout and tap Done. It shows up here.")
        }
      }
    } else LazyVerticalGrid(GridCells.Adaptive(240.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)) {
      items(sessions, key = { it.id }) { s ->
        val sets = bySession[s.id].orEmpty()
        AppCard(Modifier.fillMaxWidth().clickable { nav.navigate(Routes.session(s.id)) }) {
          Column(Modifier.padding(14.dp).heightIn(min = 96.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Filled.FitnessCenter, null, Modifier.padding(bottom = 8.dp).size(22.dp), tint = MaterialTheme.colorScheme.primary)
            Text(s.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Muted(dateLabel(s.completedAt))
            val exercises = sets.map { it.exerciseId }.distinct().size
            Muted("$exercises exercise${if (exercises == 1) "" else "s"}, ${sets.size} ${if (sets.size == 1) "entry" else "entries"}")
          }
        }
      }
    }
  }
}

/** One finished workout: every set with weight and reps, plus share, edit and delete. */
@Composable fun SessionDetail(app: AppContainer, id: String, nav: NavHostController) {
  val sessions by app.workouts.sessions().collectAsState(emptyList())
  val logs by app.workouts.history().collectAsState(emptyList())
  val scope = rememberCoroutineScope()
  var sharing by remember { mutableStateOf(false) }
  var editing by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }
  var deleted by remember { mutableStateOf(false) }
  val s = sessions.firstOrNull { it.id == id }
  if (s == null) { if (!deleted) NotFound("This session no longer exists."); return }
  val sets = remember(logs, id) { sessionSets(logs, id) }
  var name by remember(editing) { mutableStateOf(s.name) }
  val drafts = remember(editing) { mutableStateMapOf<Long, Pair<String, String>>() }
  val cardioDrafts = remember(editing) { sets.filter { it.cardio != null }.associate { st ->
    st.id to CardioDraft(app.catalog.exercise(st.exerciseId)?.tracking ?: "bike", st.cardio)
  } }
  LazyColumn(Modifier.padding(horizontal = 20.dp), contentPadding = PaddingValues(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item {
      if (editing) AppTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Workout name") }, singleLine = true)
      else { Text(s.name, style = MaterialTheme.typography.headlineSmall); Muted(dateLabel(s.completedAt)) }
    }
    item {
      if (editing) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppButton({ scope.launch {
          app.workouts.renameSession(id, name)
          sets.forEach { st ->
            cardioDrafts[st.id]?.let { app.workouts.updateSet(st.copy(cardio = it.metrics()!!)) }
            if (st.cardio == null) drafts[st.id]?.let { (w, r) ->
            app.workouts.updateSet(st.copy(weight = w.toDoubleOrNull() ?: st.weight, reps = r.toIntOrNull() ?: st.reps)) } }
          editing = false } }, Modifier.weight(1f).heightIn(min = 52.dp), enabled = cardioDrafts.values.all { it.metrics() != null }) { Text("Save changes") }
        AppOutlinedButton({ editing = false }, Modifier.weight(1f).heightIn(min = 52.dp)) { Text("Cancel") }
      } else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppButton({ sharing = true }, Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = MaterialTheme.shapes.medium) { Text("Share") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AppOutlinedButton({ editing = true }, Modifier.weight(1f)) { Text("Edit") }
          AppOutlinedButton({ confirmDelete = true }, Modifier.weight(1f)) { Text("Delete") }
        }
      }
    }
    items(sets.groupBy { it.exerciseId }.entries.toList(), key = { it.key }) { (ex, exSets) ->
      AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(app.catalog.displayName(ex), fontWeight = FontWeight.Medium)
        exSets.forEachIndexed { i, set ->
          if (!editing) Text("${if (set.cardio != null) "Entry" else "Set"} ${i + 1}: ${setLabel(set)}", style = MaterialTheme.typography.bodyMedium)
          else if (set.cardio != null) {
            CardioInput(cardioDrafts.getValue(set.id), showErrors = true)
            TextButton({ scope.launch { app.workouts.deleteSet(set.id) } }) { Text("Delete cardio entry ${i + 1}") }
          } else {
            val (w, r) = drafts[set.id] ?: (setLabel1(set.weight) to set.reps.toString())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text("Set ${i + 1}", Modifier.width(48.dp))
              if (app.catalog.exercise(ex)?.tracking != "reps") AppTextField(w, { drafts[set.id] = it to r }, Modifier.weight(1f), label = { Text(set.units) }, singleLine = true)
              AppTextField(r, { drafts[set.id] = w to it }, Modifier.weight(1f), label = { Text("Reps") }, singleLine = true)
              IconButton({ scope.launch { app.workouts.deleteSet(set.id) } }) { Icon(Icons.Filled.Delete, "Delete set ${i + 1}") }
            }
          }
        }
      } }
    }
  }
  if (sharing) ShareDialog(app, id) { sharing = false }
  if (confirmDelete) AlertDialog({ confirmDelete = false },
    title = { Text("Delete \"${s.name}\"?") }, text = { Text("Its sets are removed and streaks and totals update.") },
    confirmButton = { TextButton({ confirmDelete = false; deleted = true; scope.launch { app.workouts.deleteSession(id); nav.popBackStack() } }) { Text("Delete") } },
    dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancel") } })
}

private fun setLabel1(w: Double) = if (w % 1.0 == 0.0) w.toLong().toString() else w.toString()
