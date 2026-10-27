package com.gymclues.app

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class) @Config(sdk = [34])
class FeatureUpdateTest {
  @Test fun dragScrollsOnlyAtVisibleEdges() {
    assertEquals(-24f, edgeScrollDelta(100f, 160f, 700f, 24f))
    assertEquals(0f, edgeScrollDelta(400f, 160f, 700f, 24f))
    assertEquals(24f, edgeScrollDelta(750f, 160f, 700f, 24f))
  }

  @Test fun assignedCompletionRequiresSavedMatchingSessionAndKeepsAdditionalSessionSeparate() {
    val date = LocalDate.of(2026, 10, 24)
    val day = ProgramDay(2, "Back", true, emptyList())
    val program = WorkoutProgram("p", "Program", "BRO", "Author", "Level", "Duration", "Description",
      "Schedule", "Progression", "Rest", "", 7, listOf(day), emptyList())
    val assigned = AssignedDay(program, day, date)
    val extra = CompletedSessionEntity("extra", "user", "Other workout", workoutDate = date.toString())
    val first = CompletedSessionEntity("first", "user", "Program - Back", workoutDate = date.toString(), planId = "p_2")
    assertFalse(assignedCompleted(assigned, listOf(extra)))
    assertTrue(assignedCompleted(assigned, listOf(first, extra)))
    assertTrue(assignedCompleted(assigned, listOf(first)))
    assertTrue(assignedCompleted(assigned, listOf(CompletedSessionEntity("old", "user", "Program - Back", workoutDate = date.toString()))))
  }

  @Test fun shareImageAndPostUseSameCompletionOrderAndMeasurements() {
    val session = CompletedSessionEntity("s", "u", "Program - Back", completedAt = 1792800000000,
      workoutDate = "2026-10-24", programName = "Program", dayName = "Back")
    val entries = listOf(
      SetLog(1, 1, "treadmill", 1, 0, 0.0, sessionId = "s", cardio = CardioMetrics(10.0, speed = 2.7, incline = 5.0)),
      SetLog(2, 2, "curl", 1, 25, 50.0, sessionId = "s"),
      SetLog(3, 3, "curl", 2, 15, 60.0, sessionId = "s"),
      SetLog(4, 4, "bike", 1, 0, 0.0, sessionId = "s", cardio = CardioMetrics(20.0, distance = 5.0)))
    val card = workoutShareCard(session, entries) { it }
    assertTrue(card.caption.startsWith("#GymClues Workout Completed:\n10/24/2026"))
    assertEquals(listOf("treadmill", "curl", "bike"), card.rows.map { it.first })
    assertTrue(card.caption.indexOf("treadmill") < card.caption.indexOf("curl"))
    assertTrue(card.caption.indexOf("curl") < card.caption.indexOf("bike"))
    assertTrue(card.caption.contains("2.7 mph"))
    assertTrue(card.caption.contains("Set 2 - 60 lb x 15"))
    assertTrue(card.rows[1].second.contains("Set 2: 60 lb x 15"))
    assertFalse(card.caption.contains("total", ignoreCase = true))
  }

  @Test fun draftRoundTripKeepsCardioDataAndDisplayOrder() {
    val cardio = SessionExercise("bike", null, "", 0, 1, tracking = "bike")
    cardio.cardio!!.time = "10:00"; cardio.cardio.distance = "2.5"; cardio.sets.first().logged = true
    val strength = SessionExercise("curl", null, "10", 90, 1)
    strength.sets.first().weight = "50"; strength.sets.first().reps = "10"
    val saved = ActiveDraft("session", "2026-10-24", listOf(cardio.draft(), strength.draft()))
    val restored = Json.decodeFromString<ActiveDraft>(Json.encodeToString(ActiveDraft.serializer(), saved)).exercises.map { it.restore() }
    assertEquals(listOf("bike", "curl"), restored.map { it.exerciseId })
    assertEquals("2.5", restored.first().cardio!!.distance)
    assertTrue(restored.first().sets.first().logged)
    assertEquals("50", restored.last().sets.first().weight)
  }

  @Test fun additiveMigrationKeepsCompletedSessionsAndCustomExercises() {
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GymDb::class.java)
    helper.createDatabase("feature-update.db", 7).apply {
      execSQL("INSERT INTO completed_sessions(id,ownerId,name,gymId,completedAt,updatedAt,syncState,deleted) VALUES('s','u','Workout','',1,1,'LOCAL',0)")
      execSQL("INSERT INTO custom_exercises(id,name,equipmentIds,primaryMuscles,movement,steps,safety,ownerId,visibility,status,createdAt,updatedAt,syncState,deleted) VALUES('e','Custom','[]','[]','Cardio','[]','','u','PRIVATE','DRAFT',1,1,'LOCAL',0)")
      close()
    }
    helper.runMigrationsAndValidate("feature-update.db", 8, true, MIGRATION_7_8).apply {
      query("SELECT name,workoutDate FROM completed_sessions WHERE id='s'").use { assertTrue(it.moveToFirst()); assertEquals("Workout", it.getString(0)); assertEquals("", it.getString(1)) }
      query("SELECT tracking FROM custom_exercises WHERE id='e'").use { assertTrue(it.moveToFirst()); assertEquals("strength", it.getString(0)) }
      close()
    }
  }
}
