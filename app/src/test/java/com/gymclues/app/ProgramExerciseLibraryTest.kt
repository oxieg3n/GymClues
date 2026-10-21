package com.gymclues.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ProgramExerciseLibraryTest {
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private fun read(name: String) = ctx.assets.open(name).bufferedReader().use { it.readText() }
  private val json = Json { ignoreUnknownKeys = true }
  private fun norm(s: String) = s.lowercase().replace(Regex("[^a-z0-9]"), "")

  @Test fun everyProgramNameHasAFullPageAndKeepsItsHistoricalReference() {
    val catalog = CatalogRepository(ctx)
    val original = listOf("content.json", "library.json").flatMap { json.decodeFromString<Content>(read(it)).exercises }
    val programs = json.decodeFromString<ProgramLibrary>(read("programs.json")).programs
    val names = programs.flatMap { it.days }.flatMap { it.items }.map { it.exercise }.distinct()
    assertEquals(395, names.size)
    assertEquals(298, catalog.exercises.size)
    original.forEach { assertNotNull(catalog.exercise(it.id)) }
    names.forEach { name ->
      val exercise = catalog.byName(name)
      assertNotNull("Missing page: $name", exercise)
      val oldId = original.firstOrNull { norm(it.name) == norm(name) }?.id ?: programExerciseId(name)
      assertEquals("Changed workout history reference: $name", oldId, catalog.programExerciseReference(name))
      assertEquals(oldId, catalog.exercise(oldId)?.id)
      assertTrue(exercise!!.safetyTips.isNotEmpty())
      if (exercise.guideKind.isNotBlank()) {
        assertTrue(exercise.setup.size >= 3 && exercise.steps.size >= 3)
        assertTrue(exercise.breathing.isNotBlank())
        assertTrue(MuscleMapLibrary.load(ctx).find(exercise.muscleMapName).isNotEmpty())
      }
    }
  }

  @Test fun groupsUsePrimaryMoversAndChoicePagesLinkToIndividualGuides() {
    val catalog = CatalogRepository(ctx)
    assertFalse("Back" in catalog.byName("Deadlift")!!.primaryMuscles)
    assertTrue(catalog.byName("Romanian Deadlift")!!.primaryMuscles.containsAll(listOf("Hamstrings", "Glutes")))
    assertEquals(listOf("Core"), catalog.byName("Plank")!!.primaryMuscles)
    assertEquals(catalog.muscles.sorted(), catalog.muscles)
    assertNotEquals(catalog.byName("Dumbbell bench press")!!.id, catalog.byName("Barbell bench press")!!.id)
    val choices = catalog.exercises.filter { it.choices.isNotEmpty() }
    assertTrue(choices.isNotEmpty())
    choices.forEach { e ->
      assertTrue(e.equipmentIds.isEmpty())
      e.choices.forEach { assertNotNull("Broken option: ${it.name}", catalog.exercise(it.exerciseId)) }
    }
  }
}
