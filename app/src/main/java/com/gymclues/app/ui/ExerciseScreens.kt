package com.gymclues.app.ui
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import kotlinx.coroutines.launch

/** Exercises the user added: list, edit, delete. */
@Composable fun MyExercises(app: AppContainer, nav: NavHostController) {
  val mine by app.exercises.mine().collectAsState(emptyList())
  val scope = rememberCoroutineScope()
  var del by remember { mutableStateOf<CustomExerciseEntity?>(null) }
  del?.let { e -> AlertDialog({ del = null }, title = { Text("Delete \"${e.name}\"?") }, text = { Text("Past logs stay in the tracker.") },
    confirmButton = { TextButton({ scope.launch { app.exercises.delete(e.id) }; del = null }) { Text("Delete") } },
    dismissButton = { TextButton({ del = null }) { Text("Cancel") } }) }
  LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    item { Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
      Text("My exercises", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
      FilledTonalButton({ nav.navigate(Routes.addExercise()) }) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text("Add") } } }
    if (mine.isEmpty()) item { Muted("Add exercises that are not in the library. They stay private to you.") }
    items(mine, key = { it.id }) { e -> ListItem(headlineContent = { Text(e.name) }, supportingContent = { Text(e.primaryMuscles.joinToString()) },
      trailingContent = { Row { IconButton({ nav.navigate(Routes.addExercise(e.id)) }) { Icon(Icons.Filled.Edit, "Edit") }
        IconButton({ del = e }) { Icon(Icons.Filled.Delete, "Delete") } } },
      modifier = Modifier.clickable { nav.navigate(Routes.exercise(e.id)) }) }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun AddExercise(app: AppContainer, editId: String?, onSaved: () -> Unit) {
  val repo = app.catalog
  var name by rememberSaveable { mutableStateOf("") }
  var muscles by rememberSaveable { mutableStateOf(listOf<String>()) }
  var equipment by rememberSaveable { mutableStateOf(listOf<String>()) }
  var movement by rememberSaveable { mutableStateOf("") }
  var steps by rememberSaveable { mutableStateOf("") }
  var safety by rememberSaveable { mutableStateOf("") }
  var error by remember { mutableStateOf<String?>(null) }
  var loaded by remember { mutableStateOf(editId == null) }
  val scope = rememberCoroutineScope()
  LaunchedEffect(editId) { if (editId != null && !loaded) { app.dao.customExercise(editId)?.let {
    name = it.name; muscles = it.primaryMuscles; equipment = it.equipmentIds; movement = it.movement; steps = it.steps.joinToString("\n"); safety = it.safety }
    loaded = true } }
  fun <T> List<T>.toggle(x: T) = if (x in this) this - x else this + x
  Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(if (editId == null) "Add an exercise" else "Edit exercise", style = MaterialTheme.typography.headlineSmall)
    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Name") })
    SectionTitle("Muscles worked")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { repo.muscles.forEach { m -> FilterChip(m in muscles, { muscles = muscles.toggle(m) }, { Text(m) }) } }
    SectionTitle("Movement")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("Push", "Pull", "Legs", "Core").forEach { m -> FilterChip(movement == m, { movement = if (movement == m) "" else m }, { Text(m) }) } }
    SectionTitle("Equipment (optional)")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { repo.content.equipment.forEach { q -> FilterChip(q.id in equipment, { equipment = equipment.toggle(q.id) }, { Text(q.name) }) } }
    OutlinedTextField(steps, { steps = it }, Modifier.fillMaxWidth(), minLines = 3, label = { Text("How to do it (one step per line)") })
    OutlinedTextField(safety, { safety = it }, Modifier.fillMaxWidth(), label = { Text("Safety note (optional)") })
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Button({ scope.launch { error = app.exercises.save(editId, name, muscles, equipment, movement, steps.lines(), safety); if (error == null) onSaved() } },
      Modifier.fillMaxWidth().height(52.dp), enabled = loaded) { Text("Save exercise") }
  }
}
