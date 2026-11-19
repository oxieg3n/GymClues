package com.gymclues.app

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34]) @GraphicsMode(GraphicsMode.Mode.NATIVE)
class WorkoutTechniquesTest {
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  @Test fun suggestionsAreGuidanceAndNeverMutateActualValues() {
    listOf(40 to 8, 50 to 10, 60 to 12).forEach { (w, r) -> assertEquals(w / 2.0 to r * 2, sethSuggestion("$w", "$r")) }
    assertEquals(12.25 to 18, sethSuggestion("24.5", "9"))
    assertEquals("12.25 lb x 18", setLabel(SetLog(date = 1, exerciseId = "curl", setNo = 2, reps = 18, weight = 12.25)))
    assertEquals(20.0 to 0, sethSuggestion("40", "0"))
    listOf("NaN", "Infinity", "-1", "").forEach { assertNull(sethSuggestion(it, "8")) }
    assertNull(sethSuggestion("40", "2147483647"))
    val e = SessionExercise("curl", null, "", 90, 4)
    e.sethStarts.addAll(listOf(0, 2)); e.sets[0].weight = "40"; e.sets[0].reps = "8"
    e.sets[1].weight = "17.5"; e.sets[1].reps = "13"
    sethSuggestion(e.sets[0].weight, e.sets[0].reps)
    assertEquals("17.5", e.sets[1].weight); assertEquals("13", e.sets[1].reps)
    assertFalse(sethRoundComplete(e, 0))
    e.sets[0].done = true; e.sets[0].logged = true
    assertFalse(sethRoundComplete(e, 0)); e.sets[1].done = true; e.sets[1].logged = true
    assertTrue(sethRoundComplete(e, 0)); assertFalse(sethRoundComplete(e, 2))
    e.sethStarts.clear(); assertEquals("17.5", e.sets[1].weight)
    assertFalse(validSethStarts(listOf(0, 1), 4)); assertFalse(validSethStarts(listOf(3), 4))
    assertFalse(validSethStarts(listOf(Int.MAX_VALUE), 4))
    assertTrue(validSethStarts(listOf(0, 2), 4))
  }
  @Test fun superRoundsRequireBothAndMoveAsAnIndivisibleBlock() {
    val a = SessionExercise("a", null, "", 90, 3); val b = SessionExercise("b", null, "", 90, 2)
    val c = SessionExercise("c", null, "", 90, 1)
    a.superSetId = "pair"; b.superSetId = "pair"
    val list = mutableListOf(a, b, c)
    b.sets[1].done = true; b.sets[1].logged = true
    assertFalse(superRoundComplete(a, b, 1)); a.sets[1].done = true; a.sets[1].logged = true
    assertTrue(superRoundComplete(a, b, 1)); assertFalse(superRoundComplete(a, b, 0)); assertFalse(superRoundComplete(a, b, 2))
    moveWorkoutGroup(list, b, true) { it.superSetId }; assertEquals(listOf(c, a, b), list)
    moveWorkoutGroup(list, c, true) { it.superSetId }; assertEquals(listOf(a, b, c), list)
    a.superSetId = ""; b.superSetId = ""; assertTrue(a.sets[1].logged)
    assertTrue(superInstructions("Skullcrushers", "Hammer Curls").contains("Skullcrushers, then immediately perform one set of Hammer Curls"))
    assertTrue(sethInstructions("Dumbbell Curls").contains(SETH_CREDIT))
  }
  @Test fun draftReopeningRetainsMultipleRoundsAndIndependentRepeatedCardio() {
    val a = SessionExercise("curl", null, "", 90, 4); a.sethStarts.addAll(listOf(0, 2)); a.superSetId = "pair"
    val b = SessionExercise("press", null, "", 90, 2); b.superSetId = "pair"
    val t1 = SessionExercise("treadmill", null, "", 0, 1, tracking = "locomotion")
    val t2 = SessionExercise("treadmill", null, "", 0, 1, tracking = "locomotion")
    t1.cardio!!.time = "10"; t1.cardio.speed = "2.7"; t1.cardio.incline = "2"
    t2.cardio!!.time = "20"; t2.cardio.speed = "3"; t2.cardio.incline = "5"
    val draft = ActiveDraft("s", "2026-11-13", listOf(t1, a, b, t2).map { it.draft() })
    val restored = Json.decodeFromString<ActiveDraft>(Json.encodeToString(ActiveDraft.serializer(), draft)).exercises.map { it.restore() }
    assertEquals(4, restored.map { it.uid }.distinct().size)
    assertEquals(listOf(0, 2), restored[1].sethStarts.toList()); assertEquals("pair", restored[2].superSetId)
    restored[0].cardio!!.time = "12"; assertEquals("20", restored[3].cardio!!.time)
    assertEquals("5", restored[3].cardio!!.incline)
    val old = """{"sessionId":"s","workoutDate":"2026-11-13","exercises":[]}"""
    assertTrue(Json.decodeFromString<ActiveDraft>(old).exercises.isEmpty())
  }
  @Test fun historyAndSingleImageKeepEveryOccurrenceAndActualCompletionOrder() {
    val session = CompletedSessionEntity("s", "u", "Workout", workoutDate = "2026-11-13")
    val entries = listOf(
      SetLog(1, 10, "treadmill", 1, 0, 0.0, instanceId = "before", cardio = CardioMetrics(10.0, speed = 2.7, incline = 2.0)),
      SetLog(2, 20, "curl", 1, 8, 40.0, instanceId = "curl", superSetId = "pair", sethRound = "round", sethPart = 1),
      SetLog(3, 30, "press", 1, 10, 50.0, instanceId = "press", superSetId = "pair"),
      SetLog(4, 40, "curl", 2, 13, 17.5, instanceId = "curl", sethRound = "round", sethPart = 2),
      SetLog(5, 50, "treadmill", 1, 0, 0.0, instanceId = "after", cardio = CardioMetrics(20.0, speed = 3.0, incline = 5.0)))
    val card = workoutShareCard(session, entries.reversed()) { it }
    assertEquals(listOf("treadmill", "curl", "press", "curl", "treadmill"), card.rows.map { it.first })
    assertTrue(card.caption.startsWith("#GymClues Workout Completed:"))
    assertTrue(card.rows.first().second.contains("2.7 mph")); assertTrue(card.rows.last().second.contains("5% incline"))
    assertTrue(card.caption.contains("17.5 lb x 13")); assertTrue(card.caption.contains("SUPER SET")); assertTrue(card.caption.contains("SETH SET"))
    assertFalse(card.caption.contains("total", true))
    val image = renderShareCard(ctx, card); assertEquals(1080, image.width); assertTrue(image.height >= 1350)
    System.getenv("TECHNIQUE_PREVIEWS")?.let { dir -> val f = java.io.File(dir); f.mkdirs()
      f.resolve("share-image.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) } }
    image.recycle()
  }
  @Test fun additiveMigrationKeepsLegacyMeasurementsWithoutInventingIdentityOrDates() {
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDb::class.java)
    helper.createDatabase("techniques-migration.db", 8).apply {
      execSQL("INSERT INTO SetLog(date,exerciseId,setNo,reps,weight,units,notes,sessionId,gymId) VALUES(123,'curl',1,8,40,'kg','','s','')"); close()
    }
    helper.runMigrationsAndValidate("techniques-migration.db", 9, true, MIGRATION_8_9).apply {
      query("SELECT date,weight,units,instanceId,superSetId,sethRound FROM SetLog").use {
        assertTrue(it.moveToFirst()); assertEquals(123L, it.getLong(0)); assertEquals(40.0, it.getDouble(1), 0.0)
        assertEquals("kg", it.getString(2)); (3..5).forEach { col -> assertEquals("", it.getString(col)) }
      }; close()
    }
  }
  @Test fun templatesAndLogsPersistAndRejectInvalidRelationships() = runBlocking {
    val db = Room.databaseBuilder(ctx, GymDb::class.java, "techniques-reopen.db").build()
    val catalog = CatalogRepository(ctx)
    val user = UserSession(ctx, db.dao()); val repo = WorkoutRepository(db.dao(), catalog, user)
    val curl = catalog.exercises.first { it.tracking == "strength" }.id
    val press = catalog.exercises.first { it.tracking == "strength" && it.id != curl }.id
    val cardio = catalog.exercises.first { it.timedCardio }.id
    val items = listOf(TemplateItem(cardio, 1, "10", 0), TemplateItem(curl, 4, "8", 90, superSetId = "p", sethStarts = listOf(0, 2)),
      TemplateItem(press, 3, "10", 90, superSetId = "p"), TemplateItem(cardio, 1, "20", 0))
    val id = repo.saveCustom(null, "Techniques", items)
    val beforeId = repo.logSet(SetLog(date = 1, exerciseId = cardio, setNo = 1, reps = 0, weight = 0.0,
      instanceId = "before", cardio = CardioMetrics(10.0, speed = 2.7, incline = 2.0)))
    val afterId = repo.logSet(SetLog(date = 3, exerciseId = cardio, setNo = 1, reps = 0, weight = 0.0,
      instanceId = "after", cardio = CardioMetrics(20.0, speed = 3.0, incline = 5.0)))
    repo.updateSet(repo.history().first().first { it.id == beforeId }.copy(cardio = CardioMetrics(12.0)))
    assertEquals(20.0, repo.history().first().first { it.id == afterId }.cardio!!.minutes, 0.0)
    repo.deleteSet(beforeId); assertEquals(1, repo.history().first().size)
    repo.deleteSet(afterId)
    val set = SetLog(date = 11, exerciseId = curl, setNo = 1, reps = 8, weight = 40.0, instanceId = "one", superSetId = "p", superSetPart = 1, sethRound = "r", sethPart = 1)
    val logId = repo.logSet(set)
    repo.updateSet(set.copy(id = logId, weight = 42.0, superSetId = "", sethRound = ""))
    assertEquals("p", repo.history().first().single().superSetId)
    assertEquals(1, repo.history().first().single().superSetPart)
    try { repo.saveCustom(null, "Invalid", items.drop(2)); fail("Invalid group accepted") } catch (_: IllegalArgumentException) { }
    try { repo.saveCustom(null, "Invalid", listOf(items[1].copy(superSetId = "", sethStarts = listOf(0, 1)))); fail("Overlapping sets accepted") } catch (_: IllegalArgumentException) { }
    db.close()
    val reopened = Room.databaseBuilder(ctx, GymDb::class.java, "techniques-reopen.db").build()
    val again = WorkoutRepository(reopened.dao(), catalog, UserSession(ctx, reopened.dao()))
    val plan = again.plan("custom", id)!!
    assertEquals(listOf(cardio, curl, press, cardio), plan.items.map { it.exerciseId }); assertEquals(listOf(0, 2), plan.items[1].sethStarts)
    assertEquals(4, again.customItems(id).map { it.id }.distinct().size)
    assertEquals(42.0, again.history().first().single().weight, 0.0)
    reopened.close()
  }
}
