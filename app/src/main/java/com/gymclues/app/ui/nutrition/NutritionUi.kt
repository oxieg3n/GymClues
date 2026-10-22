package com.gymclues.app.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gymclues.app.ui.*
import com.gymclues.app.data.nutrition.*
import kotlinx.coroutines.*
import java.util.Locale

internal fun amount(value: Double?, digits: Int = 1): String = value?.let { String.format(Locale.getDefault(), "%.${digits}f", it) } ?: "—"
internal fun number(text: String): Double = text.trim().toDoubleOrNull()?.takeIf { it.isFinite() } ?: error("Enter a valid number")
internal fun optional(text: String): Double? = if (text.isBlank()) null else number(text)

@Stable internal class NutritionActions(private val scope: CoroutineScope) {
  var busy by mutableStateOf(false); private set
  var message by mutableStateOf("")
  private var lastStart = 0L
  private var lastKey = ""
  fun run(done: () -> Unit = {}, actionKey: String = "", block: suspend () -> Unit) {
    val time = android.os.SystemClock.elapsedRealtime()
    if (busy || (actionKey.isNotEmpty() && actionKey == lastKey && lastStart != 0L && time - lastStart < 500)) return
    lastStart = time; lastKey = actionKey; busy = true; message = ""
    scope.launch(Dispatchers.Main.immediate) {
      try { block(); done() }
      catch (e: CancellationException) { throw e }
      catch (e: Exception) { message = if (e is java.io.IOException) providerMessage(e) else e.message?.takeIf { it.length < 250 } ?: "Could not save. Your previous data is unchanged." }
      finally { busy = false }
    }
  }
}
@Composable internal fun NutritionDialog(onDismissRequest: () -> Unit, title: @Composable () -> Unit,
  text: @Composable () -> Unit, confirmButton: @Composable () -> Unit, dismissButton: @Composable () -> Unit) {
  Dialog(onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
      modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(.92f).heightIn(max = (LocalConfiguration.current.screenHeightDp * .85).dp)) {
      Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProvideTextStyle(MaterialTheme.typography.titleLarge, title)
        text()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { dismissButton(); confirmButton() }
      }
    }
  }
}
@Composable internal fun rememberActions(): NutritionActions {
  val scope = rememberCoroutineScope(); return remember(scope) { NutritionActions(scope) }
}
@Composable internal fun ActionMessage(actions: NutritionActions) {
  if (actions.busy) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Working…") }
  if (actions.message.isNotBlank()) Text(actions.message, color = MaterialTheme.colorScheme.onSurface)
}
@Composable internal fun NutritionPage(content: LazyListScope.() -> Unit) {
  LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp).imePadding(), contentPadding = PaddingValues(vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
}
@Composable internal fun DecimalField(label: String, value: String, changed: (String) -> Unit) {
  AppTextField(value, changed, Modifier.fillMaxWidth(), label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}
@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun Options(values: List<String>, selected: String, changed: (String) -> Unit) {
  FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    values.forEach { value -> FilterChip(selected == value, { changed(value) }, label = { Text(value) }) }
  }
}
@Composable internal fun NutrientValues(nutrients: Nutrients) {
  Text("${amount(nutrients.kcal, 0)} kcal · P ${amount(nutrients.protein)} g · C ${amount(nutrients.carbs)} g · F ${amount(nutrients.fat)} g")
  Muted("Fiber ${amount(nutrients.fiber)} g · Sugar ${amount(nutrients.sugar)} g · Sodium ${amount(nutrients.sodium, 0)} mg")
  if (listOf(nutrients.kcal, nutrients.protein, nutrients.carbs, nutrients.fat).any { it == null }) Muted("— means the source did not provide this value. Totals with missing values stay incomplete.")
}
@Composable internal fun NutrientProgress(label: String, consumed: Double?, target: Double?, unit: String = "g") {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("$label: ${amount(consumed)} / ${amount(target)} $unit")
    LinearProgressIndicator(progress = { if (consumed != null && target != null && target > 0) (consumed / target).toFloat().coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth())
  }
}
