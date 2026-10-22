package com.gymclues.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gymclues.app.R
import com.gymclues.app.data.*
import java.time.LocalDate

internal fun menuProgram(assignments: List<ProgramAssignmentEntity>, date: LocalDate = LocalDate.now()) =
  assignmentOn(assignments, date) ?: assignments.filter { !it.deleted && it.startDate > date.toString() && it.endDate.isBlank() }.minByOrNull { it.startDate }

@Composable internal fun AppHeader(app: AppContainer, showBack: Boolean, onBack: () -> Unit, openMenu: () -> Unit, go: (String) -> Unit, modifier: Modifier) {
  val activeGymId by app.gyms.activeGymId.collectAsState()
  val gym by remember(activeGymId) { app.gyms.gymFlow(activeGymId) }.collectAsState(null)
  val cs = MaterialTheme.colorScheme
  Column(modifier.padding(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
      if (showBack) IconButton(onBack, Modifier.size(44.dp)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.size(20.dp))
      }
      Surface({ go("gyms") }, Modifier.weight(1f).heightIn(min = 48.dp).tourTarget("gym"), color = cs.background) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Image(painterResource(R.drawable.gymclues_logo), null, Modifier.size(28.dp))
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("GymClues", style = MaterialTheme.typography.titleMedium, color = cs.primary)
            Text(gym?.name ?: "Pick your gym", maxLines = 1, overflow = TextOverflow.Ellipsis,
              style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
          }
        }
      }
      IconButton(openMenu, Modifier.size(48.dp).tourTarget("logo").semantics { contentDescription = "GymClues menu" }) {
        Icon(Icons.Filled.Menu, null, Modifier.size(24.dp), tint = cs.primary)
      }
    }
    NowPlayingBar(Modifier.fillMaxWidth().heightIn(min = 48.dp).tourTarget("media"))
  }
}

@Composable internal fun AppMenu(app: AppContainer, go: (String) -> Unit, modifier: Modifier = Modifier, selectedRoute: String? = null) {
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val current = menuProgram(assignments)
  val active = current?.let { c -> app.programs.programs.firstOrNull { it.id == c.programId } }
  val entries = listOf(
    Triple("Start A Workout", "programs", null),
    Triple("Completed Sessions", "sessions", null),
    if (active == null) Triple("Pick a Program", "programs", null) else Triple("Active Program", Routes.program(active.id), active.name),
    Triple("Exercises", "muscles", null), Triple("Equipment", "equipment", null), Triple("My Exercises", "myex", null), Triple("Admin", "admin", null))
  val icons = listOf(Icons.Filled.PlayCircleOutline, Icons.Filled.History, Icons.Filled.CalendarMonth,
    Icons.Filled.FitnessCenter, Icons.Filled.SportsGymnastics, Icons.Filled.BookmarkBorder, Icons.Filled.AdminPanelSettings)
  Surface(modifier.width(300.dp).tourTarget("menu"), shape = MaterialTheme.shapes.large,
    shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
    Column(Modifier.padding(vertical = 12.dp)) {
      Text("Training & tools", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.titleSmall)
      entries.forEachIndexed { index, (label, route, sub) ->
        if (index == entries.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
        val selected = selectedRoute?.substringBefore('/') == route.substringBefore('/')
        DropdownMenuItem(modifier = Modifier.fillMaxWidth(), text = { Column { Text(label, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface); sub?.let { Muted(it) } } },
          leadingIcon = { Icon(icons[index], null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary) },
          onClick = { go(route) })
      }
    }
  }
}
