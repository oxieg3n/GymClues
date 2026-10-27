package com.gymclues.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.inspector.WindowInspector
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-mdpi")
class ThemeTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private val prefs get() = ctx.getSharedPreferences("ui", Context.MODE_PRIVATE)
  private lateinit var app: AppContainer
  private var observed = AppTheme.DEFAULT

  @Before fun setup() {
    prefs.edit().clear().putBoolean("tourDone", true).commit()
    // shortcut: Compose 1.7 host clock workaround; remove when Robolectric vsync and prefetch share a clock.
    Class.forName("androidx.compose.foundation.lazy.layout.AndroidPrefetchScheduler")
      .getDeclaredField("frameIntervalNs").apply { isAccessible = true }.setLong(null, Long.MAX_VALUE / 2)
  }

  @After fun cleanup() {
    compose.activity.setContent { }
    compose.waitForIdle()
    if (::app.isInitialized) {
      runBlocking {
        val scope = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }.get(app) as CoroutineScope
        scope.coroutineContext[Job]?.cancelAndJoin()
      }
      app.db.close()
      GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
    }
    Class.forName("androidx.compose.foundation.lazy.layout.AndroidPrefetchScheduler")
      .getDeclaredField("frameIntervalNs").apply { isAccessible = true }.setLong(null, 0L)
  }

  private fun launch() {
    app = AppContainer(ctx)
    content()
  }

  private fun content() {
    compose.activity.setContent { GymCluesTheme {
      val theme = LocalAppTheme.current
      SideEffect { observed = theme }
      App(app)
    } }
    compose.waitForIdle()
  }

  private fun menu(label: String) {
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onNodeWithText(label, substring = false).performScrollTo().performClick()
  }

  private fun shot(name: String) {
    val folder = System.getenv("THEME_PREVIEWS")?.let(::File)?.apply { mkdirs() } ?: return
    compose.waitForIdle()
    compose.runOnIdle {
      val view = WindowInspector.getGlobalWindowViews().last()
      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      view.draw(Canvas(bitmap))
      folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()
    }
  }

  @Test fun fiveStableThemesKeepTheDefaultAndHandleMissingSelections() {
    assertEquals(5, AppTheme.entries.size)
    assertEquals(5, AppTheme.entries.map { it.id }.distinct().size)
    assertEquals(5, AppTheme.entries.map { it.colors.primary }.distinct().size)
    assertEquals("GymClues Default", AppTheme.DEFAULT.title)
    assertEquals(Color.Black, AppTheme.DEFAULT.colors.background)
    assertEquals(Color(0xFF0C0C0C), AppTheme.DEFAULT.colors.surfaceContainer)
    assertEquals(Color(0xFF04F730), AppTheme.DEFAULT.colors.primary)
    listOf(null, "", "removed-theme").forEach { assertEquals(AppTheme.DEFAULT, AppTheme.fromId(it)) }
    AppTheme.entries.forEach { assertEquals(it, AppTheme.fromId(it.id)) }
  }

  @Test fun informationBordersOnlyApplyToGymCluesDefault() {
    var border: androidx.compose.foundation.BorderStroke? = null
    compose.activity.setContent { GymCluesTheme {
      val theme = LocalAppTheme.current
      val stroke = informationBorder()
      SideEffect { observed = theme; border = stroke }
      AppCard { androidx.compose.material3.Text(theme.title) }
    } }
    AppTheme.entries.forEach { theme ->
      compose.runOnIdle { prefs.edit().putString(THEME_KEY, theme.id).apply() }
      compose.waitForIdle()
      assertEquals("Preference listeners apply ${theme.title}", theme, observed)
      if (theme == AppTheme.DEFAULT) {
        assertNotNull(border)
        assertEquals(1.5.dp, border!!.width)
        assertEquals(theme.colors.primary, (border!!.brush as SolidColor).value)
      } else assertNull("${theme.title} keeps its original panel treatment", border)
    }
  }

  private fun contrast(a: Color, b: Color): Float {
    val first = a.luminance(); val second = b.luminance()
    return (maxOf(first, second) + .05f) / (minOf(first, second) + .05f)
  }

  @Test fun everyPaletteKeepsTextAndControlContrast() {
    AppTheme.entries.forEach { theme ->
      val c = theme.colors
      val textPairs = listOf(c.onPrimary to c.primary, c.onPrimaryContainer to c.primaryContainer,
        c.onBackground to c.background, c.onSurface to c.surfaceContainer, c.onSurfaceVariant to c.surfaceContainer,
        c.onSurface to c.surfaceContainerHigh, c.onSurfaceVariant to c.surfaceContainerHigh,
        c.onSurfaceVariant to c.surfaceContainerHighest, c.onSurfaceVariant to c.primaryContainer,
        c.primary to c.primaryContainer, c.primary to c.surfaceContainer, c.secondary to c.surfaceContainer,
        c.tertiary to c.surfaceContainer, c.error to c.surfaceContainer, c.onError to c.error,
        c.onErrorContainer to c.errorContainer)
      textPairs.forEachIndexed { index, (text, ground) ->
        assertTrue("${theme.title} text pair $index: ${contrast(text, ground)}", contrast(text, ground) >= 4.5f)
      }
      assertTrue("${theme.title} input outline", contrast(c.outline, c.surfaceContainerHigh) >= 3f)
    }
  }

  @Test fun menuSelectsEveryThemeAndRestoresTheSavedChoice() {
    launch()
    menu("Theme")
    shot("themes-default-390")
    AppTheme.entries.forEachIndexed { index, theme ->
      compose.onNode(hasScrollAction()).performScrollToIndex(index + 1)
      compose.onNodeWithText(theme.title).assertIsDisplayed()
      if (theme != AppTheme.DEFAULT) compose.onNodeWithContentDescription("Use ${theme.title} theme").performScrollTo().performClick()
      compose.waitUntil(5_000) { observed == theme }
      assertEquals(theme.id.takeUnless { theme == AppTheme.DEFAULT }, prefs.getString(THEME_KEY, null))
      compose.onNodeWithContentDescription("${theme.title}, selected").assertIsNotEnabled()
      compose.onNodeWithContentDescription("Back").performClick()
      compose.onNodeWithText("Today", substring = false).assertIsDisplayed()
      assertEquals(theme.colors.background.toArgb(), compose.activity.window.statusBarColor)
      assertEquals(!theme.dark, WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView).isAppearanceLightStatusBars)
      shot("home-${theme.id}-390")
      menu("Theme")
    }
    shot("themes-daylight-390")
    compose.activity.setContent { }
    compose.waitForIdle()
    content()
    assertEquals(AppTheme.DAYLIGHT, observed)
    menu("Theme")
    compose.onNodeWithText("Current: Daylight.", substring = true).assertIsDisplayed()
    compose.onNode(hasScrollAction()).performScrollToIndex(5)
    compose.onNodeWithContentDescription("Daylight, selected").assertIsNotEnabled()
    compose.onNode(hasScrollAction()).performScrollToIndex(1)
    compose.onNodeWithContentDescription("Use GymClues Default theme").performClick()
    compose.waitUntil(5_000) { observed == AppTheme.DEFAULT }
    assertEquals("default", prefs.getString(THEME_KEY, null))
  }

  private fun startWorkout() {
    menu("Start A Workout")
    compose.onNodeWithText("Bro split").performClick()
    compose.onAllNodesWithText("Details")[0].performClick()
    compose.onNodeWithText("Day 1: Back").performScrollTo().performClick()
    compose.onNodeWithText("Start Workout").performClick()
    compose.waitUntil(10_000) { compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
  }

  @Test fun liveColorChangeKeepsWorkoutDraftAndLightGuidesReadable() {
    launch()
    startWorkout()
    compose.onAllNodes(hasSetTextAction())[0].performTextInput("75")
    compose.onAllNodes(hasSetTextAction())[1].performTextInput("8")
    compose.runOnIdle { prefs.edit().putString(THEME_KEY, "daylight").apply() }
    compose.waitUntil(5_000) { observed == AppTheme.DAYLIGHT }
    compose.onNodeWithText("75").assertExists()
    compose.onNodeWithText("8", substring = false).assertExists()
    shot("workout-daylight-390")
    compose.onNodeWithContentDescription("Muscle map for Deadlift").performClick()
    compose.onNodeWithText("Target").assertIsDisplayed()
    shot("muscle-map-daylight-390")
    compose.onNodeWithContentDescription("Close muscle map").performClick()
    compose.onNodeWithContentDescription("Safety guide for Deadlift").performClick()
    compose.onNodeWithText("Safety tips").assertIsDisplayed()
    shot("safety-daylight-390")
    compose.onNodeWithContentDescription("Close training guide").performClick()
    compose.onNodeWithText("75").assertExists()
    menu("Equipment")
    compose.onNodeWithText("Exercise bike").performClick()
    compose.onNodeWithText("Equipment setup guide").assertIsDisplayed()
    shot("equipment-daylight-390")
  }

  @Test @Config(qualifiers = "w320dp-h740dp-mdpi")
  fun narrowLargeTextKeepsAllThemeButtonsReachable() {
    val config = android.content.res.Configuration(ctx.resources.configuration).apply { fontScale = 1.3f }
    ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
    compose.activity.resources.updateConfiguration(config, ctx.resources.displayMetrics)
    launch()
    menu("Theme")
    shot("themes-large-text-320")
    AppTheme.entries.forEachIndexed { index, theme ->
      compose.onNode(hasScrollAction()).performScrollToIndex(index + 1)
      val action = compose.onNodeWithContentDescription(if (index == 0) "${theme.title}, selected" else "Use ${theme.title} theme")
      action.performScrollTo().assertIsDisplayed()
      val bounds = action.fetchSemanticsNode().boundsInRoot
      assertTrue("${theme.title} fits narrow width", bounds.left >= 0 && bounds.right <= 320)
    }
  }

  @Test @Config(qualifiers = "w768dp-h1024dp-mdpi")
  fun tabletShowsFiveThemeChoices() {
    launch(); menu("Theme")
    AppTheme.entries.forEach { compose.onNodeWithText(it.title).assertIsDisplayed() }
    shot("themes-tablet-768")
  }

  @Test @Config(qualifiers = "w1440dp-h900dp-mdpi")
  fun wideCanvasShowsTheBalancedFiveThemeGrid() {
    launch(); menu("Theme")
    AppTheme.entries.forEach { compose.onNodeWithText(it.title).assertIsDisplayed() }
    val ocean = compose.onNodeWithText("Ocean").fetchSemanticsNode().boundsInRoot
    val ember = compose.onNodeWithText("Ember").fetchSemanticsNode().boundsInRoot
    assertTrue(ember.left > ocean.right)
    shot("themes-wide-1440")
  }
}
