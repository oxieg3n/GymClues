package com.gymclues.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.ui.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
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
class MuscleMapTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private val maps get() = MuscleMapLibrary.load(ctx)
  private val containers = mutableListOf<AppContainer>()
  private fun app() = AppContainer(ctx).also { containers += it }

  @After fun stopBackgroundCatalogObservers() {
    compose.activity.setContent { }
    compose.waitForIdle()
    runBlocking {
    // AppContainer owns a process-lifetime observer. Stop test-owned observers
    // before Robolectric resets SQLite, so they cannot query the next sandbox.
      containers.forEach { app ->
        val field = AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }
        (field.get(app) as CoroutineScope).coroutineContext[Job]?.cancelAndJoin()
      }
    }
    containers.forEach { it.db.close() }
    GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
  }

  @Test fun everyProgramExerciseHasNamedMusclesAndLoadableImages() {
    val programs = ctx.assets.open("programs.json").bufferedReader().use {
      Json.decodeFromString<ProgramLibrary>(it.readText()).programs
    }
    val names = programs.flatMap { it.days }.flatMap { it.items }.map { it.exercise }.toSet()
    assertEquals(21, programs.size)
    assertTrue(names.size >= 395)
    names.forEach { name ->
      val variants = maps.find(name)
      assertTrue("Missing muscle map for $name", variants.isNotEmpty())
      variants.forEach { v ->
        val p = maps.profiles.getValue(v.profile)
        assertTrue("No targets for $name", p.primary.isNotEmpty())
        val roles = p.primary + p.assisting + p.stabilizers
        assertEquals("Conflicting muscle roles: $name", roles.size, roles.toSet().size)
        roles.forEach { assertTrue("Unlabelled muscle: $it", maps.muscles.getValue(it).isNotBlank()) }
        val id = ctx.resources.getIdentifier(v.image, "drawable", ctx.packageName)
        assertNotEquals("Missing image for $name", 0, id)
        val drawable = ctx.getDrawable(id)!!
        assertTrue(drawable.intrinsicWidth > 0 && drawable.intrinsicHeight > 0)
      }
    }
  }

  @Test fun movementChoicesAndSupportHaveDifferentMaps() {
    fun p(name: String) = maps.profiles.getValue(maps.find(name).single().profile)
    assertTrue("quads" in p("Deadlift").primary)
    assertTrue("hamstrings" in p("Romanian deadlift").primary)
    assertTrue("erectors" in p("Barbell row").stabilizers)
    assertFalse("erectors" in p("Chest-supported row").stabilizers)
    assertEquals(listOf("soleus"), p("Seated Calf Raise").primary)
    assertTrue("deep_core" in p("Plank").primary)
    assertEquals("mobility", p("Standing hamstring stretch").kind)
    assertEquals("recovery", p("Upper-body foam rolling (optional)").kind)
    assertEquals(2, maps.find("Pull Ups or Machine Rows").size)
    assertEquals(3, maps.find("Pulldown or pull-up or chin-up").size)
    assertTrue(maps.find("New unknown curl variation").isEmpty())
    assertEquals("Deadlift", workoutMuscleMapName("Deadlift", "Other name", false))
    assertEquals("Leg press", workoutMuscleMapName("Deadlift", "Leg press", true))
  }

  @Test fun programButtonOpensMapAndDismissesToSameWorkout() {
    val app = app()
    compose.setContent { GymCluesTheme { ProgramDayScreen(app, "B1_1", null, rememberNavController()) } }
    compose.onNodeWithContentDescription("Muscle map for Deadlift").performClick()
    compose.onNodeWithText("Muscle mapping").assertIsDisplayed()
    compose.onNodeWithText("Target muscles").assertExists()
    compose.onNodeWithText("Stabilizers").assertExists()
    saveDialog("deadlift-popup.png")
    compose.onNodeWithContentDescription("Close muscle map").performClick()
    compose.onNodeWithText("Muscle mapping").assertDoesNotExist()
    compose.onNodeWithText("Start Workout").assertIsDisplayed()
  }

  @Test fun activeWorkoutMapPreservesEnteredSetValues() {
    val app = app()
    compose.setContent { GymCluesTheme {
      val nav = rememberNavController()
      NavHost(nav, "workout") { composable("workout") {
        ActiveWorkout(app, "program", "B1_1", emptySet(), nav) {}
      } }
    } }
    compose.waitUntil(10_000) {
      compose.onAllNodesWithContentDescription("Muscle map for Deadlift").fetchSemanticsNodes().isNotEmpty()
    }
    compose.onAllNodes(hasSetTextAction())[0].performTextInput("75")
    compose.onNodeWithContentDescription("Muscle map for Deadlift").performClick()
    compose.onNodeWithText("Muscle mapping").assertIsDisplayed()
    compose.onNodeWithContentDescription("Close muscle map").performClick()
    compose.onNodeWithText("75").assertExists()
  }

  @Test fun popupSwitchesBetweenSeatedAndStandingCalfMaps() {
    compose.setContent { GymCluesTheme { MuscleMapButton("Calf raise (seated or standing)") } }
    compose.onNodeWithContentDescription("Muscle map for Calf raise (seated or standing)").performClick()
    compose.onNodeWithText("Seated", useUnmergedTree = true).assertIsDisplayed()
    compose.onNodeWithText("Soleus (deep calf)", substring = false).assertExists()
    compose.onNodeWithText("Standing").performClick()
    compose.onNodeWithText("Gastrocnemius (calf)\nSoleus (deep calf)").assertExists()
    saveDialog("calf-options-popup.png")
    compose.onNodeWithContentDescription("Close muscle map").performClick()
  }

  @Test fun representativeMapsRenderAllRoleColors() {
    val output = System.getProperty("shareOut")?.let(::File)?.apply { mkdirs() }
    listOf("Deadlift", "Bench press", "Seated Calf Raise", "Plank", "Face pull", "Standing hamstring stretch").forEach { name ->
      val v = maps.find(name).single()
      val id = ctx.resources.getIdentifier(v.image, "drawable", ctx.packageName)
      val bitmap = Bitmap.createBitmap(520, 580, Bitmap.Config.ARGB_8888)
      ctx.getDrawable(id)!!.apply { setBounds(0, 0, 520, 580); draw(Canvas(bitmap)) }
      val pixels = IntArray(520 * 580)
      bitmap.getPixels(pixels, 0, 520, 0, 0, 520, 580)
      assertTrue("Unrendered targets: $name", pixels.any { it == 0xFFFF7765.toInt() })
      if (maps.profiles.getValue(v.profile).stabilizers.isNotEmpty()) {
        assertTrue("Unrendered stabilizers: $name", pixels.any { it == 0xFF67B7F7.toInt() })
      }
      output?.resolve("${v.image}.png")?.outputStream()?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()
    }
  }

  private fun saveDialog(name: String) {
    val output = System.getProperty("shareOut")?.let(::File)?.apply { mkdirs() } ?: return
    compose.runOnIdle {
      // Draw the dialog window directly: PixelCopy capture waits for hardware
      // frames that the Robolectric host does not emit consistently.
      val view = android.view.inspector.WindowInspector.getGlobalWindowViews().last()
      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      view.draw(Canvas(bitmap))
      output.resolve(name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()
    }
  }
}
