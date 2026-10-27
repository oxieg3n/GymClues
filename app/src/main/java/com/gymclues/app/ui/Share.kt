package com.gymclues.app.ui

import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gymclues.app.data.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/** Milestones for the assigned program: 0 = just started, then 10, 25, 50 and 100 percent. */
fun milestoneFor(pct: Int): Int = listOf(100, 50, 25, 10).firstOrNull { pct >= it } ?: 0

private fun lb(v: Double) = "%,d lb".format(Locale.US, Math.round(v))
private fun sets(n: Int) = "$n set${if (n == 1) "" else "s"}"

/** Caption sent with the workout card (X uses it). The card itself lists every set. */
fun dailyShareText(session: List<SetLog>, name: (String) -> String): String {
  if (session.isEmpty()) return "Finished a workout with GymClues. #GymClues"
  val ex = session.map { it.exerciseId }.distinct()
  return "Workout complete: " + ex.take(3).joinToString(", ") { name(it) } + (if (ex.size > 3) " and more" else "") + ". #GymClues"
}

fun workoutShareText(session: CompletedSessionEntity, entries: List<SetLog>, name: (String) -> String): String = buildString {
  append("#GymClues Workout Completed:\n")
  append(sessionDate(session).format(DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)))
  if (session.programName.isNotBlank()) append("\n\nCurrent Program - ").append(session.programName)
  if (session.dayName.isNotBlank()) append("\nDay - ").append(session.dayName)
  if (session.programName.isBlank() && session.dayName.isBlank()) append("\n\n").append(session.name)
  completedExerciseGroups(entries).forEach { (id, sets) ->
    append("\n\n").append(name(id))
    sets.forEachIndexed { index, set -> append('\n').append(if (set.cardio != null) "Entry " else "Set ")
      .append(index + 1).append(" - ").append(setLabel(set)) }
  }
}

fun workoutShareCard(session: CompletedSessionEntity, entries: List<SetLog>, name: (String) -> String): ShareCard =
  ShareCard("Workout complete", session.name,
    sessionDate(session).format(DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US)) + " " +
      DateTimeFormatter.ofPattern("h:mm a", Locale.US).format(Instant.ofEpochMilli(session.completedAt).atZone(ZoneId.systemDefault())),
    workoutShareText(session, entries, name), completedExerciseGroups(entries).map { (id, sets) ->
      name(id) to sets.mapIndexed { i, set -> "${if (set.cardio != null) "Entry" else "Set"} ${i + 1}: ${setLabel(set)}" }.joinToString("\n") })

/** Sets and total weight moved on the given days (a streak). Individual exercises are not included. */
fun streakTotals(logs: List<SetLog>, days: Set<LocalDate>, zone: ZoneId = ZoneId.systemDefault()): Pair<Int, Double> {
  val inStreak = logs.filter { Instant.ofEpochMilli(it.date).atZone(zone).toLocalDate() in days }
  return inStreak.count { it.cardio == null } to inStreak.sumOf { it.weight * it.reps }
}

fun streakShareText(days: Int, sets: Int, volume: Double) =
  "$days-day streak: ${sets(sets)}, ${lb(volume)} moved. #GymClues"

fun milestoneShareText(m: Int, program: String) = when (m) {
  0 -> "Starting $program today. #GymClues"
  100 -> "Finished $program, 100% complete. #GymClues"
  else -> "$m% of $program complete. #GymClues"
}

/** Shares the card image. [pkg] targets one app; null opens the Android share sheet. Returns false if that app is missing. */
private fun send(ctx: Context, card: ShareCard, pkg: String?): Boolean {
  return runCatching {
    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", saveShareCard(ctx, card))
    val i = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri).putExtra(Intent.EXTRA_TEXT, card.caption)
      .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    if (pkg == null) ctx.startActivity(Intent.createChooser(i, "Share").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    else ctx.startActivity(i.setPackage(pkg))
  }.isSuccess
}
fun shareToX(ctx: Context, card: ShareCard) = send(ctx, card, "com.twitter.android") || send(ctx, card, null)
fun shareToFacebook(ctx: Context, card: ShareCard) = send(ctx, card, "com.facebook.katana") || send(ctx, card, null)

