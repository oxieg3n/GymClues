package com.gymclues.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gymclues.app.data.*

const val SUPER_EXPLANATION = "A Super Set is two different exercises performed back-to-back without resting between them. Complete one set of the first exercise, immediately complete one set of the second exercise, and then rest before repeating if needed."
const val SETH_CREDIT = "Technique credit: Seth Feroce — Axe & Sledge Supplements"
const val SETH_EXPLANATION = "A Seth Set is a training technique associated with bodybuilder Seth Feroce of Axe & Sledge Supplements.\n\nPerform a challenging first set for at least 8 repetitions. Immediately reduce the weight by approximately 50% and aim for at least double the repetitions without resting.\n\nExample: 40 lbs for 8 reps, immediately followed by 20 lbs for 16 reps."
fun superInstructions(first: String, second: String) = "Complete one set of $first, then immediately perform one set of $second without resting.\n\nAfter completing both exercises, you have finished one Super Set round.\n\nRest if needed, then repeat."
fun sethInstructions(name: String) = "For $name, perform the first set with a challenging weight for at least 8 reps. Immediately reduce the weight to approximately half and aim for at least double the repetitions.\n\nExample: 40 lbs x 8 followed immediately by 20 lbs x 16. Together, these make one Seth Set.\n\n$SETH_CREDIT"
fun sethSuggestion(weight: String, reps: String): Pair<Double, Int>? {
  val w = weight.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0 } ?: return null
  val r = reps.toIntOrNull()?.takeIf { it in 0..Int.MAX_VALUE / 2 } ?: return null
  return w * 0.5 to r * 2
}
/** Moves whole adjacent groups, including when an independent card crosses a pair. */
fun <T> moveWorkoutGroup(items: MutableList<T>, item: T, down: Boolean, group: (T) -> String) {
  val blocks = mutableListOf<MutableList<T>>()
  items.forEach { e ->
    if (group(e).isNotBlank() && blocks.lastOrNull()?.firstOrNull()?.let(group) == group(e)) blocks.last() += e
    else blocks += mutableListOf(e)
  }
  val at = blocks.indexOfFirst { item in it }; val to = at + if (down) 1 else -1
  if (at < 0 || to !in blocks.indices) return
  blocks.add(to, blocks.removeAt(at)); items.clear(); items.addAll(blocks.flatten())
}
fun superRoundComplete(a: SessionExercise, b: SessionExercise, round: Int) =
  a.sets.getOrNull(round)?.let { it.done && it.logged } == true && b.sets.getOrNull(round)?.let { it.done && it.logged } == true
fun sethRoundComplete(e: SessionExercise, first: Int) = e.sethStarts.contains(first) &&
  e.sets.getOrNull(first)?.let { it.done && it.logged } == true && e.sets.getOrNull(first + 1)?.let { it.done && it.logged } == true

@Composable fun TechniqueInfo(label: String, instructions: String, status: String = "") {
  var open by remember { mutableStateOf(false) }
  AppOutlinedButton({ open = true }, Modifier.fillMaxWidth()) { Text(label + if (status.isBlank()) "" else " · $status") }
  if (open) AlertDialog({ open = false }, title = { Text(label) }, text = { Text(instructions) },
    confirmButton = { TextButton({ open = false }) { Text("Close") } })
}

data class TechniqueChoice(val key: String, val exerciseId: String, val name: String, val group: String = "")

/** New library selections stay staged here until Confirm; existing instances are reused by key. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun TechniqueSetup(catalog: CatalogRepository, existing: List<TechniqueChoice>, seth: Boolean,
  dismiss: () -> Unit, initial: List<String> = emptyList(), confirm: (List<TechniqueChoice>, Int) -> Unit) {
  val staged = remember { mutableStateListOf<TechniqueChoice>() }
  val selected = remember { mutableStateListOf<String>().apply { addAll(initial) } }
  var library by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }
  var start by remember { mutableStateOf("1") }
  val choices = (existing + staged).filter { !seth || catalog.exercise(it.exerciseId)?.tracking == "strength" }
  val picked = selected.mapNotNull { key -> choices.firstOrNull { it.key == key } }
  val valid = picked.size == (if (seth) 1 else 2) && (seth || picked.map { it.exerciseId }.distinct().size == 2) &&
    (!seth || start.toIntOrNull()?.let { it in 1 until MAX_SETS } == true)
  if (library) ExercisePickerSheet(catalog, null, choices.map { it.exerciseId }.toSet(), { library = false },
    eligible = { e -> choices.none { it.exerciseId == e.id } && (!seth || e.tracking == "strength") }, maxPicks = if (seth) 1 else 2) { exercises ->
    exercises.forEach { e -> val c = TechniqueChoice(newId(), e.id, e.name); staged += c
      if (selected.size < (if (seth) 1 else 2)) selected += c.key }
    library = false
  }
  else ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
    Column(Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(20.dp)) {
    Text(if (seth) "What is a Seth Set?" else "What is a Super Set?", style = MaterialTheme.typography.titleLarge)
    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(if (seth) SETH_EXPLANATION else SUPER_EXPLANATION)
      if (seth) Text(SETH_CREDIT, color = MaterialTheme.colorScheme.primary)
      AppTextField(query, { query = it }, label = { Text("Search workout") }, singleLine = true)
      choices.filter { it.name.contains(query, true) }.forEach { c ->
        Row(Modifier.fillMaxWidth().toggleable(c.key in selected, enabled = c.group.isBlank() || seth, onValueChange = { on ->
          if (!on) selected.remove(c.key)
          else if (selected.size < (if (seth) 1 else 2)) selected += c.key
        })) { Checkbox(c.key in selected, null, enabled = c.group.isBlank() || seth); Text(c.name + if (c.group.isNotBlank() && !seth) " (already linked)" else "") }
      }
      AppOutlinedButton({ library = true }) { Text("Search exercise library") }
      if (seth) AppTextField(start, { start = it }, label = { Text("First set number (1–9)") }, singleLine = true)
      Text("Selected: " + picked.joinToString(" → ") { it.name })
      if (seth) Muted("Links this set and the following set. Targets are guidance; record your actual performance. Configure another pair for more rounds.")
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
      TextButton(dismiss) { Text("Cancel") }
      TextButton({ confirm(picked, (start.toIntOrNull() ?: 1) - 1) }, enabled = valid) { Text("Confirm") }
    }
  } }
}
