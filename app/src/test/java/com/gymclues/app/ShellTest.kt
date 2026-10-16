package com.gymclues.app

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
class ShellTest {
  @get:Rule val compose = createComposeRule()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private fun launch(tourDone: Boolean) {
    ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).edit().clear().putBoolean("tourDone", tourDone).commit()
    compose.setContent { GymCluesTheme { App(AppContainer(ctx)) } }
    compose.waitForIdle()
  }
  @Test fun homeHasOnlyRequestedSectionsAndHeaderPersists() {
    launch(true)
    compose.onNodeWithText("+ Start A Workout +").assertExists()
    compose.onNodeWithText("Program completed").assertExists()
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onNodeWithText("Progress").performClick()
    compose.onNodeWithContentDescription("GymClues menu").assertExists()
    compose.onNodeWithText("Pick your gym").assertExists().performClick()
    compose.onNodeWithText("Find a Gym").assertExists()
  }
  @Test fun startWorkoutOpensCustomWorkouts() {
    launch(true)
    compose.onNodeWithText("+ Start A Workout +").performClick()
    compose.onNodeWithText("Custom Workouts").assertExists()
  }
  @Test fun tutorialOpensRealMenuAndFinishesOnce() {
    launch(false)
    compose.onNodeWithText("Tap here to continue").performClick()
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onNodeWithText("Progress").assertExists()
    compose.onNodeWithText("Equipment").assertExists()
    repeat(7) { compose.onNodeWithText("Tap here to continue").performClick() }
    compose.onNodeWithText("Tap here to continue").assertDoesNotExist()
    assertTrue(ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).getBoolean("tourDone", false))
  }

  @Test fun shareTextsAndMilestones() {
    val z = java.time.ZoneId.of("America/New_York"); val today = java.time.LocalDate.of(2026, 10, 15)
    fun log(d: java.time.LocalDate, id: String, w: Double, r: Int, s: String = "") = SetLog(date = d.atTime(12, 0).atZone(z).toInstant().toEpochMilli(),
      exerciseId = id, setNo = 1, reps = r, weight = w, sessionId = s)
    val logs = listOf(log(today, "a", 100.0, 10, "s1"), log(today, "b", 50.0, 10, "s1"), log(today.minusDays(1), "a", 100.0, 5), log(today.minusDays(9), "a", 999.0, 9))
    assertEquals("Workout done: 2 exercises, 2 sets, 1,500 lb lifted (A, B). #GymClues", dailyShareText(logs.take(2)) { it.uppercase() })
    assertEquals("My week: 2 workouts, 3 sets, 2,000 lb lifted, 4-day streak. #GymClues", weeklyShareText(logs, today, 4, z, java.time.DayOfWeek.MONDAY))
    assertEquals(listOf(0, 0, 10, 10, 25, 50, 100, 100), listOf(0, 9, 10, 24, 25, 99.coerceAtMost(50), 100, 120).map { milestoneFor(it) })
    assertEquals("Starting PPL today. #GymClues", milestoneShareText(0, "PPL")); assertEquals("25% of PPL complete. #GymClues", milestoneShareText(25, "PPL"))
  }
}
