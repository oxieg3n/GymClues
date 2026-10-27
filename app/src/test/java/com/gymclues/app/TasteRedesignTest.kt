package com.gymclues.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.inspector.WindowInspector
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

/** Native layout/interaction checks. Seeded history is preview data in this test database only. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-mdpi")
class TasteRedesignTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private lateinit var app: AppContainer

  @Before fun alignPreviewPrefetchClock() {
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

  private fun start(assigned: Boolean = false) {
    ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).edit().putBoolean("tourDone", true).commit()
    app = AppContainer(ctx)
    if (assigned) runBlocking { app.programs.assign("B1", LocalDate.now()) }
    compose.setContent { GymCluesTheme { App(app) } }
    compose.waitForIdle()
    if (assigned) compose.waitUntil(10_000) { compose.onAllNodesWithText("Start today's workout").fetchSemanticsNodes().isNotEmpty() }
  }

  private fun menu(label: String) {
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onNodeWithText(label).performClick()
    compose.waitForIdle()
  }

  private fun shot(name: String) {
    val folder = System.getenv("TASTE_PREVIEWS")?.let(::File)?.apply { mkdirs() } ?: return
    compose.waitForIdle()
    compose.runOnIdle {
      val view = WindowInspector.getGlobalWindowViews().last()
      val image = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      view.draw(Canvas(image))
      folder.resolve("$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
      image.recycle()
    }
  }

  @Test fun phoneScreensKeepNavigationGuidesAndDraftInput() {
    start()
    compose.onNodeWithText("Today").assertIsDisplayed()
    compose.onNodeWithText("Choose a program").assertIsDisplayed()
    compose.onNodeWithText("Program completed").assertDoesNotExist()
    shot("01-home-empty-390")
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    shot("02-menu-390")
    compose.onNodeWithText("Start A Workout").performClick()
    shot("03-program-types-390")
    compose.onNodeWithText("Bro split").performClick()
    shot("04-programs-390")
    compose.onAllNodesWithText("Details")[0].performClick()
    shot("05-program-detail-390")
    compose.onNodeWithText("Day 1: Back").performScrollTo().performClick()
    shot("06-session-preview-390")
    compose.onNodeWithText("Start Workout").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Muscle map for Deadlift").fetchSemanticsNodes().isNotEmpty() }
    compose.onAllNodes(hasSetTextAction())[0].performTextInput("75")
    compose.onAllNodes(hasSetTextAction())[1].performTextInput("8")
    shot("07-workout-390")
    compose.onNodeWithContentDescription("Muscle map for Deadlift").performClick()
    shot("08-muscle-map-390")
    compose.onNodeWithContentDescription("Close muscle map").performClick()
    compose.onNodeWithText("75").assertExists()
    compose.onNodeWithContentDescription("Safety guide for Deadlift").performClick()
    shot("09-safety-390")
    compose.onNodeWithContentDescription("Close training guide").performClick()
    compose.onNodeWithText("75").assertExists()
    menu("Equipment")
    shot("10-equipment-390")
    compose.onNodeWithText("Exercise bike").performClick()
    shot("11-equipment-guide-390")
    menu("Completed Sessions")
    compose.onNodeWithText("No completed sessions yet").assertExists()
    shot("12-history-empty-390")
    menu("My Exercises")
    compose.onNodeWithText("Add", substring = false).performClick()
    shot("13-exercise-form-390")
    compose.onNodeWithText("Name").performTextInput("Preview exercise")
    compose.onNodeWithText("Preview exercise").assertExists()
    menu("Start A Workout")
    compose.onNodeWithText("Custom Workout").performClick()
    shot("14-workout-builder-390")
    compose.onNodeWithText("Add exercises").performClick()
    shot("15-exercise-picker-390")
  }

  @Test @Config(qualifiers = "w320dp-h740dp-mdpi")
  fun narrowScreenCanStartTodayAndEditASet() {
    start(assigned = true)
    val action = compose.onNodeWithText("Start today's workout").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
    assertTrue("Action fits narrow phone", action.left >= 0f && action.right <= 320f)
    shot("16-home-assigned-320")
    compose.onNodeWithText("Start today's workout").performClick()
    compose.waitUntil(10_000) { compose.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
    compose.onAllNodes(hasSetTextAction())[0].performTextInput("100")
    compose.onAllNodes(hasSetTextAction())[1].performTextInput("8")
    compose.onNodeWithText("100").assertIsDisplayed()
    shot("17-workout-320")
    compose.onNodeWithContentDescription("GymClues menu").assertIsDisplayed()
  }

  @Test fun assignedHomeAndRecoveryUseRealSchedule() {
    start(assigned = true)
    runBlocking {
      repeat(3) { day -> app.workouts.logSet(SetLog(
        date = LocalDate.now().minusDays(day.toLong()).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        exerciseId = app.catalog.byName("Deadlift")!!.id, setNo = 1, reps = 8, weight = 75.0,
        sessionId = if (day == 0) "taste-preview" else "taste-preview-$day")) }
      val assigned = scheduledDay(app.programs.programs, app.programs.assignments().first(), LocalDate.now(), RestRules.NONE)!!
      app.workouts.completeSession("taste-preview", "Back", "", LocalDate.now().toString(), planId(assigned))
    }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Logged today").fetchSemanticsNodes().isNotEmpty() }
    shot("18-home-assigned-390")
    compose.onNodeWithText("Rest Day", substring = false).performClick()
    shot("19-rest-dialog-390")
    compose.onNodeWithText("Set Today as Rest Day").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Recovery day").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Recovery day").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Start today's workout").assertDoesNotExist()
    shot("20-home-recovery-390")
    menu("Completed Sessions")
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Back").fetchSemanticsNodes().isNotEmpty() }
    shot("24-history-populated-390")
    compose.onNodeWithText("Back").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Share", substring = false).fetchSemanticsNodes().isNotEmpty() }
    shot("25-history-detail-390")
    menu("Exercises")
    shot("26-muscle-library-390")
    compose.onNodeWithText("Back", substring = false).performScrollTo().performClick()
    shot("27-exercise-library-390")
    compose.onNodeWithText("45-degree back extension", substring = false).performClick()
    shot("28-exercise-guide-390")
    compose.onNodeWithText("Pick your gym").performClick()
    shot("29-find-gym-390")
    menu("Admin")
    shot("30-pin-form-390")
  }

  @Test @Config(qualifiers = "w320dp-h740dp-mdpi")
  fun largerTextKeepsTheHeaderAndPrimaryActionReachable() {
    val config = android.content.res.Configuration(ctx.resources.configuration).apply { fontScale = 1.3f }
    ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
    compose.activity.resources.updateConfiguration(config, ctx.resources.displayMetrics)
    start()
    compose.onNodeWithContentDescription("GymClues menu").assertIsDisplayed()
    compose.onNodeWithText("Choose a program").performScrollTo().assertIsDisplayed()
    shot("21-large-text-320")
  }

  @Test @Config(qualifiers = "w768dp-h1024dp-mdpi")
  fun tabletUsesBothColumns() {
    start(assigned = true)
    compose.onNodeWithText("Training rhythm").assertIsDisplayed()
    val training = compose.onNodeWithText("Today's training").fetchSemanticsNode().boundsInRoot
    val rhythm = compose.onNodeWithText("Training rhythm").fetchSemanticsNode().boundsInRoot
    assertTrue("Tablet places stats alongside the session", rhythm.left > training.right)
    shot("22-home-tablet-768")
  }

  @Test @Config(qualifiers = "w1440dp-h900dp-mdpi")
  fun wideScreenKeepsTheWorkspaceCentered() {
    start(assigned = true)
    val title = compose.onNodeWithText("Today").fetchSemanticsNode().boundsInRoot
    assertTrue("Wide layout uses a centered readable canvas", title.left >= 200f)
    compose.onNodeWithText("Start today's workout").assertIsDisplayed()
    compose.onNodeWithText("Program progress").assertIsDisplayed()
    shot("23-home-wide-1440")
  }
}
