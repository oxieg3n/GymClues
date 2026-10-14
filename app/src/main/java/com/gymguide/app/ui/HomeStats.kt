package com.gymguide.app.ui
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** A "workout" = a calendar day with at least one logged set. */
data class WorkoutStats(val week: Int, val month: Int, val year: Int, val weekStreak: Int,
                        val weekDays: List<LocalDate>, val doneDays: Set<LocalDate>, val today: LocalDate)

fun workoutStats(logDates: List<Long>, today: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault(),
                 firstDay: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek): WorkoutStats {
  val days = logDates.mapTo(HashSet()) { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
  val weekStart = today.with(TemporalAdjusters.previousOrSame(firstDay))
  fun hasWeek(start: LocalDate) = (0L..6L).any { start.plusDays(it) in days }
  // Consecutive weeks with a workout; the current week only breaks the streak once it's over.
  var start = if (hasWeek(weekStart)) weekStart else weekStart.minusWeeks(1)
  var streak = 0
  while (hasWeek(start)) { streak++; start = start.minusWeeks(1) }
  return WorkoutStats(
    week = days.count { !it.isBefore(weekStart) && !it.isAfter(today) },
    month = days.count { it.year == today.year && it.month == today.month },
    year = days.count { it.year == today.year },
    weekStreak = streak, weekDays = (0L..6L).map { weekStart.plusDays(it) }, doneDays = days, today = today)
}

@Composable fun StreakPanel(s: WorkoutStats, modifier: Modifier = Modifier) {
  val cs = MaterialTheme.colorScheme
  Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = cs.surfaceContainer,
    border = BorderStroke(1.dp, if (s.weekStreak > 0) cs.primary.copy(alpha = 0.35f) else cs.outlineVariant)) {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocalFireDepartment, null, Modifier.size(40.dp), tint = if (s.weekStreak > 0) cs.primary else cs.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Text("${s.weekStreak}", style = MaterialTheme.typography.displayMedium, color = if (s.weekStreak > 0) cs.primary else cs.onSurface)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
          Text("week streak", style = MaterialTheme.typography.titleMedium)
          Text(when { s.weekStreak == 0 -> "Log a workout to start one"
            s.week == 0 -> "Train this week to keep it going"; else -> "This week counts. Keep it up" },
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
        }
      }
      WeekStrip(s)
      Row(Modifier.fillMaxWidth()) {
        Tally("This week", s.week, Modifier.weight(1f)); Tally("This month", s.month, Modifier.weight(1f)); Tally("This year", s.year, Modifier.weight(1f))
      }
    }
  }
}

@Composable private fun WeekStrip(s: WorkoutStats) {
  val cs = MaterialTheme.colorScheme
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    s.weekDays.forEach { d ->
      val done = d in s.doneDays; val isToday = d == s.today; val future = d.isAfter(s.today)
      val name = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.semantics { contentDescription = "$name ${d.dayOfMonth}" + if (done) ", worked out" else "" }) {
        Text(name.take(1), style = MaterialTheme.typography.labelMedium,
          color = if (isToday) cs.primary else cs.onSurfaceVariant, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
        Surface(Modifier.size(38.dp), shape = CircleShape,
          color = when { done -> cs.primary; future -> cs.surfaceContainerLow; else -> cs.surfaceContainerHighest },
          border = if (isToday && !done) BorderStroke(2.dp, cs.primary) else null) {
          Box(contentAlignment = Alignment.Center) {
            if (done) Icon(Icons.Filled.Check, null, Modifier.size(22.dp), tint = cs.onPrimary)
            else Text("${d.dayOfMonth}", style = MaterialTheme.typography.labelLarge,
              color = if (future) cs.onSurfaceVariant.copy(alpha = 0.5f) else cs.onSurface)
          }
        }
      }
    }
  }
}

@Composable private fun Tally(label: String, n: Int, modifier: Modifier) = Column(modifier) {
  Text("$n", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
  Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
