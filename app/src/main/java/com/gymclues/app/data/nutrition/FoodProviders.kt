package com.gymclues.app.data.nutrition

import com.gymclues.app.BuildConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

interface FoodProvider {
  val name: String
  val source: String
  suspend fun searchFoods(query: String): List<FoodEntity>
  suspend fun getFoodDetails(sourceId: String): FoodEntity?
  suspend fun lookupBarcode(barcode: String): FoodEntity?
}
data class FoodResponse(val status: Int, val body: String, val retryAfter: Long = 60)
fun interface FoodTransport { suspend fun get(url: String): FoodResponse }
class FoodHttp : FoodTransport {
  override suspend fun get(url: String): FoodResponse = runInterruptible(Dispatchers.IO) {
    require(URI(url).scheme == "https") { "Food providers require HTTPS" }
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
      connection.connectTimeout = 8_000; connection.readTimeout = 12_000; connection.instanceFollowRedirects = false
      connection.setRequestProperty("User-Agent", "GymClues/${BuildConfig.VERSION_NAME} (https://github.com/oxieg3n/GymClues/issues)")
      connection.setRequestProperty("Accept", "application/json")
      val code = connection.responseCode
      val stream = if (code in 200..299) connection.inputStream else connection.errorStream
      val body = stream?.bufferedReader()?.use { reader ->
        val out = StringBuilder(); val buffer = CharArray(4096)
        while (true) { val n = reader.read(buffer); if (n < 0) break
          require(out.length + n <= 2_000_000) { "Food response is too large" }; out.append(buffer, 0, n) }
        out.toString()
      }.orEmpty()
      FoodResponse(code, body, connection.getHeaderField("Retry-After")?.toLongOrNull()?.coerceIn(1, 3600) ?: 60)
    } finally { connection.disconnect() }
  }
}
class FoodRateLimit(val seconds: Long) : IOException("Provider is rate limited. Try again in $seconds seconds.")
class FoodHttpError(val status: Int) : IOException("Food provider HTTP $status")
fun providerMessage(error: Exception): String = when (error) {
  is FoodRateLimit -> error.message.orEmpty()
  is FoodHttpError -> if (error.status in 500..599) "Food database temporarily unavailable (HTTP ${error.status}). Try again shortly; your local foods are still available."
    else "Food database request failed (HTTP ${error.status}). Try again or use cached/custom foods."
  is SocketTimeoutException, is TimeoutCancellationException -> "Search timed out. Your local foods are still available."
  is IOException -> "Online food lookup is unavailable. Check your connection or use cached/custom foods."
  else -> "The provider returned incomplete or invalid data. Use a local food or enter the label manually."
}

private fun encoded(v: String) = URLEncoder.encode(v, "UTF-8")
private fun JsonObject.text(key: String) = (get(key) as? JsonPrimitive)?.contentOrNull.orEmpty()
private fun JsonObject.number(key: String): Double? = (get(key) as? JsonPrimitive)?.doubleOrNull?.takeIf { it.isFinite() && it in 0.0..1_000_000.0 }
private fun JsonObject.objectAt(key: String) = get(key) as? JsonObject
private fun JsonObject.arrayAt(key: String) = get(key) as? JsonArray ?: JsonArray(emptyList())

