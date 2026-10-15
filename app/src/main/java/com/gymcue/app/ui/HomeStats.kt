package com.gymcue.app.ui
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import com.gymcue.app.data.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
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
                        val doneDays: Set<LocalDate>, val today: LocalDate)

fun workoutStats(logDates: List<Long>, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(),
                 firstDay: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek): WorkoutStats {
  val days = logDates.mapTo(HashSet()) { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
  val weekStart = today.with(TemporalAdjusters.previousOrSame(firstDay))
  // Today not logged yet does not break the streak; it only breaks once a full day has passed without a workout.
  var d = if (today in days) today else today.minusDays(1)
  var streak = 0
  while (d in days) { streak++; d = d.minusDays(1) }
  return WorkoutStats(
    week = days.count { !it.isBefore(weekStart) && !it.isAfter(today) },
    month = days.count { it.year == today.year && it.month == today.month },
    year = days.count { it.year == today.year },
    streak = streak, doneDays = days, today = today)
}

/** [program] = the streak counts program training days, not gym days. */
@Composable fun StreakPanel(s: WorkoutStats, program: Boolean = false, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = cs.surfaceContainer,
    border = BorderStroke(1.dp, if (s.streak > 0) cs.primary.copy(alpha = 0.35f) else cs.outlineVariant)) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocalFireDepartment, null, Modifier.size(40.dp), tint = if (s.streak > 0) cs.primary else cs.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Text("${s.streak}", style = MaterialTheme.typography.displayMedium, color = if (s.streak > 0) cs.primary else cs.onSurface)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
          Text("day streak", style = MaterialTheme.typography.titleMedium)
          Text(when { program -> if (s.streak == 0) "Finish a program session to start one" else "Program days in a row. Rest days don't break it"
            s.streak == 0 -> "Log a workout to start one"
            s.today !in s.doneDays -> "Train today to keep it going"; else -> "Today counts. Keep it up" },
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
      }
      Row(Modifier.fillMaxWidth()) {
        Tally("This week", s.week, Modifier.weight(1f)); Tally("This month", s.month, Modifier.weight(1f)); Tally("This year", s.year, Modifier.weight(1f))
      }
    }
  }
}

@Composable private fun Tally(label: String, n: Int, modifier: Modifier) = Column(modifier) {
  Text("$n", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
  Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable fun ProgramProgressCard(p: ProgramProgress, onNewProgram: () -> Unit) {
  val cs = MaterialTheme.colorScheme
  Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = cs.surfaceContainer,
    border = BorderStroke(1.dp, if (p.complete) cs.primary else cs.outlineVariant)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (p.complete) "Program complete" else "Program completed", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text("${p.pct}%", style = MaterialTheme.typography.titleLarge, color = cs.primary)
      }
      LinearProgressIndicator(progress = { p.pct / 100f }, Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
        color = cs.primary, trackColor = cs.surfaceContainerHighest)
      Text("${p.done} of ${p.total} sessions in ${p.program.name}", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
      if (p.complete) Button(onNewProgram, Modifier.fillMaxWidth()) { Text("Start a new program") }
    }
  }
}
