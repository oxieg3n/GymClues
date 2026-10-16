package com.gymclues.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
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
  Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
    Text("Completed Sessions", style = MaterialTheme.typography.headlineSmall)
    if (sessions.isEmpty()) {
      Spacer(Modifier.height(12.dp))
      Text("No completed sessions yet", style = MaterialTheme.typography.titleLarge)
      Muted("Finish a workout and tap Done. It shows up here.")
    } else LazyVerticalGrid(GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)) {
      items(sessions, key = { it.id }) { s ->
        val sets = bySession[s.id].orEmpty()
        Card(Modifier.fillMaxWidth().clickable { nav.navigate(Routes.session(s.id)) }) {
          Column(Modifier.padding(14.dp).heightIn(min = 96.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(s.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Muted(dateLabel(s.completedAt))
            Muted("${sets.map { it.exerciseId }.distinct().size} exercises, ${sets.size} sets")
          }
        }
      }
    }
  }
}

/** One finished workout: every set with weight and reps, plus the share button. */
@Composable fun SessionDetail(app: AppContainer, id: String, nav: NavHostController) {
  val sessions by app.workouts.sessions().collectAsState(emptyList())
  val logs by app.workouts.history().collectAsState(emptyList())
  var sharing by remember { mutableStateOf(false) }
  val s = sessions.firstOrNull { it.id == id }
  if (s == null) { NotFound("This session no longer exists."); return }
  val byExercise = remember(logs, id) { sessionSets(logs, id).groupBy { it.exerciseId } }
  LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item { Text(s.name, style = MaterialTheme.typography.headlineSmall); Muted(dateLabel(s.completedAt)) }
    item { Button({ sharing = true }, Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium) { Text("Share") } }
    items(byExercise.entries.toList(), key = { it.key }) { (ex, sets) ->
      Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(app.catalog.displayName(ex), fontWeight = FontWeight.Bold)
        sets.forEachIndexed { i, set -> Text("Set ${i + 1}: ${setLabel(set)}", style = MaterialTheme.typography.bodyMedium) }
      } }
    }
  }
  if (sharing) ShareDialog(app, id) { sharing = false }
}
