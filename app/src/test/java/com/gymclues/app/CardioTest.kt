package com.gymclues.app

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
class CardioTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private val containers = mutableListOf<AppContainer>()
  private fun app() = AppContainer(ctx).also { containers += it }

  @After fun cleanup() {
    compose.activity.setContent { }
    compose.waitForIdle()
    runBlocking { containers.forEach { app ->
      val f = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }
      (f.get(app) as CoroutineScope).coroutineContext[Job]?.cancelAndJoin()
    } }
    containers.forEach { it.db.close() }
    GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
  }

  @Test fun validatesTimesAndMachineMeasurementsWithoutLosingShortIntervals() {
    assertEquals(0.5, cardioMinutes("0:30")!!, 0.0)
    assertEquals(1.5, cardioMinutes("1.5")!!, 0.0)
    listOf("", "0", "-1", "NaN", "Infinity", "1:60", "-1:30", "a:20").forEach { assertNull(cardioMinutes(it)) }
    val draft = CardioDraft("locomotion", seconds = 20)
    assertEquals("0:20", draft.time)
    assertEquals(20 / 60.0, draft.metrics()!!.minutes, 0.0)
    assertEquals(20 / 60.0, CardioDraft("locomotion", draft.metrics()).metrics()!!.minutes, 0.0)
    draft.speed = "NaN"; assertNull(draft.metrics())
    draft.speed = "5.5"; draft.incline = "-2"; assertNotNull(draft.metrics())
    val stairs = CardioDraft("stairs").apply { time = "10"; floors = "1.5" }
    assertNull(stairs.metrics())
    stairs.floors = "20"; assertEquals(20, stairs.metrics()!!.floors)
    assertFalse(CardioMetrics(10.0, resistance = Double.POSITIVE_INFINITY).valid())
  }

  @Test fun migrationPreservesExistingHistoryAndWorkouts() {
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDb::class.java)
    helper.createDatabase("cardio-migration.db", 5).apply {
      execSQL("INSERT INTO SetLog(date,exerciseId,setNo,reps,weight,units,notes,sessionId,gymId) VALUES(1,'ex_leg_press',1,10,100,'lb','','old','')")
      close()
    }
    val db = helper.runMigrationsAndValidate("cardio-migration.db", 6, true, MIGRATION_5_6)
    db.query("SELECT reps,weight,cardio FROM SetLog").use {
      assertTrue(it.moveToFirst()); assertEquals(10, it.getInt(0)); assertEquals(100.0, it.getDouble(1), 0.0); assertTrue(it.isNull(2))
    }
    db.close()
  }

  @Test fun savesEditsAndSharesCardioAlongsideStrengthAndLoadsAllHiitPlans() = runBlocking {
    val app = app()
    val w = app.workouts
    val cardio = CardioMetrics(20.0, distance = 5.2, resistance = 7.0)
    w.logSet(SetLog(date = now(), exerciseId = "ex_cardio_stationary_bicycling", setNo = 1, reps = 0, weight = 0.0, cardio = cardio, sessionId = "mixed"))
    w.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 10, weight = 100.0, sessionId = "mixed"))
    w.completeSession("mixed", "Mixed workout", "")
    val entry = w.history().first().first { it.cardio != null }
    assertEquals(cardio, entry.cardio)
    assertTrue(setLabel(entry).contains("5.2 mi")); assertFalse(setLabel(entry).contains("reps"))
    w.updateSet(entry.copy(cardio = cardio.copy(minutes = 25.0)))
    assertEquals(25.0, w.history().first().first { it.id == entry.id }.cardio!!.minutes, 0.0)
    assertEquals(1000.0, streakTotals(w.history().first(), setOf(java.time.LocalDate.now())).second, 0.0)
    val hiit = app.programs.programs.filter { it.split == "HIIT" }
    assertEquals(3, hiit.size)
    assertEquals(listOf(840, 2400, 600), hiit.map { it.days.first().items.sumOf { it.durationSec } })
    hiit.forEach { p ->
      val plan = app.programs.plan("${p.id}_1")!!
      assertEquals(p.days.first().items.size, plan.items.size)
      plan.items.forEach { assertTrue(app.catalog.exercise(it.exerciseId)!!.timedCardio); assertTrue(it.durationSec > 0) }
      assertEquals(3, p.days.count { it.training })
    }
    assertEquals("reps", app.catalog.byName("Jumping jacks")!!.tracking)
    w.deleteSession("mixed"); assertTrue(w.history().first().isEmpty())
  }

  @Test fun activeWorkoutRecordsTreadmillFieldsAndPreventsDuplicates() {
    val app = app()
    val id = runBlocking { app.workouts.saveCustom(null, "Cardio day", listOf(TemplateItem("ex_cardio_treadmill_walking", 1, "20", 0))) }
    compose.setContent { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    compose.onNodeWithText("Minutes").performTextReplacement("15")
    compose.onNodeWithText("Speed (mph)").performTextReplacement("3.5")
    compose.onNodeWithText("Incline (%)").performTextReplacement("5")
    compose.onNodeWithText("lb").assertDoesNotExist()
    snapshot("cardio-entry.png")
    compose.onNodeWithText("Record cardio").performClick()
    compose.waitUntil(10_000) { runBlocking { app.workouts.history().first().size == 1 } }
    compose.onNodeWithText("Recorded").assertIsNotEnabled()
    val stored = runBlocking { app.workouts.history().first().single().cardio!! }
    assertEquals(15.0, stored.minutes, 0.0); assertEquals(3.5, stored.speed!!, 0.0); assertEquals(5.0, stored.incline!!, 0.0)
    compose.onNodeWithText("Finish Workout").performScrollTo().performClick()
    compose.onNodeWithText("Done").performClick()
    compose.waitUntil(10_000) { runBlocking { app.workouts.sessions().first().size == 1 } }
    assertEquals(1, runBlocking { app.workouts.history().first().size })
  }

  @Test fun finishingUntouchedCardioTargetsDoesNotInventCompletedMeasurements() {
    val app = app()
    val id = runBlocking { app.workouts.saveCustom(null, "Planned cardio", listOf(TemplateItem("ex_cardio_treadmill_walking", 1, "20", 0))) }
    compose.setContent { GymCluesTheme { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } }
    compose.onNodeWithText("Finish Workout").performScrollTo().performClick()
    compose.onNodeWithText("Done").performClick()
    compose.waitUntil(10_000) { runBlocking { app.workouts.sessions().first().isNotEmpty() } }
    assertTrue(runBlocking { app.workouts.history().first().isEmpty() })
  }

  @Test fun pickerOffersAllCardioAndJumpingJacksUseRepsWithoutWeight() {
    val app = app()
    var selected = emptyList<Exercise>()
    compose.setContent { GymCluesTheme { ExercisePickerSheet(app.catalog, emptyList(), emptySet(), {}, null) { selected = it } } }
    compose.onNodeWithText("Cardio", useUnmergedTree = true).performClick()
    compose.onNodeWithText("Search").performTextInput("Jumping jacks")
    compose.onNode(hasText("Jumping jacks") and !hasSetTextAction()).performClick()
    compose.onNodeWithText("Add 1 exercise(s)").performClick()
    assertEquals("ex_cardio_jumping_jacks", selected.single().id)
    val id = runBlocking { app.workouts.saveCustom(null, "Jacks", listOf(TemplateItem(selected.single().id, 2, "15", 30))) }
    compose.activity.setContent { GymCluesTheme { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } }
    compose.waitForIdle()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Jacks").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("  2 sets  ", useUnmergedTree = true).assertExists()
    compose.onNodeWithText("lb").assertDoesNotExist()
    compose.onAllNodesWithText("reps").assertCountEquals(2)
  }
  @Test fun machineFormsAndHiitCategoryUseTheCorrectFields() {
    compose.setContent { GymCluesTheme { CardioInput(CardioDraft("stairs")) } }
    compose.onNodeWithText("Speed (steps/min)").assertExists()
    compose.onNodeWithText("Floors").assertExists()
    compose.activity.setContent { GymCluesTheme { CardioInput(CardioDraft("bike")) } }
    compose.waitForIdle()
    compose.onNodeWithText("Distance (mi)").assertExists()
    compose.onNodeWithText("Resistance").assertExists()
    compose.activity.setContent { GymCluesTheme { CardioInput(CardioDraft("rower")) } }
    compose.waitForIdle()
    compose.onNodeWithText("Distance (m)").assertExists()
    val app = app()
    compose.activity.setContent { GymCluesTheme { ProgramTypesScreen(app, rememberNavController()) } }
    compose.waitForIdle()
    compose.onNode(hasScrollAction()).performScrollToIndex(app.programs.programs.map { it.split }.distinct().indexOf("HIIT") + 1)
    compose.onNodeWithText("HIIT").assertIsDisplayed()
    snapshot("hiit-category.png")
    compose.activity.setContent { GymCluesTheme { ProgramDayScreen(app, "HIIT_TABATA_1", null, rememberNavController()) } }
    compose.waitForIdle()
    compose.onNodeWithText("300s").assertIsDisplayed()
    compose.onNodeWithText("1 sets", substring = true).assertDoesNotExist()
    snapshot("hiit-program.png")
  }

  private fun snapshot(name: String) {
    val folder = System.getenv("CARDIO_PREVIEWS")?.let { java.io.File(it) } ?: return
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
