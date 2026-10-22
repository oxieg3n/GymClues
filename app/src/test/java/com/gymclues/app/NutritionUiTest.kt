package com.gymclues.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.inspector.WindowInspector
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import com.gymclues.app.ui.nutrition.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class) @GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-mdpi")
class NutritionUiTest {
  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
  private val ctx: Context get() = ApplicationProvider.getApplicationContext()
  private lateinit var app: AppContainer
  private val today get() = LocalDate.now()
  private val onlineFood = FoodEntity(ownerId = "", name = "Chicken label", brand = "Fixture brand", source = "off", sourceId = "12345678", barcode = "12345678",
    license = "Open Food Facts · ODbL / DbCL", sourceUrl = "https://world.openfoodfacts.org/product/12345678", basisQuantity = 100.0, basisUnit = "g", nutrients = Nutrients(120.0, 25.0, 2.0, 1.0))
  @Before fun setup() {
    Class.forName("androidx.compose.foundation.lazy.layout.AndroidPrefetchScheduler").getDeclaredField("frameIntervalNs").apply { isAccessible = true }.setLong(null, Long.MAX_VALUE / 2)
    ctx.getSharedPreferences("ui", Context.MODE_PRIVATE).edit().clear().putBoolean("tourDone", true).commit()
    app = AppContainer(ctx, listOf(object : FoodProvider {
      override val name = "Fixture"; override val source = "off"
      override suspend fun searchFoods(query: String) = listOf(onlineFood)
      override suspend fun getFoodDetails(sourceId: String) = onlineFood
      override suspend fun lookupBarcode(barcode: String) = onlineFood.takeIf { barcode == "12345678" }
    }))
  }
  @After fun cleanup() {
    compose.mainClock.autoAdvance = true
    compose.activity.setContent { }; compose.waitForIdle()
    runBlocking { (AppContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }.get(app) as CoroutineScope).coroutineContext[Job]?.cancelAndJoin() }
    app.nutrition.close(); app.db.close(); GymDb::class.java.getDeclaredField("i").apply { isAccessible = true }.set(null, null)
    Class.forName("androidx.compose.foundation.lazy.layout.AndroidPrefetchScheduler").getDeclaredField("frameIntervalNs").apply { isAccessible = true }.setLong(null, 0L)
  }
  private fun start() { compose.activity.setContent { GymCluesTheme { App(app) } }; compose.waitForIdle() }
  private fun nutrition() {
    compose.onNodeWithContentDescription("GymClues menu").performClick()
    compose.onAllNodesWithText("Nutrition").onLast().performScrollTo().performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("0 kcal consumed").fetchSemanticsNodes().isNotEmpty() }
  }
  private fun click(text: String) {
    reveal(text)
    compose.onNodeWithText(text).performClick()
    compose.waitForIdle()
  }
  private fun reveal(text: String) {
    compose.waitUntil(10_000) {
      try {
        if (compose.onAllNodesWithText(text).fetchSemanticsNodes().isEmpty()) compose.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        else compose.onNodeWithText(text).performScrollTo()
        compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
      } catch (_: AssertionError) { false }
    }
  }
  private fun input(label: String, value: String) {
    reveal(label)
    compose.onNode(hasSetTextAction() and hasText(label)).performTextReplacement(value)
  }
  private fun waitFor(text: String) {
    if (text.contains("kcal consumed") || text.contains("kcal remaining")) {
      compose.waitUntil(10_000) { compose.onAllNodes(isDialog()).fetchSemanticsNodes().isEmpty() }
      compose.onNode(hasScrollAction()).performScrollToIndex(0)
    }
    try { compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } }
    catch (e: Throwable) { shot("failure-${text.take(30).replace(Regex("[^A-Za-z0-9]"), "-")}"); throw AssertionError("Waiting for '$text'\n${compose.onAllNodes(isRoot()).onLast().printToString()}", e) }
  }
  private fun shot(name: String) {
    val dir = System.getenv("NUTRITION_PREVIEWS")?.let(::File)?.apply { mkdirs() } ?: return
    compose.waitForIdle(); compose.runOnIdle {
      val view = WindowInspector.getGlobalWindowViews().last()
      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888); view.draw(Canvas(bitmap))
      dir.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
    }
  }
  @Test fun menuSearchLogEditDeleteAndDateNavigation() {
    runBlocking { app.nutrition.saveGoal(NutritionGoal(ownerId = app.nutrition.owner, effectiveDate = today.toString(), daily = Nutrients(2000.0, 100.0, 200.0, 60.0))) }
    start(); nutrition(); shot("01-nutrition-empty-390")
    click("Add food to Lunch"); input("Search foods", "Chicken")
    click("Search food databases"); waitFor("Chicken label"); click("Chicken label"); waitFor("Serving size")
    input("Quantity", "1.5"); shot("02-food-details-390")
    click("Add to Lunch"); waitFor("180 kcal consumed")
    shot("03-nutrition-logged-390")
    click("Chicken label")
    compose.onNode(hasSetTextAction() and hasText("Quantity")).performTextReplacement("2")
    compose.onNodeWithText("Save entry").performClick(); waitFor("240 kcal consumed")
    click("Chicken label"); compose.onNodeWithText("Delete entry").performClick(); waitFor("0 kcal consumed")
    compose.onNodeWithContentDescription("Previous nutrition day").performClick(); waitFor(today.minusDays(1).toString())
    compose.onNodeWithContentDescription("Next nutrition day").performClick(); waitFor(today.toString())
    assertTrue(runBlocking { app.nutrition.diary(today, today).first().isEmpty() })
  }
  @Test fun createFoodAndUnknownBarcodeHaveUsableManualFallbacks() {
    start(); nutrition(); click("Add food to Breakfast")
    input("Package barcode", "11111111"); click("Look up barcode"); waitFor("No usable nutrition record found. You can create a food from this barcode.")
    click("Create food from barcode")
    input("Food name", "My yogurt"); input("Calories (kcal)", "150"); input("Protein (g)", "20")
    input("Carbohydrates (g)", "10"); input("Fat (g)", "3"); click("Save food")
    waitFor("My yogurt"); click("Add favorite"); waitFor("Remove favorite")
    click("Add to Breakfast"); waitFor("150 kcal consumed")
    assertEquals("11111111", runBlocking { app.nutrition.foods().first().single().barcode })
    assertTrue(runBlocking { app.nutrition.foods().first().single().favorite })
  }
  @Test fun goalSettingsUpdateTargetsAndPersist() {
    start(); nutrition(); click("Nutrition goals")
    input("Calories (kcal)", "2200"); input("Protein (g)", "150"); input("Carbohydrates (g)", "250"); input("Fat (g)", "65")
    click("Save nutrition goals"); waitFor("Nutrition goals saved")
    compose.onNodeWithContentDescription("Back").performClick(); waitFor("2200 kcal remaining · 2200 target")
    assertEquals(150.0, runBlocking { app.nutrition.goals().first().single().daily.protein!! }, 0.0)
  }
  @Test fun copyMealFromYesterdayAndCreateRecipeThroughTheUi() {
    runBlocking {
      val food = app.nutrition.saveFood(onlineFood.copy(id = newId(), sourceId = newId(), source = "user", ownerId = app.nutrition.owner))
      app.nutrition.log(food, today.minusDays(1).toString(), "Lunch", 1.0, portions(food).first(), newId())
    }
    start(); nutrition()
    compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Copy yesterday's Lunch"))
    compose.onNodeWithContentDescription("Copy yesterday's Lunch").performClick(); waitFor("120 kcal consumed")
    click("Save as meal"); input("Name", "Usual lunch"); click("Save meal"); waitFor("120 kcal consumed")
    click("Meals & recipes"); click("Add Usual lunch"); compose.onNodeWithText("Add to diary").performClick(); waitFor("Added to Snacks")
    click("Create recipe"); input("Name", "Chicken prep"); input("Number of recipe servings", "2")
    click("Add ingredient"); waitFor("Chicken label"); compose.onNodeWithText("Chicken label").performScrollTo().performClick()
    compose.onAllNodesWithText("Add ingredient").onLast().performClick()
    click("Save recipe"); waitFor("Chicken prep")
    assertEquals(setOf("meal", "recipe"), runBlocking { app.nutrition.bundles().first().map { it.kind }.toSet() })
    shot("04-saved-recipes-390")
    click("Add Chicken prep")
    compose.onNode(hasSetTextAction() and hasText("Recipe servings")).performTextReplacement("2")
    compose.onNodeWithText("Add to diary").performClick()
    compose.waitUntil(10_000) { compose.onAllNodes(isDialog()).fetchSemanticsNodes().isEmpty() }
    assertEquals(360.0, runBlocking { app.nutrition.days(today, today).first().single().total.kcal!! }, 0.0)
  }
  @Test fun weightAndWaterAreEditableAndHistoryOpens() {
    start(); nutrition(); click("Weight & water")
    input("Weight on $today", "80"); click("Save weigh-in"); waitFor("Weigh-in saved")
    click("Add 250 ml")
    compose.waitUntil(10_000) { runBlocking { app.nutrition.water(today, today).first().isNotEmpty() } }
    click("Edit water entry")
    compose.onNode(hasSetTextAction() and hasText("Milliliters")).performTextReplacement("500")
    compose.onNodeWithText("Save water").performClick()
    compose.waitUntil(10_000) { compose.onAllNodesWithText("Save water").fetchSemanticsNodes().isEmpty() }
    assertEquals(500.0, runBlocking { app.nutrition.water(today, today).first().single().ml }, 0.0)
    compose.onNodeWithContentDescription("Back").performClick(); click("Nutrition history"); waitFor("0 completed workouts · 0 workout days")
    shot("05-history-390")
  }
  @Test @Config(qualifiers = "w320dp-h740dp-mdpi") fun narrowLargeTextKeepsFoodActionsReachable() {
    val config = android.content.res.Configuration(ctx.resources.configuration).apply { fontScale = 1.3f }
    ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics); compose.activity.resources.updateConfiguration(config, compose.activity.resources.displayMetrics)
    start(); nutrition(); click("Add food to Dinner"); compose.onNodeWithText("Scan barcode").performScrollTo().assertIsDisplayed()
    shot("06-search-large-text-320")
  }
  @Test @Config(qualifiers = "w768dp-h1024dp-mdpi") fun tabletDiaryRetainsHomeAndMusicNavigation() {
    start(); compose.onNodeWithContentDescription("GymClues menu").assertIsDisplayed(); nutrition(); shot("07-nutrition-tablet-768")
    compose.onNodeWithText("Add food to Breakfast").performScrollTo().assertIsDisplayed()
  }
}
