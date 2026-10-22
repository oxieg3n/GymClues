package com.gymclues.app.ui
import com.gymclues.app.data.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** A "workout" = a calendar day with at least one logged set. Streak = consecutive days with a workout. */
data class WorkoutStats(val week: Int, val month: Int, val year: Int, val streak: Int,
                        val doneDays: Set<LocalDate>, val today: LocalDate, val streakDays: Set<LocalDate> = emptySet())

fun workoutStats(logDates: List<Long>, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(),
                 firstDay: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek): WorkoutStats {
  val days = logDates.mapTo(HashSet()) { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
  val weekStart = today.with(TemporalAdjusters.previousOrSame(firstDay))
  // Today not logged yet does not break the streak; it only breaks once a full day has passed without a workout.
  var d = if (today in days) today else today.minusDays(1)
  var streak = 0; val streakDays = HashSet<LocalDate>()
  while (d in days) { streak++; streakDays.add(d); d = d.minusDays(1) }
  return WorkoutStats(
    week = days.count { !it.isBefore(weekStart) && !it.isAfter(today) },
    month = days.count { it.year == today.year && it.month == today.month },
    year = days.count { it.year == today.year },
    streak = streak, doneDays = days, today = today, streakDays = streakDays)
}

/** [program] = the streak counts scheduled training days, with rest days excluded. */
@Composable fun StreakPanel(s: WorkoutStats, program: Boolean = false, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  BoxWithConstraints(modifier.fillMaxWidth()) {
  val compact = maxWidth < 340.dp
  Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text("Training rhythm", style = MaterialTheme.typography.titleMedium)
      Text("Workout days", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Tally("Day streak", s.streak, Modifier.weight(1f))
      Tally("This week", s.week, Modifier.weight(1f))
      if (!compact) {
        Tally("This month", s.month, Modifier.weight(1f))
        Tally("This year", s.year, Modifier.weight(1f))
      }
    }
    if (compact) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Tally("This month", s.month, Modifier.weight(1f))
      Tally("This year", s.year, Modifier.weight(1f))
    }
    if (program) Muted("Streak follows scheduled training; rest days keep it intact.")
  }
  }
}

@Composable private fun Tally(label: String, n: Int, modifier: Modifier) = Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
  Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  Text("$n", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
}

@Composable fun ProgramProgressCard(p: ProgramProgress, onNewProgram: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  AppCard(Modifier.fillMaxWidth()) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(if (p.complete) "Program complete" else "Program progress", style = MaterialTheme.typography.titleMedium)
        Text("${p.pct}%", style = MaterialTheme.typography.titleLarge, color = if (p.pct > 0) Green else cs.onSurface)
      }
      LinearProgressIndicator(progress = { p.pct / 100f }, Modifier.fillMaxWidth().height(4.dp),
        color = Green, trackColor = cs.surfaceContainerHighest, drawStopIndicator = {})
      Muted("${p.done} of ${p.total} sessions \u00b7 ${p.program.name}")
      if (p.complete) AppOutlinedButton(onNewProgram, Modifier.fillMaxWidth()) { Text("Start a new program") }
    }
  }
}
