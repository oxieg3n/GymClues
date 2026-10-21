package com.gymclues.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.first
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-mdpi")
class TrainingGuideTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private val library get() = TrainingGuideLibrary.load(ctx)
  private val containers = mutableListOf<AppContainer>()
  private fun app() = AppContainer(ctx).also { containers += it }
  private val output get() = System.getenv("TRAINING_GUIDE_PREVIEWS")?.let(::File)?.apply { mkdirs() }

  @After fun cleanup() {
    compose.activity.setContent { }
    compose.waitForIdle()
    runBlocking {
      containers.forEach { app ->
        val f = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }
        (f.get(app) as CoroutineScope).coroutineContext[Job]?.cancelAndJoin()
      }
    }
    containers.forEach { it.db.close() }
    GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
  }

  @Test fun equipmentGroupsCoverTheCatalogOnceInAStableUsefulOrder() {
    val catalog = CatalogRepository(ctx)
    val groups = catalog.equipmentGroups
    assertEquals(listOf(EquipmentType.CARDIO, EquipmentType.UPPER_BODY, EquipmentType.LOWER_BODY,
      EquipmentType.CORE, EquipmentType.CABLE, EquipmentType.SMITH, EquipmentType.FREE_WEIGHTS, EquipmentType.BENCHES), groups.keys.toList())
    val items = groups.values.flatten()
    assertEquals(35, items.size)
    assertEquals(catalog.content.equipment.map { it.id }.toSet(), items.map { it.id }.toSet())
    assertEquals(items.size, items.map { it.id }.distinct().size)
    assertEquals(groups, equipmentGroups(catalog.content.equipment.reversed()))
    groups.values.forEach { rows -> assertEquals(rows.map { it.name.lowercase() }.sorted(), rows.map { it.name.lowercase() }) }
    assertEquals(EquipmentType.BENCHES, catalog.equipment("eq_x_45_degree_extension_bench")!!.machineType)
    assertEquals(EquipmentType.LOWER_BODY, catalog.equipment("eq_leg_press")!!.machineType)
    assertEquals(EquipmentType.CORE, catalog.equipment("eq_x_lumbar_extension_machine")!!.machineType)
    assertEquals(EquipmentType.OTHER_STRENGTH, equipmentType("new_machine", "Selectorized"))
    assertEquals(EquipmentType.OTHER, equipmentType("new_machine", ""))
    assertTrue(equipmentGroups(emptyList()).isEmpty())
  }

  @Test fun groupedEquipmentKeepsAvailabilitySwitchesAndGuideNavigation() {
    val app = app()
    compose.setContent { GymCluesTheme { Surface {
      val nav = rememberNavController()
      val off by app.dao.unavailable().collectAsState(emptyList())
      androidx.navigation.compose.NavHost(nav, startDestination = "equipment") {
        composable("equipment") { EquipmentScreen(app, off.map { it.equipmentId }.toSet(), nav) }
        composable("equip/{id}") { entry ->
          EquipmentDetail(app, entry.arguments!!.getString("id")!!, null, nav)
        }
      }
    } } }
    compose.onNodeWithText("Cardio machines").assertIsDisplayed()
    compose.onNodeWithText("Exercise bike").assertIsDisplayed()
    val bikeSwitch = hasAnyAncestor(hasText("Exercise bike")) and isToggleable()
    compose.onNode(bikeSwitch).assertIsOn().performClick()
    compose.waitUntil(10_000) { kotlinx.coroutines.runBlocking { app.dao.unavailable().first().any { it.equipmentId == "eq_cardio_bike" } } }
    compose.waitUntil(10_000) { compose.onNode(bikeSwitch).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] == androidx.compose.ui.state.ToggleableState.Off }
    compose.onNode(bikeSwitch).assertIsOff().performClick()
    compose.waitUntil(10_000) { kotlinx.coroutines.runBlocking { app.dao.unavailable().first().none { it.equipmentId == "eq_cardio_bike" } } }
    compose.waitUntil(10_000) { compose.onNode(bikeSwitch).fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.ToggleableState] == androidx.compose.ui.state.ToggleableState.On }
    snapshot("equipment-grouped.png")
    compose.onNodeWithText("Exercise bike").performClick()
    compose.onNodeWithText("Equipment setup guide").assertExists()
  }

  @Test fun everyEquipmentImageLoadsAndEveryCatalogExerciseHasCoachingAndSetups() {
    val catalog = CatalogRepository(ctx)
    assertEquals(35, catalog.content.equipment.size)
    assertEquals(catalog.content.equipment.map { it.id }.toSet(), library.equipment.keys)
    catalog.content.equipment.forEach { q ->
      val guide = library.equipment.getValue(q.id)
      assertEquals(4, guide.steps.size)
      val id = ctx.resources.getIdentifier(guide.image, "drawable", ctx.packageName)
      assertNotEquals("Missing setup image for ${q.name}", 0, id)
      val bitmap = Bitmap.createBitmap(640, 400, Bitmap.Config.ARGB_8888)
      ctx.getDrawable(id)!!.apply { setBounds(0, 0, 640, 400); draw(Canvas(bitmap)) }
      val pixels = IntArray(640 * 400)
      bitmap.getPixels(pixels, 0, 640, 0, 0, 640, 400)
      assertTrue("Unrendered callouts: ${q.name}", pixels.any { it == 0xFFFFD18A.toInt() })
      output?.resolve("${guide.image}.png")?.outputStream()?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()
    }
    assertEquals(298, catalog.content.exercises.size)
    catalog.content.exercises.forEach { e ->
      assertTrue("Missing specific safety for ${e.name}", library.variants(e.name).isNotEmpty() || e.guideKind.isNotBlank())
      assertTrue(e.safetyTips.size >= 2 && e.injuryPrevention.size >= 2 && e.mistakes.size >= 3)
      assertTrue(e.mistakes.take(3).all { ':' in it })
      e.equipmentIds.forEach { id ->
        val setup = catalog.setupFor(e.id, id)
        assertNotNull("Missing exercise equipment configuration: ${e.name} / $id", setup)
        assertTrue(setup!!.position.isNotBlank())
        assertTrue(id in library.equipment)
      }
    }
    assertTrue(catalog.setupFor("ex_x_cable_curl", "eq_x_cable")!!.position.startsWith("Low pulley"))
    assertTrue(catalog.setupFor("ex_x_cable_rope_pushdown", "eq_x_cable")!!.position.startsWith("High pulley"))
    assertTrue(catalog.setupFor("ex_x_incline_dumbbell_press", "eq_adjustable_bench")!!.angle.contains("incline"))
    assertTrue("eq_adjustable_bench" in catalog.exercise("ex_smith_bench")!!.equipmentIds)
  }

  @Test fun everyProgramPrescriptionHasSafetyIncludingWarmupsAndMovementChoices() {
    val programs = ctx.assets.open("programs.json").bufferedReader().use {
      Json.decodeFromString<ProgramLibrary>(it.readText()).programs
    }
    programs.flatMap { it.days }.flatMap { it.items }.forEach { r ->
      val variants = library.variants(r.exercise)
      assertTrue("Missing program safety: ${r.exercise}", variants.isNotEmpty())
      variants.forEach { v ->
        assertNotEquals("general", v.profile)
        val p = library.profiles.getValue(v.profile)
        assertTrue(p.safetyTips.size >= 2 && p.injuryPrevention.size >= 2 && p.mistakes.size >= 3)
      }
    }
    assertEquals(2, library.variants("Pull Ups or Machine Rows").size)
    assertEquals(3, library.variants("Pulldown or pull-up or chin-up").size)
    assertTrue(library.safetyFor("unknown custom lift").mistakes.isNotEmpty())
    assertTrue(library.equipment.getValue("eq_x_assisted_pull_up_machine").steps.first().text.contains("more assistance"))
  }

  @Test fun equipmentPageShowsDiagramAndZoomControls() {
    val app = app()
    compose.setContent { GymCluesTheme { Surface { EquipmentDetail(app, "eq_x_cable", null, rememberNavController()) } } }
    compose.onNodeWithText("Equipment setup guide").assertExists()
    compose.onNodeWithContentDescription("Numbered equipment setup diagram for Cable Station").assertExists()
    snapshot("cable-setup-page.png")
    compose.onNodeWithText("Enlarge setup diagram").performScrollTo().performClick()
    compose.onNodeWithContentDescription("Zoom in setup diagram").performClick()
    snapshot("cable-setup-zoom.png")
    compose.onNodeWithText("Reset").performClick()
    compose.onNodeWithContentDescription("Close training guide").performClick()
    compose.onNodeWithText("Equipment setup guide").assertExists()
  }

  @Test fun exercisePageShowsSafetyPreventionAndCorrections() {
    val app = app()
    compose.setContent { GymCluesTheme { Surface {
      ExerciseDetail(app, app.catalog.exercise("ex_x_cable_curl")!!, emptySet(), rememberNavController())
    } } }
    compose.onNodeWithText("Safety tips").performScrollTo().assertIsDisplayed()
    snapshot("exercise-safety.png")
    compose.onNodeWithText("Injury prevention").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("Common mistakes & corrections").performScrollTo().assertIsDisplayed()
    snapshot("exercise-corrections.png")
  }

  @Test fun programOnlyExerciseOpensSafetyAndEquipmentGuide() {
    val app = app()
    compose.setContent { GymCluesTheme { Surface { ProgramDayScreen(app, "B1_1", null, rememberNavController()) } } }
    compose.onNodeWithContentDescription("Safety guide for Deadlift").performClick()
    compose.onNodeWithText("Safety & common mistakes").assertIsDisplayed()
    snapshot("program-safety.png")
    compose.onNodeWithText("Barbell and Rack").performScrollTo().performClick()
    compose.onNodeWithText("Equipment setup").assertIsDisplayed()
    snapshot("rack-setup-popup.png")
    compose.onAllNodesWithContentDescription("Close training guide").onLast().performClick()
    compose.onNodeWithContentDescription("Close training guide").performClick()
    compose.onNodeWithText("Start workout").assertIsDisplayed()
  }

  @Test fun safetyMovementChoicesUseDistinctGuides() {
    compose.setContent { GymCluesTheme { ExerciseSafetyButton("Pull Ups or Machine Rows") } }
    compose.onNodeWithContentDescription("Safety guide for Pull Ups or Machine Rows").performClick()
    compose.onNodeWithText("Pull-up").assertExists()
    compose.onNodeWithText("Machine row").performClick()
    compose.onNodeWithText(library.profiles.getValue("supported_row").safetyTips.first()).assertExists()
    compose.onNodeWithContentDescription("Close training guide").performClick()
  }

  @Test fun workoutGuideLinkOpensTheNewLibraryPage() {
    val app = app()
    compose.setContent { GymCluesTheme { Surface {
      val nav = rememberNavController()
      androidx.navigation.compose.NavHost(nav, startDestination = "day") {
        composable("day") { ProgramDayScreen(app, "B1_1", null, nav) }
        composable("ex/{id}") { entry ->
          ExerciseDetail(app, app.catalog.exercise(entry.arguments!!.getString("id")!!)!!, emptySet(), nav)
        }
      }
    } } }
    compose.onAllNodesWithText("Exercise guide").onFirst().performClick()
    compose.onNodeWithText("Deadlift").assertIsDisplayed()
    compose.onNodeWithText("Setup").performScrollTo().assertIsDisplayed()
    compose.onNodeWithText("How to do it").performScrollTo().assertIsDisplayed()
    snapshot("new-library-page.png")
  }

  private fun snapshot(name: String) {
    val out = output ?: return
    compose.waitForIdle()
    compose.runOnIdle {
      val view = android.view.inspector.WindowInspector.getGlobalWindowViews().last()
      val bmp = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      view.draw(Canvas(bmp))
      out.resolve(name).outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bmp.recycle()
    }
  }
}
