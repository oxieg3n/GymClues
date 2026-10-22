package com.gymclues.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun InfoPill(text: String) {
  Text(text, Modifier.padding(vertical = 2.dp), style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun IconBadge(icon: ImageVector) {
  Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
    Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
  }
}

@Composable
fun InstructionStep(number: Int, text: String) {
  Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(number.toString().padStart(2, '0'), Modifier.width(28.dp).padding(top = 3.dp),
      style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text, Modifier.weight(1f).padding(top = 3.dp), style = MaterialTheme.typography.bodyMedium)
  }
}

/** Shared presentation only: Material retains focus, input, click and accessibility behavior. */
@Composable
fun AppButton(
  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(24.dp),
  colors: ButtonColors = ButtonDefaults.buttonColors(disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
  contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit
) = Button(onClick, modifier.defaultMinSize(minHeight = 48.dp), enabled, shape,
  colors = colors, elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
  contentPadding = contentPadding, content = content)

@Composable
fun AppOutlinedButton(
  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(24.dp),
  contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit
) = OutlinedButton(onClick, modifier.defaultMinSize(minHeight = 48.dp), enabled, shape,
  colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor = MaterialTheme.colorScheme.primary, disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  contentPadding = contentPadding, content = content)

@Composable
fun AppTonalButton(
  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  content: @Composable RowScope.() -> Unit
) = FilledTonalButton(onClick, modifier.defaultMinSize(minHeight = 44.dp), enabled,
  shape = RoundedCornerShape(24.dp),
  colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer, disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
  contentPadding = contentPadding, content = content)

@Composable
fun AppCard(
  modifier: Modifier = Modifier,
  colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  content: @Composable ColumnScope.() -> Unit
) = Card(modifier, shape = MaterialTheme.shapes.medium, colors = colors,
  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  content = content)

@Composable
fun AppListItem(
  headlineContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  overlineContent: (@Composable () -> Unit)? = null,
  supportingContent: (@Composable () -> Unit)? = null,
  leadingContent: (@Composable () -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null
) {
  Surface(modifier.padding(vertical = 2.dp).fillMaxWidth(), shape = MaterialTheme.shapes.small,
    color = MaterialTheme.colorScheme.surfaceContainerLow) {
    ListItem(headlineContent = {
      ProvideTextStyle(MaterialTheme.typography.titleSmall, headlineContent)
    }, overlineContent = overlineContent, supportingContent = supportingContent,
      leadingContent = leadingContent, trailingContent = trailingContent,
      colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        supportingColor = MaterialTheme.colorScheme.onSurfaceVariant),
      modifier = Modifier.padding(vertical = 3.dp))
  }
}

@Composable
fun AppTextField(
  value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
  enabled: Boolean = true, readOnly: Boolean = false,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  label: (@Composable () -> Unit)? = null, placeholder: (@Composable () -> Unit)? = null,
  leadingIcon: (@Composable () -> Unit)? = null, trailingIcon: (@Composable () -> Unit)? = null,
  supportingText: (@Composable () -> Unit)? = null, isError: Boolean = false,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = false, maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE, minLines: Int = 1
) {
  val cs = MaterialTheme.colorScheme
  val interaction = remember { MutableInteractionSource() }
  val focused by interaction.collectIsFocusedAsState()
  val border = when { isError -> cs.error; focused -> cs.primary; else -> cs.outline }
  BasicTextField(value, onValueChange, modifier.semantics { if (isError) error("Invalid entry") }, enabled, readOnly,
    textStyle = MaterialTheme.typography.bodyMedium.merge(textStyle).copy(color = if (enabled) cs.onSurface else cs.onSurfaceVariant),
    keyboardOptions = keyboardOptions, keyboardActions = keyboardActions, singleLine = singleLine,
    maxLines = maxLines, minLines = minLines, visualTransformation = visualTransformation,
    interactionSource = interaction, cursorBrush = SolidColor(cs.primary),
    decorationBox = { inner ->
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) ProvideTextStyle(MaterialTheme.typography.labelMedium.copy(color = if (isError) cs.error else cs.onSurfaceVariant), label)
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).background(cs.surfaceContainerHigh, MaterialTheme.shapes.small)
          .border(1.dp, border, MaterialTheme.shapes.small).padding(horizontal = 12.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          leadingIcon?.invoke()
          Box(Modifier.weight(1f)) {
            if (value.isEmpty() && placeholder != null) ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(color = cs.onSurfaceVariant), placeholder)
            inner()
          }
          trailingIcon?.invoke()
        }
        if (supportingText != null) ProvideTextStyle(MaterialTheme.typography.bodySmall.copy(color = if (isError) cs.error else cs.onSurfaceVariant), supportingText)
      }
    })
}

@Composable fun ScreenTitle(title: String, detail: String = "") {
  Column(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
    if (detail.isNotBlank()) Muted(detail)
  }
}
