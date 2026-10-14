package com.gymguide.app.ui
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymguide.app.data.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

private val dayFmt = java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d")
fun planId(a: AssignedDay) = "${a.program.id}_${a.day.day}"

/** Home calendar: Today and Tomorrow with their assigned sessions, plus Week/Month pages. */
@Composable fun CalendarCard(app: AppContainer, stats: WorkoutStats, go: (String) -> Unit) {
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val cs = MaterialTheme.colorScheme
  Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = cs.surfaceContainer, border = BorderStroke(1.dp, cs.outlineVariant)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Schedule", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        FilledTonalButton({ go("week") }, contentPadding = PaddingValues(horizontal = 14.dp)) { Text("Week") }
        Spacer(Modifier.width(8.dp))
        FilledTonalButton({ go("month") }, contentPadding = PaddingValues(horizontal = 14.dp)) { Text("Month") }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Today" to stats.today, "Tomorrow" to stats.today.plusDays(1)).forEach { (label, date) ->
          DayCard(app, label, date, scheduledDay(app.programs.programs, assignments, date), date in stats.doneDays, go, Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable private fun DayCard(app: AppContainer, label: String, date: LocalDate, a: AssignedDay?, done: Boolean, go: (String) -> Unit, modifier: Modifier) {
  val cs = MaterialTheme.colorScheme
  Surface(modifier.heightIn(min = 148.dp), shape = MaterialTheme.shapes.medium, color = cs.surfaceContainerHigh,
    border = if (label == "Today") BorderStroke(1.5.dp, cs.primary) else null) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, color = if (label == "Today") cs.primary else cs.onSurface, modifier = Modifier.weight(1f))
        if (done) Surface(shape = CircleShape, color = cs.primary, modifier = Modifier.size(24.dp).semantics { contentDescription = "Worked out" }) {
          Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, null, Modifier.size(16.dp), tint = cs.onPrimary) } }
      }
      Muted(date.format(dayFmt))
      Spacer(Modifier.weight(1f))
      when {
        a == null -> { Text("No program", style = MaterialTheme.typography.bodyMedium)
          TextButton({ go("programs") }, contentPadding = PaddingValues(0.dp)) { Text("Choose a program") } }
        !a.day.training -> { Text("Rest day", style = MaterialTheme.typography.titleMedium); Muted(a.program.name) }
        else -> SessionLink(a, go)
      }
    }
  }
}

