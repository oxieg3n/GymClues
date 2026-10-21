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
  Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      if (showBack) FilledTonalIconButton(onBack, Modifier.size(44.dp),
        shape = MaterialTheme.shapes.small,
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = cs.surfaceContainerHigh, contentColor = cs.onSurface)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", Modifier.size(20.dp)) }
      Surface({ go("gyms") }, Modifier.weight(1f).fillMaxHeight().tourTarget("gym"), shape = MaterialTheme.shapes.small,
        color = cs.surfaceContainerLow, border = BorderStroke(1.dp, cs.outlineVariant)) {
        Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Place, null, Modifier.size(18.dp), tint = cs.primary)
          Spacer(Modifier.width(6.dp))
          Text(gym?.name ?: "Pick your gym", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
        }
      }
      Surface(openMenu, Modifier.fillMaxHeight().tourTarget("logo").semantics { contentDescription = "GymClues menu" },
        shape = MaterialTheme.shapes.small, color = cs.surfaceContainer, border = BorderStroke(1.dp, cs.outlineVariant)) {
        Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Image(painterResource(R.drawable.gymclues_logo), null, Modifier.size(26.dp))
          Text("GymClues", style = MaterialTheme.typography.titleMedium, maxLines = 1, softWrap = false)
          Icon(Icons.Filled.ExpandMore, null, Modifier.size(18.dp), tint = cs.primary)
        }
      }
    }
    NowPlayingBar(Modifier.weight(1f).tourTarget("media"))
  }
}

@Composable internal fun AppMenu(app: AppContainer, go: (String) -> Unit, modifier: Modifier = Modifier) {
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val current = menuProgram(assignments)
  val active = current?.let { c -> app.programs.programs.firstOrNull { it.id == c.programId } }
  val entries = listOf(
    Triple("Start A Workout", "styles", null),
    Triple("Completed Sessions", "sessions", null),
    if (active == null) Triple("Pick a Program", "programs", null) else Triple("Active Program", Routes.program(active.id), active.name),
    Triple("Exercises", "muscles", null), Triple("Equipment", "equipment", null), Triple("My Exercises", "myex", null), Triple("Admin", "admin", null))
  val icons = listOf(Icons.Filled.PlayCircleOutline, Icons.Filled.History, Icons.Filled.CalendarMonth,
    Icons.Filled.FitnessCenter, Icons.Filled.SportsGymnastics, Icons.Filled.BookmarkBorder, Icons.Filled.AdminPanelSettings)
  Surface(modifier.width(280.dp).tourTarget("menu"), shape = MaterialTheme.shapes.large,
    shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surfaceContainerHigh,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
    Column(Modifier.padding(vertical = 8.dp)) {
      entries.forEachIndexed { index, (label, route, sub) ->
        if (index == entries.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
        DropdownMenuItem(text = { Column { Text(label, style = MaterialTheme.typography.labelLarge); sub?.let { Muted(it) } } },
          leadingIcon = { Icon(icons[index], null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
          onClick = { go(route) })
      }
    }
  }
}
