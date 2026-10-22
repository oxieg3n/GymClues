package com.gymclues.app

import com.gymclues.app.data.nutrition.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class NutritionProviderTest {
  private val product = """{"code":"3017620422003","product_name":"Test label","brands":"Test brand","serving_quantity":30,"serving_size":"1 package (30 g)","nutriments":{"energy-kcal_100g":120,"proteins_100g":25,"carbohydrates_100g":2,"fat_100g":1,"sodium_100g":0.2}}"""
  @Test fun offSearchAndBarcodeParseRealResponseShape() = runBlocking {
    val urls = mutableListOf<String>()
    val provider = OpenFoodFactsProvider(FoodTransport { url -> urls += url; FoodResponse(200, if (url.contains("cgi")) """{"products":[$product]}""" else """{"product":$product}""") }, wait = {})
    val food = provider.searchFoods("test label").single()
    assertEquals("off", food.source); assertEquals(120.0, food.nutrients.kcal!!, 0.0)
    assertEquals(200.0, food.nutrients.sodium!!, 0.0); assertEquals(30.0, food.portions.single().amount, 0.0)
    assertNotNull(provider.lookupBarcode("3017620422003")); assertTrue(urls.last().contains("/api/v3/product/"))
    assertTrue(urls.first().contains("test+label"))
  }
  @Test fun emptyMalformedMissingAndInvalidResponses() = runBlocking {
    assertTrue(OpenFoodFactsProvider(FoodTransport { FoodResponse(200, """{"products":[]}""") }).searchFoods("rice").isEmpty())
    assertNull(OpenFoodFactsProvider(FoodTransport { FoodResponse(404, "") }).lookupBarcode("12345678"))
    assertNull(OpenFoodFactsProvider.parse(Json.parseToJsonElement("""{"code":"12345678","product_name":"Missing nutrition"}""").jsonObject))
    assertNull(OpenFoodFactsProvider.parse(Json.parseToJsonElement(product.replace("Test label", "")).jsonObject))
    try { OpenFoodFactsProvider(FoodTransport { FoodResponse(200, "not-json") }).searchFoods("food"); fail("Malformed response must fail") } catch (_: IllegalArgumentException) { }
    try { OpenFoodFactsProvider().lookupBarcode("abc"); fail("Invalid barcode must fail before a request") } catch (_: IllegalArgumentException) { }
  }
  @Test fun missingMacrosAreNotInvented() {
    val p = Json.parseToJsonElement("""{"code":"12345678","product_name":"Partial","nutriments":{"energy-kcal_100g":50}}""").jsonObject
    val f = OpenFoodFactsProvider.parse(p)!!
    assertNull(f.nutrients.protein); assertEquals(50.0, f.nutrients.kcal!!, 0.0)
  }
  @Test fun rateLimitsThrottleAndHonorRetryAfter() = runBlocking {
    var time = 1000L; val waits = mutableListOf<Long>(); var calls = 0
    val provider = OpenFoodFactsProvider(FoodTransport { calls++; FoodResponse(if (calls < 3) 200 else 429, """{"products":[]}""", 90) },
      clock = { time }, wait = { waits += it; time += it })
    provider.searchFoods("food"); provider.searchFoods("food"); assertEquals(listOf(6100L), waits)
    try { provider.searchFoods("food"); fail() } catch (e: FoodRateLimit) { assertEquals(90, e.seconds.toInt()) }
    try { provider.searchFoods("food"); fail() } catch (_: FoodRateLimit) { assertEquals(3, calls) }
  }
  @Test fun timeoutOfflineAndServerFailuresAreErrorsNotFoods() = runBlocking {
    for (error in listOf(IOException("offline"), SocketTimeoutException("timeout"))) {
      try { OpenFoodFactsProvider(FoodTransport { throw error }).searchFoods("rice"); fail() } catch (e: IOException) { assertSame(error, e) }
    }
    try { OpenFoodFactsProvider(FoodTransport { FoodResponse(503, "") }).searchFoods("rice"); fail() } catch (_: IOException) { }
  }
  @Test fun usdaProxyHasNoKeyAndUsesExplicitNutrientUnitsAndPortions() = runBlocking {
    var url = ""
    val json = """{"fdcId":123,"description":"Chicken breast","foodNutrients":[{"nutrient":{"id":1008,"unitName":"kcal"},"amount":120},{"nutrient":{"id":1003,"unitName":"g"},"amount":25}],"foodPortions":[{"gramWeight":150,"portionDescription":"piece"}]}"""
    val provider = UsdaFoodProvider("https://example.test/fdc", FoodTransport { request -> url = request; FoodResponse(200, json) })
    val food = provider.getFoodDetails("123")!!
    assertEquals(25.0, food.nutrients.protein!!, 0.0); assertEquals(150.0, food.portions.single().amount, 0.0)
    assertFalse(url.contains("api_key")); assertTrue(food.license.contains("CC0"))
    assertFalse(UsdaFoodProvider.validProxy("http://example.test"))
    assertFalse(UsdaFoodProvider.validProxy("https://user:secret@example.test"))
    assertFalse(UsdaFoodProvider.validProxy("https://example.test?api_key=secret"))
  }
  @Test fun usdaThrottlesAndDoesNotRetryDuringServerCooldown() = runBlocking {
    var time = 1000L; var calls = 0; val waits = mutableListOf<Long>()
    val provider = UsdaFoodProvider("https://example.test", FoodTransport {
      calls++; FoodResponse(if (calls == 1) 200 else 429, "{\"foods\":[]}", 120)
    }, { time }, { waits += it; time += it })
    provider.searchFoods("rice")
    try { provider.searchFoods("rice"); fail() } catch (_: FoodRateLimit) { }
    try { provider.searchFoods("rice"); fail() } catch (_: FoodRateLimit) { }
    assertEquals(listOf(3700L), waits); assertEquals(2, calls)
  }
}