private enum class Kind(val label: String) { DAY("This workout"), STREAK("Workout streak"), MILESTONE("Program milestone") }

/** Shown after a workout. The user picks what to share, edits the text, then picks Facebook, X or any other app. */
@Composable fun ShareDialog(app: AppContainer, sessionId: String, onDismiss: () -> Unit) {
  val ctx = LocalContext.current
  val logs by app.workouts.history().collectAsState(emptyList())
  val assignments by app.programs.assignments().collectAsState(emptyList())
  val rest by app.programs.restRules().collectAsState(RestRules.NONE)
  val today = LocalDate.now()
  val sessions by app.workouts.sessions().collectAsState(emptyList())
  val stats = remember(sessions) { workoutStats(sessions.map { sessionDate(it).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }, today) }
  val streakDays = remember(stats, assignments, rest) { programStreakDays(stats.doneDays, app.programs.programs, assignments, rest, today) ?: stats.streakDays }
  val progress = remember(stats, assignments, rest) { programProgress(stats.doneDays, app.programs.programs, assignments, rest, today) }
  val assignmentId = assignmentOn(assignments, today)?.id
  val prefs = remember { ctx.getSharedPreferences("ui", Context.MODE_PRIVATE) }
  val milestone = progress?.let { milestoneFor(it.pct) }
  val isNew = milestone != null && prefs.getInt("ms_$assignmentId", -1) < milestone
  val kinds = Kind.values().filter { it != Kind.MILESTONE || progress != null }
  var kind by remember { mutableStateOf(Kind.DAY) }
  val card = when (kind) {
    Kind.DAY -> sessions.firstOrNull { it.id == sessionId }?.let {
      workoutShareCard(it, sessionSets(logs, sessionId), app.catalog::displayName)
    } ?: ShareCard("Workout complete", "Workout", "", "")
    Kind.STREAK -> streakTotals(logs, streakDays).let { (n, v) ->
      ShareCard("${streakDays.size}-day streak", lb(v), "moved in ${sets(n)}", streakShareText(streakDays.size, n, v)) }
    Kind.MILESTONE -> ShareCard(if (milestone == 0) "New program" else "Program milestone", when (milestone) { 0 -> "Started"; 100 -> "100%"; else -> "$milestone%" },
      progress?.program?.name.orEmpty(), milestoneShareText(milestone ?: 0, progress?.program?.name.orEmpty()))
  }
  val preview = remember(card) { runCatching { renderShareCard(ctx, card).asImageBitmap() }.getOrNull() }
  var copyMessage by remember { mutableStateOf<String?>(null) }
  fun shared() { if (kind == Kind.MILESTONE && milestone != null) prefs.edit().putInt("ms_$assignmentId", milestone).apply(); onDismiss() }
  AlertDialog(onDismissRequest = onDismiss, title = { Text("Share your workout") },
    text = { Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      kinds.forEach { k -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(kind == k, { kind = k })
        Text(k.label + if (k == Kind.MILESTONE && isNew) " (new)" else "", Modifier.weight(1f)) } }
      if (preview != null) Image(preview, "Workout share image preview", Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(MaterialTheme.shapes.medium), contentScale = ContentScale.Fit)
      else Text("Could not render the complete image on this device. The post text can still be copied.", color = MaterialTheme.colorScheme.error)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppButton({ if (shareToFacebook(ctx, card)) shared() else copyMessage = "Could not open sharing. Try again." }, Modifier.weight(1f), enabled = preview != null, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Facebook") }
        AppButton({ if (shareToX(ctx, card)) shared() else copyMessage = "Could not open sharing. Try again." }, Modifier.weight(1f), enabled = preview != null, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("X") }
      }
      AppOutlinedButton({ copyMessage = try {
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("GymClues post", card.caption)); "Post text copied"
      } catch (_: Exception) { "Could not copy post text" } }, Modifier.fillMaxWidth()) { Text("Copy Post Text") }
      copyMessage?.let { Text(it) }
      AppOutlinedButton({ if (send(ctx, card, null)) shared() else copyMessage = "Could not open sharing. Try again." }, Modifier.fillMaxWidth(), enabled = preview != null) { Text("Other apps") }
    } },
    confirmButton = {}, dismissButton = { TextButton(onDismiss) { Text("Not now") } })
}