/** Tappable program name for a day; opens that day's workout page. */
@Composable fun SessionLink(a: AssignedDay, go: (String) -> Unit) {
  val cs = MaterialTheme.colorScheme
  Surface(onClick = { go(Routes.programDay(planId(a), a.date.toString())) }, shape = MaterialTheme.shapes.small, color = cs.primary.copy(alpha = 0.14f),
    border = BorderStroke(1.dp, cs.primary.copy(alpha = 0.5f))) {
    Column(Modifier.fillMaxWidth().padding(10.dp)) {
      Text(a.day.session, style = MaterialTheme.typography.titleMedium, color = cs.primary, maxLines = 2, overflow = TextOverflow.Ellipsis)
      Text(a.program.name, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable private fun useCalendarData(app: AppContainer): Pair<Set<LocalDate>, List<ProgramAssignmentEntity>> {
  val logs by app.workouts.history().collectAsState(emptyList())
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val stats = remember(logs) { workoutStats(logs.map { it.date }) }
  return stats.doneDays to assignments
}

@Composable fun WeekCalendar(app: AppContainer, go: (String) -> Unit) {
  val (done, assignments) = useCalendarData(app)
  var offset by rememberSaveable { mutableIntStateOf(0) }
  val today = LocalDate.now()
  val start = today.with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek)).plusWeeks(offset.toLong())
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton({ offset-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous week") }
      Text("${start.format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))} - ${start.plusDays(6).format(java.time.format.DateTimeFormatter.ofPattern("MMM d"))}",
        style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
      IconButton({ offset++ }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next week") }
    }
    (0L..6L).map { start.plusDays(it) }.forEach { d ->
      val a = scheduledDay(app.programs.programs, assignments, d)
      val cs = MaterialTheme.colorScheme
      Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = cs.surfaceContainer,
        border = if (d == today) BorderStroke(1.5.dp, cs.primary) else null) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          Column(Modifier.width(56.dp)) {
            Text(d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.labelLarge,
              color = if (d == today) cs.primary else cs.onSurfaceVariant)
            Text("${d.dayOfMonth}", style = MaterialTheme.typography.headlineSmall)
          }
          Box(Modifier.weight(1f)) {
            when {
              a == null -> Muted(if (assignments.isEmpty()) "No program assigned" else "Outside program dates")
              !a.day.training -> Muted("Rest day")
              else -> SessionLink(a, go)
            }
          }
          if (d in done) Icon(Icons.Filled.CheckCircle, "Worked out", tint = cs.primary) else Spacer(Modifier.size(24.dp))
        }
      }
    }
  }
}

@Composable fun MonthCalendar(app: AppContainer, go: (String) -> Unit) {
  val (done, assignments) = useCalendarData(app)
  var offset by rememberSaveable { mutableIntStateOf(0) }
  var picked by rememberSaveable { mutableStateOf<String?>(null) }
  val today = LocalDate.now()
  val month = YearMonth.from(today).plusMonths(offset.toLong())
  val first = WeekFields.of(Locale.getDefault()).firstDayOfWeek
  val lead = ((month.atDay(1).dayOfWeek.value - first.value) + 7) % 7
  val cells: List<LocalDate?> = List(lead) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
  val cs = MaterialTheme.colorScheme
  Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton({ offset--; picked = null }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month") }
      Text("${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}", style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
      IconButton({ offset++; picked = null }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month") }
    }
    Row { (0..6).map { first.plus(it.toLong()) }.forEach { Text(it.getDisplayName(TextStyle.NARROW, Locale.getDefault()), Modifier.weight(1f),
      textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant) } }
    cells.chunked(7).forEach { week ->
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (week + List(7 - week.size) { null }).forEach { d ->
          if (d == null) { Spacer(Modifier.weight(1f).height(64.dp)); return@forEach }
          val a = scheduledDay(app.programs.programs, assignments, d)
          val sel = picked == d.toString()
          Surface(onClick = { picked = d.toString() }, modifier = Modifier.weight(1f).height(64.dp), shape = MaterialTheme.shapes.small,
            color = if (sel) cs.primary.copy(alpha = 0.2f) else cs.surfaceContainer,
            border = when { sel -> BorderStroke(1.5.dp, cs.primary); d == today -> BorderStroke(1.dp, cs.primary); else -> null }) {
            Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelLarge, color = if (d == today) cs.primary else cs.onSurface)
                if (d in done) Icon(Icons.Filled.Check, "Worked out", Modifier.size(14.dp).padding(start = 2.dp), tint = cs.primary)
              }
              if (a != null) Text(if (a.day.training) a.day.session else "Rest", fontSize = 10.sp, lineHeight = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = if (a.day.training) cs.primary else cs.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
          }
        }
      }
    }
    val sel = picked?.let(LocalDate::parse)
    if (sel != null) {
      val a = scheduledDay(app.programs.programs, assignments, sel)
      Surface(Modifier.fillMaxWidth().padding(top = 8.dp), shape = MaterialTheme.shapes.medium, color = cs.surfaceContainerHigh) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(sel.format(dayFmt) + if (sel in done) "  (worked out)" else "", style = MaterialTheme.typography.titleMedium)
          when {
            a == null -> Muted(if (assignments.isEmpty()) "No program assigned." else "Outside program dates.")
            !a.day.training -> Muted("Rest day (${a.program.name})")
            else -> SessionLink(a, go)
          }
        }
      }
    }
  }
}