/** Explicit search, not search-as-you-type: OFF permits ten searches/minute and fifteen product reads/minute. */
class OpenFoodFactsProvider(private val transport: FoodTransport = FoodHttp(), private val clock: () -> Long = { System.nanoTime() / 1_000_000 },
  private val wait: suspend (Long) -> Unit = { delay(it) }) : FoodProvider {
  override val name = "Open Food Facts"
  override val source = "off"
  private val mutex = Mutex()
  private var nextSearch = 0L; private var nextProduct = 0L; private var blockedUntil = 0L
  private val fields = "code,product_name,product_name_en,brands,nutriments,nutrition,serving_size,serving_quantity,serving_quantity_unit,product_quantity_unit,nutrition_data_per"
  private suspend fun request(url: String, search: Boolean): JsonObject = mutex.withLock {
    val time = clock(); if (time < blockedUntil) throw FoodRateLimit((blockedUntil - time + 999) / 1000)
    val next = if (search) nextSearch else nextProduct
    if (next > time) wait(next - time)
    currentCoroutineContext().ensureActive()
    if (search) nextSearch = clock() + 6100 else nextProduct = clock() + 4100
    val response = transport.get(url)
    if (response.status == 429) { blockedUntil = clock() + response.retryAfter * 1000; throw FoodRateLimit(response.retryAfter) }
    if (response.status == 404) return@withLock buildJsonObject { }
    if (response.status !in 200..299) throw FoodHttpError(response.status)
    Json.parseToJsonElement(response.body).jsonObject
  }
  override suspend fun searchFoods(query: String): List<FoodEntity> {
    require(query.trim().length in 2..120)
    val root = request("https://search.openfoodfacts.org/search?q=${encoded(query.trim())}&page_size=20&langs=en&fields=$fields", true)
    val hits = root["hits"] as? JsonArray ?: throw SerializationException("Food search returned an invalid response")
    return hits.take(20).mapNotNull { (it as? JsonObject)?.let(::parse) }
  }
  override suspend fun getFoodDetails(sourceId: String): FoodEntity? {
    require(sourceId.matches(Regex("[0-9]{8}|[0-9]{12,14}")))
    return request("https://world.openfoodfacts.org/api/v3/product/$sourceId?fields=$fields", false).objectAt("product")?.let(::parse)
  }
  override suspend fun lookupBarcode(barcode: String) = getFoodDetails(barcode)

  companion object {
    fun parse(product: JsonObject): FoodEntity? {
      val code = product.text("code"); val title = product.text("product_name").ifBlank { product.text("product_name_en") }
      if (!code.matches(Regex("[0-9]{8}|[0-9]{12,14}")) || title.isBlank()) return null
      val n = product.objectAt("nutriments") ?: return null
      // OFF's standardized _100g values also represent 100 ml for explicitly volume-based labels.
      val unit = if (product.text("serving_quantity_unit") == "ml" || product.text("product_quantity_unit") == "ml" || product.text("nutrition_data_per") == "100ml") "ml" else "g"
      val nutrients = Nutrients(n.number("energy-kcal_100g") ?: n.number("energy_100g")?.div(4.184),
        n.number("proteins_100g"), n.number("carbohydrates_100g"), n.number("fat_100g"), n.number("fiber_100g"),
        n.number("sugars_100g"), n.number("sodium_100g")?.times(1000))
      if (!nutrients.valid() || nutrients.values().all { it == null }) return null
      val portion = product.number("serving_quantity")?.takeIf { it > 0 }?.let {
        FoodPortion(product.text("serving_size").ifBlank { "1 serving ($it $unit)" }, it)
      }
      val brand = (product["brands"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }?.joinToString(", ") ?: product.text("brands")
      return FoodEntity(id = stableNutritionId("off|$code"), ownerId = "", name = title.take(200), brand = brand.take(200),
        barcode = code, source = "off", sourceId = code, sourceUrl = "https://world.openfoodfacts.org/product/$code",
        license = "Open Food Facts · ODbL 1.0 / DbCL 1.0", basisQuantity = 100.0, basisUnit = unit,
        nutrients = nutrients, portions = listOfNotNull(portion))
    }
  }
}

/** A configured server must forward these documented FDC paths and add its own key; the APK never receives that key. */
class UsdaFoodProvider(private val proxyUrl: String, private val transport: FoodTransport = FoodHttp(),
  private val clock: () -> Long = { System.nanoTime() / 1_000_000 }, private val wait: suspend (Long) -> Unit = { delay(it) }) : FoodProvider {
  override val name = "USDA FoodData Central"
  override val source = "usda"
  private val mutex = Mutex()
  private var nextRequest = 0L
  private var blockedUntil = 0L
  init { require(validProxy(proxyUrl)) { "Use an HTTPS USDA proxy URL without credentials, query or fragment" } }
  private suspend fun request(path: String): JsonElement = mutex.withLock {
    val time = clock()
    if (time < blockedUntil) throw FoodRateLimit((blockedUntil - time + 999) / 1000)
    if (time < nextRequest) wait(nextRequest - time)
    currentCoroutineContext().ensureActive()
    nextRequest = clock() + 3700
    val response = transport.get(proxyUrl.trimEnd('/') + path)
    if (response.status == 429) { blockedUntil = clock() + response.retryAfter * 1000; throw FoodRateLimit(response.retryAfter) }
    if (response.status == 404) return@withLock JsonNull
    if (response.status !in 200..299) throw FoodHttpError(response.status)
    Json.parseToJsonElement(response.body)
  }
  override suspend fun searchFoods(query: String): List<FoodEntity> {
    require(query.trim().length in 2..120)
    val root = request("/foods/search?query=${encoded(query)}&pageSize=20") as? JsonObject ?: return emptyList()
    return root.arrayAt("foods").take(20).mapNotNull { (it as? JsonObject)?.let(::parse) }
  }
  override suspend fun getFoodDetails(sourceId: String): FoodEntity? {
    require(sourceId.matches(Regex("[0-9]+")))
    return (request("/food/$sourceId") as? JsonObject)?.let(::parse)
  }
  override suspend fun lookupBarcode(barcode: String) = searchFoods(barcode).firstOrNull { it.barcode == barcode }
  companion object {
    fun validProxy(url: String) = runCatching { URI(url).let { it.scheme == "https" && !it.host.isNullOrBlank() && it.userInfo == null && it.query == null && it.fragment == null } }.getOrDefault(false)
    fun parse(food: JsonObject): FoodEntity? {
      val id = food.text("fdcId"); val title = food.text("description")
      if (id.toLongOrNull() == null || title.isBlank()) return null
      val nutrients = food.arrayAt("foodNutrients").mapNotNull { it as? JsonObject }
      fun amount(number: Int, unit: String): Double? = nutrients.firstNotNullOfOrNull { n ->
        val detail = n.objectAt("nutrient")
        val nutrientId = n.number("nutrientId")?.toInt() ?: detail?.number("id")?.toInt()
        val actualUnit = n.text("unitName").ifBlank { detail?.text("unitName").orEmpty() }.lowercase(Locale.ROOT)
        if (nutrientId == number && actualUnit == unit) n.number("value") ?: n.number("amount") else null
      }
      val values = Nutrients(amount(1008, "kcal") ?: amount(2048, "kcal") ?: amount(2047, "kcal") ?: amount(1062, "kj")?.div(4.184),
        amount(1003, "g"), amount(1005, "g"), amount(1004, "g"), amount(1079, "g"), amount(2000, "g"), amount(1093, "mg"))
      if (!values.valid() || values.values().all { it == null }) return null
      val portions = food.arrayAt("foodPortions").mapNotNull { (it as? JsonObject)?.let { p ->
        val grams = p.number("gramWeight")?.takeIf { it > 0 } ?: return@let null
        val label = p.text("portionDescription").ifBlank { "${p.number("amount") ?: 1.0} ${p.objectAt("measureUnit")?.text("name").orEmpty()}" }
        FoodPortion("$label ($grams g)", grams)
      } }.toMutableList()
      if (food.text("servingSizeUnit").lowercase(Locale.ROOT) == "g") food.number("servingSize")?.takeIf { it > 0 }?.let {
        portions += FoodPortion(food.text("householdServingFullText").ifBlank { "1 serving ($it g)" }, it)
      }
      return FoodEntity(id = stableNutritionId("usda|$id"), ownerId = "", name = title.take(200), brand = food.text("brandOwner").ifBlank { food.text("brandName") }.take(200),
        barcode = food.text("gtinUpc").takeIf { it.matches(Regex("[0-9]{8}|[0-9]{12,14}")) }.orEmpty(), source = "usda", sourceId = id,
        sourceUrl = "https://fdc.nal.usda.gov/food-details/$id/nutrients", license = "USDA FoodData Central · CC0 1.0",
        basisQuantity = 100.0, basisUnit = "g", nutrients = values, portions = portions)
    }
  }
}
