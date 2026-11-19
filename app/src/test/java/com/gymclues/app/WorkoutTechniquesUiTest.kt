package com.gymclues.app

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w480dp-h1100dp-mdpi")
class WorkoutTechniquesUiTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private lateinit var app: AppContainer
  private val content = mutableStateOf<@Composable () -> Unit>({})
  private fun show(waitForText: Boolean = true, block: @Composable () -> Unit) {
    compose.mainClock.autoAdvance = false
    content.value = block
    try { compose.waitUntil(10000) {
      compose.mainClock.advanceTimeByFrame()
      (!waitForText || compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).fetchSemanticsNodes().isNotEmpty()) &&
        compose.onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).fetchSemanticsNodes().isEmpty()
    } } finally { compose.mainClock.autoAdvance = true }
    compose.waitForIdle()
  }
  @Before fun setup() {
    Class.forName("androidx.compose.foundation.lazy.layout.AndroidPrefetchScheduler").getDeclaredField("frameIntervalNs").apply { isAccessible = true }.setLong(null, Long.MAX_VALUE / 2)
    app = AppContainer(ctx)
    compose.setContent { content.value() }
  }
  @After fun cleanup() {
    compose.mainClock.autoAdvance = true
    try { compose.activityRule.scenario.close() } finally {
    runBlocking { (AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }.get(app) as CoroutineScope).coroutineContext[Job]?.cancelAndJoin() }
    app.nutrition.close(); app.db.close(); GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
    Class.forName("androidx.compose.foundation.lazy.layout.AndroidPrefetchScheduler").getDeclaredField("frameIntervalNs").apply { isAccessible = true }.setLong(null, 0L)
    }
  }
  private fun start(id: String) { show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }; compose.waitForIdle()
    compose.waitUntil(10000) { compose.onAllNodes(hasScrollAction()).fetchSemanticsNodes().isNotEmpty() }
  }
  private fun scroll(text: String) { compose.onNode(hasScrollAction()).performScrollToNode(hasText(text, substring = true)); compose.waitForIdle() }
  private fun shot(name: String) {
    val dir = System.getenv("TECHNIQUE_PREVIEWS")?.let { java.io.File(it) } ?: return
    dir.mkdirs(); compose.waitForIdle(); compose.runOnIdle {
      val view = android.view.inspector.WindowInspector.getGlobalWindowViews().last()
      val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
      view.draw(android.graphics.Canvas(image)); dir.resolve(name).outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }; image.recycle()
    }
  }
  @Test fun setupCanCancelWithoutChangingWorkout() {
    val a = app.catalog.exercises.first { it.tracking == "strength" }
    var cancelled = false
    show { GymCluesTheme { TechniqueSetup(app.catalog, listOf(TechniqueChoice("existing", a.id, a.name)), false, { cancelled = true }) { _, _ -> fail("Cancelled pairing was saved") } } }
    compose.onNodeWithText("Cancel").performClick(); assertTrue(cancelled)
  }
  @Test fun activeSuperSetShowsBothCardsAndExerciseSpecificDismissibleInstructions() {
    val a = app.catalog.exercises.first { it.tracking == "strength" }
    val b = app.catalog.exercises.first { it.tracking == "strength" && it.id != a.id }
    val c = app.catalog.exercises.first { it.tracking == "strength" && it.id !in listOf(a.id, b.id) }
    val id = runBlocking { app.workouts.saveCustom(null, "Super workout", listOf(TemplateItem(a.id, 1, "10", 90), TemplateItem(b.id, 1, "10", 90), TemplateItem(c.id, 1, "10", 90))) }
    show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    compose.onNodeWithText("Super Set", substring = false).performClick()
    compose.onAllNodesWithText(a.name, substring = false).onLast().performClick()
    compose.onAllNodesWithText(b.name, substring = false).onLast().performClick()
    compose.onNodeWithText("Confirm").performClick()
    scroll("SUPER SET"); shot("super-set.png")
    compose.onNodeWithText("SUPER SET", substring = true).performClick()
    compose.onNodeWithText(superInstructions(a.name, b.name)).assertExists(); shot("super-instructions.png")
    compose.onNodeWithText("Close").performClick()
    compose.onNode(hasScrollAction()).performScrollToIndex(0)
    compose.onNodeWithText("Unlock Layout").performClick()
    val move = compose.onNodeWithContentDescription("Drag ${a.name}").fetchSemanticsNode().config[SemanticsActions.CustomActions].first { it.label.endsWith("down") }
    compose.runOnIdle { assertTrue(move.action()) }
    compose.onNodeWithText("Save Layout", substring = true).performSemanticsAction(SemanticsActions.OnClick) { it() }
    compose.waitUntil(10000) { compose.onAllNodesWithText("Save Layout", substring = true).fetchSemanticsNodes().isEmpty() }
    val json = ctx.getSharedPreferences("active_workouts", Context.MODE_PRIVATE).getString("${app.session.userId}_custom_$id", null)!!
    val draft = kotlinx.serialization.json.Json.decodeFromString<ActiveDraft>(json)
    assertEquals(listOf(c.id, a.id, b.id), draft.exercises.map { it.exerciseId })
    assertEquals(listOf(a.id, b.id, c.id), runBlocking { app.workouts.plan("custom", id)!!.items.map { it.exerciseId } })
    scroll("Unlink Super Set")
    compose.onAllNodesWithText("Unlink Super Set").onFirst().performClick()
    compose.onNodeWithText("SUPER SET", substring = false).assertDoesNotExist()
  }
  @Test fun activeSethSuggestionsAreEditableAndBothPartsAreRequired() {
    val a = app.catalog.exercises.first { it.tracking == "strength" }
    val id = runBlocking { app.workouts.saveCustom(null, "Seth workout", listOf(TemplateItem(a.id, 2, "8", 90))) }
    show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    compose.onNodeWithText("Seth Set", substring = false).performClick()
    compose.onNodeWithText(SETH_CREDIT).assertExists()
    compose.onAllNodesWithText(a.name, substring = false).onLast().performClick(); compose.onNodeWithText("Confirm").performClick()
    compose.onAllNodesWithText("lb", substring = false).onFirst().performTextInput("40")
    compose.onAllNodesWithText("reps", substring = false).onFirst().performTextInput("8")
    compose.onNodeWithContentDescription("Complete set 1 of ${a.name}").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.history().first().size == 1 } }
    scroll("Suggested target")
    compose.onAllNodesWithText("lb", substring = false).onLast().assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, androidx.compose.ui.text.AnnotatedString("")))
    compose.onAllNodesWithText("lb", substring = false).onLast().performTextReplacement("17.5")
    compose.onAllNodesWithText("reps", substring = false).onLast().performTextReplacement("13")
    compose.onNodeWithText("SETH SET · Round 1: incomplete").assertExists()
    compose.onNodeWithContentDescription("Complete set 2 of ${a.name}").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.history().first().size == 2 } }
    compose.onNodeWithText("SETH SET · Round 1: complete").assertExists(); shot("seth-set.png")
    compose.onNodeWithText("SETH SET · Round 1: complete").performClick()
    compose.onNodeWithText(sethInstructions(a.name)).assertExists(); shot("seth-instructions.png")
    compose.onNodeWithText("Close").performClick()
    val logs = runBlocking { app.workouts.history().first() }.sortedBy { it.id }
    assertEquals(17.5, logs[1].weight, 0.0); assertEquals(13, logs[1].reps)
    assertEquals(logs[0].sethRound, logs[1].sethRound)
    scroll("Finish Workout"); compose.onNodeWithText("Finish Workout").performClick(); compose.onNodeWithText("Done").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.sessions().first().isNotEmpty() } }
    val finished = runBlocking { app.workouts.sessions().first().single() }
    show { GymCluesTheme { Surface { SessionDetail(app, finished.id, rememberNavController()) } } }
    compose.waitUntil(10000) { compose.onAllNodesWithText("SETH SET · Part 2").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Set 2: 17.5 lb x 13").assertExists(); shot("completed-techniques.png")
  }
  @Test fun existingAndNewSuperPairDoesNotDuplicateExistingExercise() {
    val a = app.catalog.exercises.first { it.tracking == "strength" }
    val b = app.catalog.exercises.first { it.tracking == "strength" && it.id != a.id }
    val id = runBlocking { app.workouts.saveCustom(null, "Mixed pair", listOf(TemplateItem(a.id, 1, "8", 90))) }
    show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    compose.onNodeWithText("Super Set").performClick(); compose.onAllNodesWithText(a.name).onLast().performClick()
    compose.onNodeWithText("Search exercise library").performClick(); compose.onNodeWithText("Search").performTextInput(b.name)
    compose.onNode(hasText(b.name) and !hasSetTextAction()).performClick(); compose.onNodeWithText("Add 1 exercise(s)").performClick()
    compose.onNodeWithText("Confirm").performClick()
    compose.waitUntil(10000) {
      val raw = ctx.getSharedPreferences("active_workouts", Context.MODE_PRIVATE).getString("${app.session.userId}_custom_$id", null)
      raw != null && kotlinx.serialization.json.Json.decodeFromString<ActiveDraft>(raw).exercises.size == 2
    }
    val raw = ctx.getSharedPreferences("active_workouts", Context.MODE_PRIVATE).getString("${app.session.userId}_custom_$id", null)!!
    val draft = kotlinx.serialization.json.Json.decodeFromString<ActiveDraft>(raw)
    assertEquals(listOf(a.id, b.id), draft.exercises.map { it.exerciseId }); assertEquals(2, draft.exercises.map { it.uid }.distinct().size)
    assertEquals(draft.exercises[0].superSetId, draft.exercises[1].superSetId)
  }
  @Test fun customCardioRemainsSelectableAndSavesTwiceInBuilder() {
    val custom = runBlocking { app.exercises.createCardio("My treadmill", "locomotion").second!! }
    compose.waitUntil(10000) { app.catalog.exercise(custom.id) != null }
    var saved = false
    show { GymCluesTheme { Surface { WorkoutBuilder(app, null, emptySet(), {}, { saved = true }) } } }
    compose.onNodeWithText("Workout name").performTextInput("Custom repeated cardio")
    repeat(2) {
      compose.onNode(hasScrollAction()).performScrollToIndex(0)
      compose.onNodeWithText("Add Cardio").performClick(); compose.onNodeWithText("Search").performTextInput("My treadmill")
      compose.onNode(hasText("My treadmill") and !hasSetTextAction()).performClick()
      compose.onNodeWithText("Add 1 exercise(s)").performClick()
    }
    scroll("Save workout"); compose.onNodeWithText("Save workout").performSemanticsAction(SemanticsActions.OnClick) { it() }
    compose.waitUntil(10000) { compose.waitForIdle(); saved }
    val items = runBlocking { app.workouts.customItems(app.workouts.customWorkouts().first().single().id) }
    assertEquals(listOf(custom.id, custom.id), items.map { it.exerciseId }); assertEquals(2, items.map { it.id }.distinct().size)
  }
  @Test fun completeLongCaptionCanBeCopiedWithoutDiscardingEntries() {
    val treadmill = "ex_cardio_treadmill_walking"
    runBlocking {
      app.workouts.logSet(SetLog(date = 1, exerciseId = treadmill, setNo = 1, reps = 0, weight = 0.0,
        sessionId = "share", instanceId = "before", cardio = CardioMetrics(10.0, speed = 2.7, incline = 2.0)))
      repeat(10) { app.workouts.logSet(SetLog(date = 2L + it, exerciseId = "ex_chest_press", setNo = it + 1, reps = 8, weight = 40.0,
        sessionId = "share", instanceId = "strength")) }
      app.workouts.logSet(SetLog(date = 20, exerciseId = treadmill, setNo = 1, reps = 0, weight = 0.0,
        sessionId = "share", instanceId = "after", cardio = CardioMetrics(20.0, speed = 3.0, incline = 5.0)))
      app.workouts.completeSession("share", "Long complete workout", "")
    }
    val expected = runBlocking { workoutShareText(app.workouts.sessions().first().single(), app.workouts.history().first(), app.catalog::displayName) }
    assertTrue(expected.length > 280)
    show { GymCluesTheme { ShareDialog(app, "share") {} } }
    compose.waitUntil(10000) { compose.onAllNodesWithText("This complete log exceeds X", substring = true).fetchSemanticsNodes().isNotEmpty() }
    scroll("Copy Post Text"); compose.onNodeWithText("Copy Post Text").performClick()
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    assertEquals(expected, clipboard.primaryClip!!.getItemAt(0).text.toString())
  }
  @Test fun linkedWarmupRecordsActualPartsInsteadOfSkippingThem() {
    val exercise = app.catalog.exercises.first { it.tracking == "strength" }
    val id = runBlocking { app.workouts.saveCustom(null, "Linked warm-up", listOf(TemplateItem(exercise.id, 2, "8", 90))) }
    val e = SessionExercise(exercise.id, null, "8", 90, 2, TemplateItem(exercise.id, 2, "8", 90, phase = "Warm-up", sethStarts = listOf(0)))
    e.sets[0].weight = "40"; e.sets[0].reps = "8"; e.sets[1].weight = "20"; e.sets[1].reps = "16"
    val draft = ActiveDraft("warm-up-session", "2026-11-13", listOf(e.draft()))
    ctx.getSharedPreferences("active_workouts", Context.MODE_PRIVATE).edit().putString("${app.session.userId}_custom_$id",
      kotlinx.serialization.json.Json.encodeToString(ActiveDraft.serializer(), draft)).commit()
    show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    scroll("Finish Workout"); compose.onNodeWithText("Finish Workout").performSemanticsAction(SemanticsActions.OnClick) { it() }
    compose.waitUntil(10000) { compose.onAllNodesWithText("Done", substring = false).fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Done").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.sessions().first().isNotEmpty() } }
    val logs = runBlocking { app.workouts.history().first() }
    assertEquals(2, logs.size); assertEquals(setOf(1, 2), logs.map { it.sethPart }.toSet())
    assertEquals(1, logs.map { it.sethRound }.distinct().size)
  }
  @Test fun suggestionsDoNotBecomeRecordedMeasurementsOnFinish() {
    val exercise = app.catalog.exercises.first { it.tracking == "strength" }
    val id = runBlocking { app.workouts.saveCustom(null, "Partial Seth round", listOf(TemplateItem(exercise.id, 2, "8", 90, sethStarts = listOf(0)))) }
    show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    compose.onAllNodesWithText("lb", substring = false).onFirst().performTextInput("40")
    compose.onAllNodesWithText("reps", substring = false).onFirst().performTextInput("8")
    compose.onNodeWithContentDescription("Complete set 1 of ${exercise.name}").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.history().first().size == 1 } }
    scroll("Finish Workout"); compose.onNodeWithText("Finish Workout").performSemanticsAction(SemanticsActions.OnClick) { it() }
    compose.waitUntil(10000) { compose.onAllNodesWithText("Done", substring = false).fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Done").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.sessions().first().isNotEmpty() } }
    val logs = runBlocking { app.workouts.history().first() }
    assertEquals(1, logs.size); assertEquals(1, logs.single().sethPart)
  }
  @Test fun builderCreatesTwoNewLinkedExercisesAndMultipleSethRounds() {
    val a = app.catalog.exercises.first { it.tracking == "strength" }
    val b = app.catalog.exercises.first { it.tracking == "strength" && it.id != a.id }
    var saved = false
    show { GymCluesTheme { Surface { WorkoutBuilder(app, null, emptySet(), {}, { saved = true }) } } }
    compose.onNodeWithText("Workout name").performTextInput("Linked template")
    compose.onNodeWithText("Super Set").performClick(); compose.onNodeWithText("Search exercise library").performClick()
    compose.onNodeWithText("Search", substring = false).performTextInput(a.name)
    compose.onNode(hasText(a.name) and !hasSetTextAction()).performClick()
    compose.onNodeWithText("Search", substring = false).performTextReplacement(b.name)
    compose.onNode(hasText(b.name) and !hasSetTextAction()).performClick()
    compose.onNodeWithText("Add 2 exercise(s)").performClick(); compose.onNodeWithText("Confirm").performClick()
    compose.onNodeWithText("Seth Set").performClick(); compose.onAllNodesWithText(a.name).onLast().performClick(); compose.onNodeWithText("Confirm").performClick()
    compose.onNodeWithText("Seth Set").performClick(); compose.onAllNodesWithText(a.name).onLast().performClick()
    compose.onNodeWithText("First set number (1–9)").performTextReplacement("3"); compose.onNodeWithText("Confirm").performClick()
    scroll("Save workout"); compose.onNodeWithText("Save workout").performSemanticsAction(SemanticsActions.OnClick) { it() }
    try { compose.waitUntil(10000) { compose.waitForIdle(); saved } }
    catch (e: Throwable) { shot("builder-save-failure.png"); throw AssertionError(compose.onAllNodes(isRoot()).onLast().printToString(), e) }
    val plan = runBlocking { val w = app.workouts.customWorkouts().first().single(); app.workouts.plan("custom", w.id)!! }
    assertEquals(listOf(a.id, b.id), plan.items.map { it.exerciseId }); assertTrue(plan.items[0].superSetId.isNotBlank())
    assertEquals(plan.items[0].superSetId, plan.items[1].superSetId); assertEquals(listOf(0, 2), plan.items[0].sethStarts)
  }
  @Test fun duplicateCardioSurvivesReopeningAndAppearsSeparatelyInHistoryAndSharing() {
    val treadmill = "ex_cardio_treadmill_walking"
    val id = runBlocking { app.workouts.saveCustom(null, "Before and after", listOf(TemplateItem(treadmill, 1, "10", 0))) }
    show { GymCluesTheme { Surface { ActiveWorkout(app, "custom", id, emptySet(), rememberNavController()) {} } } }
    compose.waitUntil(10000) { compose.onAllNodesWithText("Minutes").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Minutes").performTextReplacement("10"); compose.onNodeWithText("Speed (mph)").performTextInput("2.7")
    compose.onNodeWithText("Incline (%)").performTextInput("2"); compose.onNodeWithText("Record cardio").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.history().first().size == 1 } }
    compose.onNodeWithText("Add Cardio").performClick(); compose.onNodeWithText("Search", substring = false).performTextInput("Treadmill (walking)")
    val label = app.catalog.displayName(treadmill)
    // Use the actual catalog name rather than depending on display punctuation.
    compose.onNodeWithText("Search", substring = false).performTextReplacement(label)
    compose.onAllNodes(hasText(label) and !hasSetTextAction()).onLast().performClick(); compose.onNodeWithText("Add 1 exercise(s)").performClick()
    scroll("Record cardio")
    compose.onAllNodesWithText("Minutes").onLast().performTextReplacement("20")
    compose.onAllNodesWithText("Speed (mph)").onLast().performTextInput("3.0")
    compose.onAllNodesWithText("Incline (%)").onLast().performTextInput("5")
    compose.onNodeWithText("Record cardio").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.history().first().size == 2 } }
    show(waitForText = false) { }; compose.waitForIdle(); start(id)
    scroll("Finish Workout"); compose.onNodeWithText("Finish Workout").performClick(); compose.onNodeWithText("Done").performClick()
    compose.waitUntil(10000) { runBlocking { app.workouts.sessions().first().isNotEmpty() } }
    val session = runBlocking { app.workouts.sessions().first().single() }
    assertNull(ctx.getSharedPreferences("active_workouts", Context.MODE_PRIVATE).getString("${app.session.userId}_custom_$id", null))
    val logs = runBlocking { app.workouts.history().first() }.sortedBy { it.id }
    assertEquals(2, logs.map { it.instanceId }.distinct().size); assertEquals(10.0, logs[0].cardio!!.minutes, 0.0)
    assertEquals(20.0, logs[1].cardio!!.minutes, 0.0)
    val card = workoutShareCard(session, logs, app.catalog::displayName)
    assertEquals(listOf(label, label), card.rows.map { it.first })
    show { GymCluesTheme { Surface { SessionDetail(app, session.id, rememberNavController()) } } }; compose.waitForIdle()
    compose.waitUntil(10000) { compose.onAllNodesWithText(label).fetchSemanticsNodes().size == 2 }
    compose.onAllNodesWithText(label).assertCountEquals(2); shot("completed-session.png")
  }
}
