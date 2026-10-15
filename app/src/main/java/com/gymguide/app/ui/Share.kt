package com.gymguide.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gymguide.app.data.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** Milestones for the assigned program: 0 = just started, then 10, 25, 50 and 100 percent. */
fun milestoneFor(pct: Int): Int = listOf(100, 50, 25, 10).firstOrNull { pct >= it } ?: 0

private fun lb(v: Double) = "%,d lb".format(Locale.US, Math.round(v))
private fun sets(n: Int) = "$n set${if (n == 1) "" else "s"}"

fun dailyShareText(session: List<SetLog>, name: (String) -> String): String {
  if (session.isEmpty()) return "Finished a workout with GymCue. #GymCue"
  val ex = session.map { it.exerciseId }.distinct()
  val top = ex.take(3).joinToString(", ") { name(it) } + if (ex.size > 3) " and more" else ""
  return "Workout done: ${ex.size} exercise${if (ex.size == 1) "" else "s"}, ${sets(session.size)}, ${lb(session.sumOf { it.weight * it.reps })} lifted ($top). #GymCue"
}

fun weeklyShareText(logs: List<SetLog>, today: LocalDate, streak: Int, zone: ZoneId = ZoneId.systemDefault(),
                    firstDay: java.time.DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek): String {
  val start = today.with(TemporalAdjusters.previousOrSame(firstDay))
  val week = logs.filter { val d = Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate(); !d.isBefore(start) && !d.isAfter(today) }
  val days = week.map { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() }.distinct().size
  return "My week: $days workout${if (days == 1) "" else "s"}, ${sets(week.size)}, ${lb(week.sumOf { it.weight * it.reps })} lifted" +
    (if (streak > 0) ", $streak-day streak" else "") + ". #GymCue"
}

fun milestoneShareText(m: Int, program: String) = when (m) {
  0 -> "Starting $program today. #GymCue"
  100 -> "Finished $program, 100% complete. #GymCue"
  else -> "$m% of $program complete. #GymCue"
}

private fun send(ctx: Context, text: String, pkg: String?): Boolean {
  val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
  if (pkg == null) { ctx.startActivity(Intent.createChooser(i, "Share workout").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return true }
  return runCatching { ctx.startActivity(i.setPackage(pkg).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
}

fun shareToX(ctx: Context, text: String) {
  if (!send(ctx, text, "com.twitter.android"))
    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/intent/post?text=" + Uri.encode(text))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Facebook ignores pre-filled text from other apps, so the text is also copied for pasting. */
fun shareToFacebook(ctx: Context, text: String) {
  (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("GymCue", text))
  Toast.makeText(ctx, "Text copied. Paste it into your Facebook post.", Toast.LENGTH_LONG).show()
  if (!send(ctx, text, "com.facebook.katana")) send(ctx, text, null)
}

private enum class Kind(val label: String) { DAY("Today's workout"), WEEK("Weekly summary"), MILESTONE("Program milestone") }

/** Shown after a workout. The user picks what to share, edits the text, then picks Facebook, X or any other app. */
@Composable fun ShareDialog(app: AppContainer, sessionId: String, onDismiss: () -> Unit) {
  val ctx = LocalContext.current
  val logs by app.workouts.history().collectAsState(emptyList())
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val rest by app.programs.restRules().collectAsState(RestRules.NONE)
  val today = LocalDate.now()
  val stats = remember(logs) { workoutStats(logs.map { it.date }, today) }
  val streak = remember(stats, assignments, rest) { programStreak(stats.doneDays, app.programs.programs, assignments, rest, today) ?: stats.streak }
  val progress = remember(stats, assignments, rest) { programProgress(stats.doneDays, app.programs.programs, assignments, rest, today) }
  val assignmentId = assignmentOn(assignments, today)?.id
  val prefs = remember { ctx.getSharedPreferences("ui", Context.MODE_PRIVATE) }
  val milestone = progress?.let { milestoneFor(it.pct) }
  val isNew = milestone != null && prefs.getInt("ms_$assignmentId", -1) < milestone
  val kinds = Kind.values().filter { it != Kind.MILESTONE || progress != null }
  var kind by remember { mutableStateOf(if (isNew && milestone != 0) Kind.MILESTONE else Kind.DAY) }
  val generated = when (kind) {
    Kind.DAY -> dailyShareText(logs.filter { it.sessionId == sessionId }) { app.catalog.displayName(it) }
    Kind.WEEK -> weeklyShareText(logs, today, streak)
    Kind.MILESTONE -> milestoneShareText(milestone ?: 0, progress?.program?.name.orEmpty())
  }
  var text by remember(kind, generated) { mutableStateOf(generated) }
  fun shared() { if (kind == Kind.MILESTONE && milestone != null) prefs.edit().putInt("ms_$assignmentId", milestone).apply(); onDismiss() }
  AlertDialog(onDismissRequest = onDismiss, title = { Text("Share your workout") },
    text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      kinds.forEach { k -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(kind == k, { kind = k })
        Text(k.label + if (k == Kind.MILESTONE && isNew) " (new)" else "", Modifier.weight(1f)) } }
      OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), label = { Text("What will be posted") }, minLines = 3)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button({ shareToFacebook(ctx, text); shared() }, Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Facebook") }
        Button({ shareToX(ctx, text); shared() }, Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) { Text("X") }
      }
      OutlinedButton({ send(ctx, text, null); shared() }, Modifier.fillMaxWidth()) { Text("Other apps") }
    } },
    confirmButton = {}, dismissButton = { TextButton(onDismiss) { Text("Not now") } })
}
