package com.gymclues.app
import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlinx.coroutines.cancelAndJoin
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class RegressionTests {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()

  private val containers = mutableListOf<AppContainer>()
  private fun newApp() = AppContainer(ctx).also { containers += it }

  @org.junit.After fun cleanup() {
    compose.activity.setContent { }
    compose.waitForIdle()
    kotlinx.coroutines.runBlocking { containers.forEach { app ->
      val scope = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }.get(app) as kotlinx.coroutines.CoroutineScope
      scope.coroutineContext[kotlinx.coroutines.Job]?.cancelAndJoin()
    } }
    containers.forEach { it.db.close() }
    GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
  }


  /** Completed Sessions starts empty, only lists workouts the user finished, and survives sets for exercises missing from the catalog. */
  @Test fun completedSessionsListsOnlyFinishedWorkouts() {
    val app = newApp()
    compose.setContent { val nav = androidx.navigation.compose.rememberNavController(); CompletedSessions(app, nav) }
    compose.onNodeWithText("No completed sessions yet").assertExists()
    runBlocking {
      app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 10, weight = 100.0, sessionId = "s1"))
      app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_deleted_from_catalog", setNo = 1, reps = 5, weight = 50.0, sessionId = "s1"))
      app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 8, weight = 90.0, sessionId = "unfinished"))
      app.workouts.completeSession("s1", "Leg day", "") }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Leg day").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("2 exercises, 2 entries").assertExists()
    compose.onNodeWithText("No completed sessions yet").assertDoesNotExist()
  }

  /** The detail page shows each set with weight and reps and offers Share. */
  @Test fun sessionDetailShowsSetsAndShare() {
    val app = newApp()
    runBlocking {
      app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 10, weight = 100.0, sessionId = "s2"))
      app.workouts.completeSession("s2", "Push day", "") }
    compose.setContent { val nav = androidx.navigation.compose.rememberNavController(); SessionDetail(app, "s2", nav) }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Push day").fetchSemanticsNodes().isNotEmpty() }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Set 1: 100 lb x 10").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Share").performClick()
    compose.onNodeWithText("Share your workout").assertExists()
    compose.onNodeWithText("Copy Post Text").performScrollTo().performClick()
    val copied = (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager).primaryClip
      ?.getItemAt(0)?.coerceToText(ctx).toString()
    assertTrue(copied!!.startsWith("#GymClues Workout Completed:"))
  }

  @Test fun savedAssignedWorkoutDisablesRestartAndOffersAnotherWorkout() {
    val app = newApp()
    val program = app.programs.programs.first { p -> p.days.any { it.training } }
    val day = program.days.first { it.training }
    val plan = "${program.id}_${day.day}"
    val date = java.time.LocalDate.now().toString()
    runBlocking { app.workouts.completeSession("first", "${program.name} - ${day.session}", "", date, plan, program.name, day.session) }
    compose.setContent { val nav = androidx.navigation.compose.rememberNavController(); ProgramDayScreen(app, plan, date, nav) }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Completed").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Completed").assertIsNotEnabled()
    compose.onNodeWithText("Start a 2nd Workout").assertExists()
  }

  @Test fun customCardioIsPrivateAndReusable() = runBlocking {
    val app = newApp()
    val name = "Test cardio ${newId()}"
    val (error, exercise) = app.exercises.createCardio(name, "bike")
    assertNull(error)
    assertEquals("bike", exercise?.tracking)
    assertEquals(app.session.userId, app.exercises.mine().first().first { it.id == exercise!!.id }.ownerId)
  }

  @Test fun secondWorkoutKeepsFirstSessionAndItsCardio() = runBlocking {
    val app = newApp()
    val date = java.time.LocalDate.now().toString()
    val first = newId(); val second = newId()
    app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_bike", setNo = 1, reps = 0, weight = 0.0,
      sessionId = first, cardio = CardioMetrics(10.0, distance = 2.0)))
    app.workouts.completeSession(first, "Scheduled", "", date, "p_1")
    app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 10, weight = 50.0, sessionId = second))
    app.workouts.completeSession(second, "Additional", "", date)
    assertEquals(2, app.workouts.sessions().first().count { it.id == first || it.id == second })
    assertEquals(1, app.workouts.history().first().count { it.sessionId == first && it.cardio?.distance == 2.0 })
  }

  /** v1 database (with history + an old custom workout) upgrades to v2 without losing data. */
  @Test fun migrationKeepsHistory() {
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDb::class.java)
    helper.createDatabase("mig.db", 1).apply {
      execSQL("INSERT INTO SetLog(date,exerciseId,setNo,reps,weight,units,notes) VALUES(1,'ex_leg_press',1,10,100,'lb','')")
      execSQL("INSERT INTO CustomWorkout(name,exerciseIdsCsv) VALUES('Old one','ex_leg_press,ex_chest_press')")
      close() }
    val db = helper.runMigrationsAndValidate("mig.db", 6, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
    db.query("SELECT COUNT(*) FROM SetLog").use { it.moveToFirst(); assertEquals(1, it.getInt(0)) }
    db.query("SELECT COUNT(*) FROM workout_items").use { it.moveToFirst(); assertEquals(2, it.getInt(0)) }
    db.query("SELECT COUNT(*) FROM completed_sessions").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
    db.query("SELECT COUNT(*) FROM rest_days").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
    db.query("SELECT COUNT(*) FROM program_assignments").use { it.moveToFirst(); assertEquals(0, it.getInt(0)) }
    db.close()
  }

  /** Sessions can be renamed, their sets edited, and deleted (sets go too). */
  @Test fun sessionEditAndDelete() = runBlocking {
    val app = newApp(); val w = app.workouts
    w.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 10, weight = 100.0, sessionId = "s3"))
    w.completeSession("s3", "Old name", "")
    w.renameSession("s3", "New name")
    val set = w.history().first().single { it.sessionId == "s3" }
    w.updateSet(set.copy(weight = 120.0, reps = 8))
    assertEquals("New name", w.sessions().first().single { it.id == "s3" }.name)
    assertEquals("120 lb x 8", setLabel(w.history().first().single { it.sessionId == "s3" }))
    w.deleteSession("s3")
    assertTrue(w.sessions().first().none { it.id == "s3" })
    assertTrue(w.history().first().none { it.sessionId == "s3" })
  }

  /** Edit mode on the detail page shows the save control. */
  @Test fun sessionDetailEditMode() {
    val app = newApp()
    runBlocking { app.workouts.logSet(SetLog(date = now(), exerciseId = "ex_leg_press", setNo = 1, reps = 10, weight = 100.0, sessionId = "s4"))
      app.workouts.completeSession("s4", "Pull day", "") }
    compose.setContent { val nav = androidx.navigation.compose.rememberNavController(); SessionDetail(app, "s4", nav) }
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Edit").fetchSemanticsNodes().isNotEmpty() }
    compose.onNodeWithText("Edit").performClick()
    compose.onNodeWithText("Save changes").assertExists()
  }

  /** Removing every exercise and the minimum-set rule never crash the active workout. */
  @Test fun activeWorkoutRemoveAllAndMinimumSets() {
    val app = newApp()
    compose.setContent { val nav = androidx.navigation.compose.rememberNavController()
      NavHost(nav, "w") { composable("w") {
        ActiveWorkout(app, "template", "t_fullbody_1", emptySet(), nav) {} } } }
    compose.waitForIdle()
    // Set minus is disabled once only one set remains (template has 2)
    compose.onAllNodesWithContentDescription("Remove set")[0].performClick()
    compose.onAllNodesWithContentDescription("Remove set")[0].assertIsNotEnabled()
    compose.onAllNodesWithContentDescription("Add set")[0].performClick()
    repeat(3) { compose.onAllNodes(hasContentDescription("Remove ", substring = true) and hasClickAction() and !hasContentDescription("Remove set"))[0].performClick(); compose.waitForIdle() }
    compose.onNodeWithText("No exercises in this workout.").assertExists()
  }

  @Test fun customWorkoutSavesAndLoads() = runBlocking {
    val app = newApp(); app.session.ensureUser()
    val id = app.workouts.saveCustom(null, "My Push", listOf(TemplateItem("ex_chest_press", 3, "10", 60), TemplateItem("ex_db_incline_press", 2, "8-10", 90)))
    val plan = app.workouts.plan("custom", id)!!
    assertEquals("My Push", plan.name); assertEquals(2, plan.items.size); assertEquals("ex_db_incline_press", plan.items[1].exerciseId)
    assertNull(app.workouts.plan("template", "does_not_exist"))
  }

  @Test fun enrichmentFillsOnlyIdentifiedFieldsAndLeavesRestBlank() = runBlocking {
    val app = newApp()
    val known = app.gyms.submitEquipment(GymEquipmentEntity(gymId = "g", name = "Seated chest press", submittedBy = "u"))
    assertEquals("eq_chest_press", known.catalogEquipmentId); assertEquals("Selectorized", known.category)
    assertTrue(known.primaryMuscles.contains("Chest")); assertEquals("", known.model)
    val unknown = app.gyms.submitEquipment(GymEquipmentEntity(gymId = "g", name = "Mystery Machine 3000", submittedBy = "u"))
    assertEquals("", unknown.catalogEquipmentId); assertEquals("", unknown.category); assertTrue(unknown.primaryMuscles.isEmpty())
  }

  @Test fun adminPinRulesAndLockout() {
    val auth = com.gymclues.app.auth.AdminAuth(ctx)
    assertFalse(auth.setPin("12345")); assertFalse(auth.setPin("12a456")); assertTrue(auth.setPin("482916"))
    auth.lock(); assertTrue(auth.verify("482916") is com.gymclues.app.auth.AdminAuth.Result.Ok)
    auth.lock(); repeat(4) { assertTrue(auth.verify("000000") is com.gymclues.app.auth.AdminAuth.Result.Wrong) }
    assertTrue(auth.verify("000000") is com.gymclues.app.auth.AdminAuth.Result.Locked)
    assertTrue(auth.verify("482916") is com.gymclues.app.auth.AdminAuth.Result.Locked)  // correct PIN still blocked during lockout
  }

  @Test fun youtubeLinksAreRecognised() {
    assertEquals("dQw4w9WgXcQ", youtubeId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
    assertEquals("dQw4w9WgXcQ", youtubeId("https://youtu.be/dQw4w9WgXcQ"))
    assertNull(youtubeId("https://planetfitness.com/some-page"))
  }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class WorkoutStatsTest {
  private val z = java.time.ZoneId.of("America/New_York")
  private fun at(d: java.time.LocalDate) = d.atTime(12, 0).atZone(z).toInstant().toEpochMilli()
  private val today = java.time.LocalDate.of(2026, 10, 14) // Wednesday
  @Test fun countsAndDayStreak() {
    val logs = listOf(today, today, today.minusDays(1), today.minusDays(2), today.minusDays(4), today.minusMonths(2), today.minusYears(1)).map(::at)
    val s = workoutStats(logs, today, z, java.time.DayOfWeek.SUNDAY)
    assertEquals(3, s.streak)               // today, yesterday, day before; gap on day 3 ends it
    assertEquals(3, s.week); assertEquals(4, s.month); assertEquals(5, s.year)
    assertTrue(today in s.doneDays)
  }
  @Test fun streakSurvivesUntilDayEnds() {
    assertEquals(2, workoutStats(listOf(today.minusDays(1), today.minusDays(2)).map(::at), today, z).streak)
    assertEquals(0, workoutStats(listOf(today.minusDays(2)).map(::at), today, z).streak)
    assertEquals(0, workoutStats(emptyList(), today, z).streak)
  }
}

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class ProgramTests {
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()

  private val containers = mutableListOf<AppContainer>()
  private fun newApp() = AppContainer(ctx).also { containers += it }

  @org.junit.After fun cleanup() {
    compose.activity.setContent { }
    compose.waitForIdle()
    kotlinx.coroutines.runBlocking { containers.forEach { app ->
      val scope = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }.get(app) as kotlinx.coroutines.CoroutineScope
      scope.coroutineContext[kotlinx.coroutines.Job]?.cancelAndJoin()
    } }
    containers.forEach { it.db.close() }
    GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
  }

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Test fun assetsLoadAndKeepOriginalPrescriptions() {
    val app = newApp(); val ps = app.programs.programs
    assertEquals(24, ps.size); assertEquals(8, ps.groupBy { it.split }.size); assertTrue(ps.filter { it.split != "HIIT" }.all { it.description.length > 200 })
    val p3 = ps.first { it.id == "P3" }
    assertEquals(8, p3.cycleLength); assertEquals(8, p3.days.size)
    val bench = p3.days[0].items.first { it.exercise == "Flat barbell bench press" }
    assertEquals("5", bench.sets); assertEquals("15", bench.reps); assertTrue(bench.repsNote.startsWith("TOTAL"))
    assertEquals(675, ps.filter { it.split != "HIIT" }.sumOf { p -> p.days.sumOf { it.items.size } })
    assertTrue(app.catalog.exercises.size >= 100 && app.catalog.exercise("ex_x_cable_curl") != null)
    assertTrue(app.catalog.exercises.all { e -> e.equipmentIds.all { app.catalog.equipment(it) != null } })
  }

  private fun days(vararg n: Int) = n.map { java.time.LocalDate.of(2026, 10, it) }.toSet()
  private val a = listOf(ProgramAssignmentEntity(programId = "B1", startDate = "2026-10-05", ownerId = "u"))   // Mon Back, Tue Chest, Wed Legs, Thu Shoulders, Fri Arms, Sat/Sun rest
  private val today = java.time.LocalDate.of(2026, 10, 14)

  @Test fun programStreakSkipsRestAndCountsTrainingDays() {
    val ps = newApp().programs.programs
    fun st(done: Set<java.time.LocalDate>, rest: RestRules = RestRules.NONE) = programStreak(done, ps, a, rest, today)
    assertEquals(7, st(days(5, 6, 7, 8, 9, 12, 13)))                    // weekend (cycle rest) does not break it; today not logged yet
    assertEquals(8, st(days(5, 6, 7, 8, 9, 12, 13, 14)))                // today counts once logged
    assertEquals(4, st(days(5, 6, 8, 9, 12, 13)))                       // missed Wed the 7th: streak is 13, 12, 9, 8
    val off = RestRules(listOf(RestDayEntity(ownerId = "u", weekday = 0, startDate = "2026-10-13", endDate = "2026-10-14")))
    assertEquals(6, st(days(5, 6, 7, 8, 9, 12), off))                   // user rest day on the 13th is skipped, not a miss
    assertNull(programStreak(days(13), ps, emptyList(), RestRules.NONE, today))   // no program: caller falls back to gym days
  }

  @Test fun programProgressCountsScheduledSessions() {
    val ps = newApp().programs.programs
    val p = programProgress(days(5, 6, 7, 8, 9, 12, 13, 11), ps, a, RestRules.NONE, today)!!   // the 11th is a rest day: not counted
    assertEquals(7, p.done); assertEquals(50, p.total); assertEquals(14, p.pct); assertFalse(p.complete)
    assertTrue(ProgramProgress(ps[0], 50, 50).complete); assertEquals(100, ProgramProgress(ps[0], 60, 50).pct)
    assertNull(programProgress(emptySet(), ps, emptyList(), RestRules.NONE, today))
  }

  @Test fun scheduleUsesCycleLengthAndNewestAssignment() {
    val ps = newApp().programs.programs
    val a = ProgramAssignmentEntity(programId = "P3", startDate = "2026-10-14", ownerId = "u")
    fun day(d: String) = scheduledDay(ps, listOf(a), java.time.LocalDate.parse(d))?.day
    assertEquals("Push A", day("2026-10-14")!!.session)
    assertFalse(day("2026-10-17")!!.training)               // cycle day 4 = rest
    assertEquals("Push B", day("2026-10-18")!!.session)     // day 5
    assertEquals("Push A", day("2026-10-22")!!.session)     // wraps after 8 days
    assertNull(day("2026-10-13"))                           // before start
    val b = ProgramAssignmentEntity(programId = "B1", startDate = "2026-10-20", ownerId = "u")
    assertEquals("P3", scheduledDay(ps, listOf(a.copy(endDate = "2026-10-20"), b), java.time.LocalDate.parse("2026-10-19"))!!.program.id)
    assertEquals("Back", scheduledDay(ps, listOf(a.copy(endDate = "2026-10-20"), b), java.time.LocalDate.parse("2026-10-20"))!!.day.session)
  }

  @Test fun assignPersistsAndReplacesOldProgram() = runBlocking {
    val app = newApp()
    app.programs.assign("B1", java.time.LocalDate.now()); app.programs.assign("P1", java.time.LocalDate.now().plusDays(1))
    val l = app.dao.assignmentsOnce(app.session.userId)
    assertEquals(2, l.size); assertEquals(java.time.LocalDate.now().plusDays(1).toString(), l.first { it.programId == "B1" }.endDate)
    assertTrue(app.dao.outbox().count { it.entityType == "program_assignment" } >= 3)
  }

  @Test fun userExerciseSavesValidatesAndJoinsCatalog() = runBlocking {
    val app = newApp()
    assertNotNull(app.exercises.save(null, " ", listOf("Chest"), emptyList(), "", emptyList(), ""))
    assertNotNull(app.exercises.save(null, "Cable curl", listOf("Biceps"), emptyList(), "", emptyList(), ""))   // duplicate of library
    assertNull(app.exercises.save(null, "My alternating curl", listOf("Biceps"), listOf("eq_dumbbells"), "Pull", listOf("Curl up", "Rotate"), ""))
    val e = app.dao.customExercises(app.session.userId).first().single()
    assertEquals(Visibility.PRIVATE, e.visibility); assertEquals(SyncState.PENDING, e.syncState)
    assertTrue(app.dao.outbox().any { it.entityType == "exercise" && it.entityId == e.id })
  }

  @Test fun restDaysShiftTheProgram() {
    val ps = newApp().programs.programs
    val a = listOf(ProgramAssignmentEntity(programId = "B1", startDate = "2026-10-12", ownerId = "u"))   // Mon: Back, Tue Chest, Wed Legs, Thu Shoulders, Fri Arms
    fun rd(w: Int, s: String, e: String = "") = RestDayEntity(ownerId = "u", weekday = w, startDate = s, endDate = e)
    fun on(rules: List<RestDayEntity>, d: String) = scheduledDay(ps, a, java.time.LocalDate.parse(d), RestRules(rules))
    // one-off rest on Tue 13th: Chest slides to Wed, everything after moves a day
    val one = listOf(rd(0, "2026-10-13", "2026-10-14"))
    assertTrue(on(one, "2026-10-13")!!.userRest)
    assertEquals("Chest & Abs", on(one, "2026-10-14")!!.day.session)
    assertEquals("Back", on(one, "2026-10-12")!!.day.session)
    // every Friday off from the start: Arms is pushed to Saturday, Friday shows rest
    val fri = listOf(rd(5, "2026-10-12"))
    assertTrue(on(fri, "2026-10-16")!!.userRest); assertEquals("Arms", on(fri, "2026-10-17")!!.day.session)
    // a weekly rule that ended keeps earlier dates unchanged
    val ended = listOf(rd(5, "2026-10-12", "2026-10-16"))
    assertEquals("Arms", on(ended, "2026-10-16")!!.day.session)
  }

  @Test fun restDayRepositoryRules() = runBlocking {
    val app = newApp(); val today = java.time.LocalDate.now()
    app.programs.addRestDate(today); app.programs.addRestDate(today)   // duplicate is ignored
    assertEquals(1, app.dao.restDaysOnce(app.session.userId).size)
    assertTrue(RestRules(app.dao.restDaysOnce(app.session.userId)).isRest(today))
    app.programs.removeRestDate(today)
    assertFalse(RestRules(app.dao.restDaysOnce(app.session.userId).filter { !it.deleted }).isRest(today))
    for (d in 1..6) assertTrue(app.programs.setWeekly(d, true))
    assertFalse(app.programs.setWeekly(7, true))                        // cannot block every day
    assertTrue(app.programs.setWeekly(1, false))
    assertTrue(app.programs.setWeekly(7, true))
  }

  @Test fun programDayWorkoutRunsWithUnmatchedExercisesAndLogsOnlyWorkingSets() {
    val app = newApp()
    compose.setContent { val nav = androidx.navigation.compose.rememberNavController()
      NavHost(nav, "w") { composable("w") { ActiveWorkout(app, "program", "B3_1", emptySet(), nav) {} } } }
    compose.waitForIdle()
    compose.onAllNodesWithText("Warm-up", substring = true)[0].assertExists()   // several warm-up rows now fit on screen
    compose.onNodeWithText("Child pose to cobra").assertExists()
  }
}
