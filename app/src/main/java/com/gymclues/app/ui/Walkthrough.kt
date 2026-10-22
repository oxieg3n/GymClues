package com.gymclues.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal val tourSteps = listOf(
  "gym" to "Find a gym or change your gym here. This always opens Find a Gym, even after you choose one.",
  "logo" to "Open the menu to choose programs, build a workout, or browse your guides and completed sessions.",
  "menu" to "This is the real GymClues menu. Start a workout, or open your completed sessions, program, exercise and equipment lists, or the admin login here.",
  "media" to "Control your music here on any page. Tap the music note to change apps, or the song to open your music app.",
  "progress" to "Follow your program progress here. At 100%, you can start a new program.",
  "streak" to "Your streak counts completed program days. Scheduled rest days and your own rest days do not break it.",
  "schedule" to "See your upcoming sessions here. Week and Month show your full schedule.",
  "rest" to "Take today off or plan future rest days. Your program shifts so no session is skipped."
)
internal class TourState(initial: Int) {
  var step by mutableIntStateOf(initial)
  val bounds = mutableStateMapOf<String, Rect>()
  val target get() = tourSteps.getOrNull(step)?.first
  fun next() { step = if (step == tourSteps.lastIndex) -1 else step + 1 }
}
internal val LocalTour = staticCompositionLocalOf<TourState?> { null }

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable internal fun Modifier.tourTarget(name: String): Modifier {
  val tour = LocalTour.current ?: return this
  val requester = remember { BringIntoViewRequester() }
  LaunchedEffect(tour.step) { if (tour.target == name && name !in listOf("gym", "logo", "media", "menu")) requester.bringIntoView() }
  return bringIntoViewRequester(requester).onGloballyPositioned { tour.bounds[name] = it.boundsInRoot() }
}

@Composable internal fun Walkthrough(tour: TourState, openMenu: () -> Unit) {
  val step = tourSteps.getOrNull(tour.step) ?: return
  val rect = tour.bounds[step.first] ?: return
  BackHandler { tour.step = -1 }
  val density = LocalDensity.current
  val green = MaterialTheme.colorScheme.primary
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
    var cardHeight by remember { mutableIntStateOf(with(density) { 160.dp.roundToPx() }) }
    val gap = with(density) { 22.dp.toPx() }
    val below = rect.center.y < h / 2
    val y = (if (below) rect.bottom + gap else rect.top - gap - cardHeight).coerceIn(8f, maxOf(8f, h - cardHeight - 8f))
    val cardEdge = if (below) y else y + cardHeight
    val target = Offset(rect.center.x.coerceIn(8f, w - 8f), if (below) rect.bottom else rect.top)
    val start = Offset(target.x, cardEdge)
    Canvas(Modifier.fillMaxSize().pointerInput(tour.step, rect) {
      detectTapGestures { if (tour.step == 1 && rect.contains(it)) openMenu() }
    }) {
      val path = Path().apply { fillType = PathFillType.EvenOdd; addRect(Rect(Offset.Zero, size)); addRect(rect.inflate(3.dp.toPx())) }
      drawPath(path, Color.Black.copy(alpha = .72f))
      drawRect(green, rect.topLeft, rect.size, style = Stroke(2.dp.toPx()))
      drawLine(green, start, target, 2.dp.toPx())
      val dy = if (below) 1 else -1
      drawLine(green, target, target + Offset(-6.dp.toPx(), dy * 8.dp.toPx()), 2.dp.toPx())
      drawLine(green, target, target + Offset(6.dp.toPx(), dy * 8.dp.toPx()), 2.dp.toPx())
    }
    Surface(Modifier.offset { IntOffset(with(density) { 16.dp.roundToPx() }, y.roundToInt()) }
      .width(maxWidth - 32.dp).onSizeChanged { cardHeight = it.height }.clickable { tour.next() },
      shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHighest,
      border = androidx.compose.foundation.BorderStroke(1.dp, green)) {
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(step.second, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Tap here to continue", color = green, style = MaterialTheme.typography.labelLarge)
          Text("${tour.step + 1}/${tourSteps.size}", style = MaterialTheme.typography.bodySmall)
        }
        Text("Skip tour", modifier = Modifier.clickable { tour.step = -1 }, style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}
