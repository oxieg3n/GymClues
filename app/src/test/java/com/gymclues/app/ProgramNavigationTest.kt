package com.gymclues.app

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
class ProgramNavigationTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private val containers = mutableListOf<AppContainer>()
  private fun app() = AppContainer(ctx).also { containers += it }

  @After fun cleanup() {
    compose.activity.setContent { }
    compose.waitForIdle()
    runBlocking { containers.forEach { app ->
      val scope = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }.get(app) as CoroutineScope
      scope.coroutineContext[Job]?.cancelAndJoin()
    } }
    containers.forEach { it.db.close() }
    GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
  }

  private fun launch(app: AppContainer) {
    ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).edit().putBoolean("tourDone", true).commit()
    compose.setContent { GymCluesTheme { App(app) } }
  }

  @Test fun leavingUnsavedLayoutAsksBeforeDiscarding() {
    val app = app()
    launch(app)
    compose.onNodeWithText("Unlock Layout").performClick()
    compose.waitForIdle()
    assertTrue(app.layoutEditing)
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onNodeWithText("Exercises").performClick()
    compose.onNodeWithText("Discard layout changes?").assertExists()
    compose.onNodeWithText("Keep editing").performClick()
    compose.onNodeWithText("Cancel layout").assertExists()
  }

  @Test fun assignmentCheckIncludesScheduledProgramsAndExcludesEndedOrDeletedOnes() {
    val today = LocalDate.of(2026, 10, 22)
    val active = ProgramAssignmentEntity(programId = "B1", startDate = today.minusDays(3).toString(), ownerId = "me")
    assertTrue(isProgramAssigned(listOf(active), "B1", today))
    assertTrue(isProgramAssigned(listOf(active.copy(startDate = today.plusDays(3).toString())), "B1", today))
    assertFalse(isProgramAssigned(listOf(active.copy(endDate = today.toString())), "B1", today))
    assertFalse(isProgramAssigned(listOf(active.copy(deleted = true)), "B1", today))
    assertFalse(isProgramAssigned(listOf(active), "B2", today))
    assertFalse(isProgramAssigned(emptyList(), "B1", today))
  }

  @Test fun activeProgramMenuShowsDisabledAlreadyAssignedButton() {
    val app = app()
    runBlocking { app.programs.assign("B1", LocalDate.now()) }
    launch(app)
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Active Program").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Active Program").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Already Assigned").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Already Assigned").assertIsNotEnabled().performClick()
    compose.onNodeWithText("Assign to me").assertDoesNotExist()
    compose.onNodeWithText("Assign").assertDoesNotExist()
    assertEquals(1, runBlocking { app.dao.assignmentsOnce(app.session.userId).size })
    snapshot("active-program-assigned.png")
  }

  @Test fun scheduledProgramIsDisabledAndBecomesAssignableAfterStopping() {
    val app = app()
    runBlocking { app.programs.assign("B1", LocalDate.now().plusDays(2)) }
    compose.setContent { GymCluesTheme { Surface { ProgramDetail(app, "B1", rememberNavController()) } } }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Already Assigned").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Already Assigned").assertIsNotEnabled()
    runBlocking { app.programs.stop() }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Assign to me").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Assign to me").assertIsEnabled().performClick()
    compose.onNodeWithText("Assign").assertExists()
    compose.onNodeWithText("Cancel").performClick()
  }

  @Test fun programListAlsoDisablesTheAssignedProgram() {
    val app = app()
    runBlocking { app.programs.assign("B1", LocalDate.now()) }
    compose.setContent { GymCluesTheme { Surface { ProgramsScreen(app, rememberNavController(), "BRO") } } }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Already Assigned").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Already Assigned").assertIsNotEnabled()
    compose.onNodeWithText("Change start").assertDoesNotExist()
  }

  @Test fun startWorkoutOffersAllProgramTypesAndBuildsACustomWorkout() {
    val app = app()
    launch(app)
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onNodeWithText("Start A Workout").performClick()
    compose.onNodeWithText("Start a Workout").assertIsDisplayed()
    compose.onNodeWithText("Custom Workout").assertIsDisplayed()
    val types = listOf("Bro split", "Push / Pull / Legs", "Upper / Lower", "Full body", "Arnold split", "Torso / Limbs", "PPL + Upper/Lower hybrid", "HIIT")
    types.forEachIndexed { index, label ->
      compose.onNode(hasScrollAction()).performScrollToIndex(index + 2)
      compose.onNodeWithText(label).assertIsDisplayed()
    }
    compose.onNodeWithText("HIIT").performClick()
    compose.onNodeWithText("Tabata 20/10 Cycling").assertExists()
    compose.onNodeWithContentDescription("Back").performClick()
    compose.onNode(hasScrollAction()).performScrollToIndex(0)
    snapshot("start-workout-program-types.png")
    compose.onNodeWithText("Custom Workout").performClick()
    compose.onNodeWithText("Create custom workout").assertIsDisplayed()
    compose.onNodeWithText("Add exercises").performClick()
    compose.onNodeWithText("Search").performTextInput("Walking")
    compose.onNode(hasText("Walking") and !hasSetTextAction()).performClick()
    compose.onNodeWithText("Add 1 exercise(s)").performClick()
    compose.onNodeWithText("Walking", substring = true).assertExists()
    compose.onNodeWithText("Workout name").performTextInput("My walking workout")
    compose.onNodeWithText("Save workout").performScrollTo().performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Saved workouts").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Saved workouts").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("My walking workout").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("My walking workout").assertIsDisplayed()
  }

  private fun snapshot(name: String) {
    val folder = System.getenv("PROGRAM_NAV_PREVIEWS")?.let { java.io.File(it) } ?: return
    folder.mkdirs()
    compose.waitForIdle()
    compose.runOnIdle {
      val view = android.view.inspector.WindowInspector.getGlobalWindowViews().last()
      val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
      view.draw(android.graphics.Canvas(bitmap))
      folder.resolve(name).outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()
    }
  }
}
