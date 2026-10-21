package com.gymclues.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun InfoPill(text: String) {
  Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
    Text(text, Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
      style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
fun IconBadge(icon: ImageVector) {
  Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
      Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
    }
  }
}

@Composable
fun InstructionStep(number: Int, text: String) {
  Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer) {
      Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
        Text(number.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
      }
    }
    Text(text, Modifier.weight(1f).padding(top = 3.dp), style = MaterialTheme.typography.bodyMedium)
  }
}

/** Shared presentation only: Material retains focus, input, click and accessibility behavior. */
@Composable
fun AppButton(
  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(14.dp),
  colors: ButtonColors = ButtonDefaults.buttonColors(),
  contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit
) = Button(onClick, modifier.defaultMinSize(minHeight = 48.dp), enabled, shape,
  colors = colors, elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
  contentPadding = contentPadding, content = content)

@Composable
fun AppOutlinedButton(
  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
  shape: Shape = RoundedCornerShape(14.dp),
  contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
  content: @Composable RowScope.() -> Unit
) = OutlinedButton(onClick, modifier.defaultMinSize(minHeight = 48.dp), enabled, shape,
  colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor = MaterialTheme.colorScheme.onSurface),
  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  contentPadding = contentPadding, content = content)

@Composable
fun AppTonalButton(
  onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  content: @Composable RowScope.() -> Unit
) = FilledTonalButton(onClick, modifier.defaultMinSize(minHeight = 44.dp), enabled,
  shape = RoundedCornerShape(12.dp),
  colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
  contentPadding = contentPadding, content = content)

@Composable
fun AppCard(
  modifier: Modifier = Modifier,
  colors: CardColors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
  content: @Composable ColumnScope.() -> Unit
) = Card(modifier, shape = MaterialTheme.shapes.medium, colors = colors,
  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)), content = content)

@Composable
fun AppListItem(
  headlineContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  overlineContent: (@Composable () -> Unit)? = null,
  supportingContent: (@Composable () -> Unit)? = null,
  leadingContent: (@Composable () -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null
) {
  Surface(modifier.padding(vertical = 4.dp).fillMaxWidth(), shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))) {
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
) = OutlinedTextField(value, onValueChange, modifier, enabled, readOnly, textStyle,
  label = label, placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
  supportingText = supportingText, isError = isError, visualTransformation = visualTransformation,
  keyboardOptions = keyboardOptions, keyboardActions = keyboardActions,
  singleLine = singleLine, maxLines = maxLines, minLines = minLines,
  shape = RoundedCornerShape(14.dp),
  colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant))
