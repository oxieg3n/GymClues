package com.gymclues.app.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

internal fun edgeScrollDelta(y: Float, top: Float, bottom: Float, step: Float) = when {
  y < top -> -step
  y > bottom -> step
  else -> 0f
}

@Composable fun LayoutHandle(label: String, up: () -> Unit, down: () -> Unit, scrollable: ScrollableState? = null) {
  val currentUp by rememberUpdatedState(up)
  val currentDown by rememberUpdatedState(down)
  val density = LocalDensity.current
  val step = with(density) { 72.dp.toPx() }
  val edgeStep = with(density) { 24.dp.toPx() }
  val topEdge = with(density) { 160.dp.toPx() }
  val bottomEdge = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() - 120.dp.toPx() }
  val scope = rememberCoroutineScope()
  var windowTop by remember { mutableFloatStateOf(0f) }
  Icon(Icons.Filled.DragHandle, "Drag $label", Modifier.size(48.dp)
    .onGloballyPositioned { windowTop = it.positionInWindow().y }
    .semantics { customActions = listOf(
      CustomAccessibilityAction("Move $label up") { currentUp(); true },
      CustomAccessibilityAction("Move $label down") { currentDown(); true }) }
    .pointerInput(Unit) {
      var distance = 0f
      detectDragGestures(onDragStart = { distance = 0f }) { change, drag ->
        change.consume(); distance += drag.y
        if (distance > step) { currentDown(); distance = 0f }
        if (distance < -step) { currentUp(); distance = 0f }
        if (scrollable != null) {
          val y = windowTop + change.position.y
          val delta = edgeScrollDelta(y, topEdge, bottomEdge, edgeStep)
          if (delta != 0f) scope.launch { scrollable.scrollBy(delta) }
        }
      }
    })
}

@Composable fun SaveLayoutButton(save: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
  AppButton(save, modifier.heightIn(min = 56.dp), enabled = enabled) { Text("✓ Save Layout") }
}
