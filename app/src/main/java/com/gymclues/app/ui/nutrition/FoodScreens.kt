package com.gymclues.app.ui.nutrition

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable internal fun FoodSearchContent(app: AppContainer, selected: (FoodEntity) -> Unit) {
  val repo = app.nutrition
  val history by remember(repo) { repo.recent() }.collectAsState(emptyList())
  val usage by remember(repo) { repo.usage() }.collectAsState(emptyList())
  var query by rememberSaveable { mutableStateOf("") }
  var debounced by remember { mutableStateOf("") }
  val foods by remember(repo, debounced) { repo.foods(debounced) }.collectAsState(emptyList())
  var mode by rememberSaveable { mutableStateOf("All") }
  val actions = rememberActions()
  LaunchedEffect(query) { delay(300); debounced = query }
  val matches = remember(foods, history, debounced, mode, usage) { rankedFoods(foods, history, debounced, mode, usage) }
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    AppTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Search foods") })
    Options(listOf("All", "Recent", "Frequent", "Favorites", "My foods"), mode) { mode = it }
    AppButton({ actions.run {
      val result = repo.search(query)
      actions.message = result.message.ifBlank { if (result.foods.isEmpty()) "No matching foods found. Try another description or create a food." else "${result.foods.size} online results cached for offline use." }
    } }, enabled = !actions.busy && query.trim().length >= 2) { Text("Search food databases") }
    Muted("Local matches update as you type. Online search runs when you tap Search, respecting provider rate limits.")
    ActionMessage(actions)
    if (matches.isEmpty()) Text("No local matches. Search online or create a food from its label.")
    matches.forEach { food -> AppCard(Modifier.fillMaxWidth().clickable { selected(food) }) {
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(food.name, style = MaterialTheme.typography.titleMedium)
        Muted("${food.brand.ifBlank { "Generic / unbranded" }} · ${when (food.source) { "off" -> "Open Food Facts"; "usda" -> "USDA FoodData Central"; else -> "My food" }} ${if (food.favorite) "· Favorite" else ""}")
        Text("Per ${amount(food.basisQuantity)} ${food.basisUnit}")
        NutrientValues(food.nutrients)
      }
    } }
    Muted("Open Food Facts · ODbL/DbCL. USDA · CC0 when a proxy is configured. Verify contributed values against the package label.")
  }
}

@Composable fun FoodSearchScreen(app: AppContainer, nav: NavHostController, date: String, meal: String) {
  val actions = rememberActions()
  var barcode by rememberSaveable { mutableStateOf("") }
  var missing by rememberSaveable { mutableStateOf(false) }
  fun lookup(code: String) {
    barcode = code; missing = false
    actions.run { val food = app.nutrition.barcode(code)
      if (food == null) { missing = true; actions.message = "No usable nutrition record found. You can create a food from this barcode." }
      else nav.navigate(foodPath(date, meal, food.id))
    }
  }
  val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
    result.contents?.let(::lookup) ?: run { actions.message = "Scan canceled. Search, enter a barcode or create a food instead." }
  }
  NutritionPage {
    item { ScreenTitle("Add food", "$meal · $date") }
    item { AppButton({ try {
      scanner.launch(ScanOptions().setDesiredBarcodeFormats(listOf("UPC_A", "UPC_E", "EAN_8", "EAN_13"))
        .setPrompt("Scan a food package barcode, or press Back to cancel").setBeepEnabled(false).setOrientationLocked(false))
    } catch (_: Exception) { actions.message = "Scanner unavailable. Enter the barcode below or search for a food." } }, enabled = !actions.busy) { Text("Scan barcode") } }
    item { AppTextField(barcode, { barcode = it.filter(Char::isDigit).take(14) }, Modifier.fillMaxWidth(), label = { Text("Package barcode") }) }
    item { AppOutlinedButton({ lookup(barcode) }, enabled = !actions.busy && barcode.isNotBlank()) { Text("Look up barcode") }; ActionMessage(actions) }
    item { AppOutlinedButton({ nav.navigate(editorPath(date, meal, barcode = barcode)) }) { Text(if (missing || barcode.isNotBlank()) "Create food from barcode" else "Create custom food") } }
    item { FoodSearchContent(app) { nav.navigate(foodPath(date, meal, it.id)) } }
  }
}

@Composable fun FoodDetailScreen(app: AppContainer, nav: NavHostController, date: String, meal: String, id: String) {
  var food by remember { mutableStateOf<FoodEntity?>(null) }
  var loading by remember { mutableStateOf(true) }
  var quantity by rememberSaveable { mutableStateOf("1") }
  var portionIndex by rememberSaveable { mutableIntStateOf(0) }
  var initialized by rememberSaveable { mutableStateOf(false) }
  val actions = rememberActions()
  val token = rememberSaveable { newId() }
  val uri = LocalUriHandler.current
  LaunchedEffect(id) { try {
    food = app.nutrition.food(id)
    if (!initialized) food?.let { f ->
      val preferred = app.nutrition.goals().first().filter { it.effectiveDate <= date }.maxByOrNull { it.effectiveDate }?.foodUnit
      val options = portions(f)
      if (preferred == "oz" && f.basisUnit == "g") {
        portionIndex = options.indexOfFirst { it.label == "oz" }; quantity = (f.basisQuantity / 28.349523125).toString()
      } else if (f.portions.isNotEmpty()) portionIndex = options.indexOfFirst { it == f.portions.first() }.coerceAtLeast(0)
      initialized = true
    }
  } finally { loading = false } }
  val current = food
  NutritionPage {
    item { ScreenTitle(current?.name ?: "Food details", "$meal · $date") }
    if (loading) item { CircularProgressIndicator() }
    else if (current == null) item { Text("Food not found. Return to search or create a custom food.") }
    else {
      val servings = portions(current)
      val portion = servings.getOrElse(portionIndex) { servings.first() }
      item { Text(current.brand.ifBlank { "Generic / unbranded" }); Muted(current.license) }
      item { DecimalField("Quantity", quantity) { quantity = it } }
      item { Text("Serving size"); Options(servings.map { it.label }, portion.label) { value -> portionIndex = servings.indexOfFirst { it.label == value } } }
      item { val parsed = quantity.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 && it <= 100_000 }
        if (parsed != null) NutrientValues(current.nutrients.scale(parsed * portion.amount / current.basisQuantity)) else Text("Enter a positive quantity") }
      item { ActionMessage(actions) }
      item { AppButton({ actions.run({ nav.popBackStack("nutrition?date={date}", false) }) {
        app.nutrition.log(current, date, meal, number(quantity), portion, token)
      } }, enabled = !actions.busy) { Text("Add to $meal") } }
      item { AppOutlinedButton({ actions.run { app.nutrition.favorite(id); food = app.nutrition.food(id) } }, enabled = !actions.busy) { Text(if (current.favorite) "Remove favorite" else "Add favorite") } }
      if (current.source == "user") {
        item { AppOutlinedButton({ nav.navigate(editorPath(date, meal, id)) }) { Text("Edit custom food") } }
        item { TextButton({ actions.run({ nav.popBackStack() }) { app.nutrition.deleteFood(id) } }, enabled = !actions.busy) { Text("Delete custom food") }; Muted("Previously logged entries keep their saved nutrition facts.") }
      } else item {
        AppOutlinedButton({ actions.run { food = app.nutrition.refreshFood(id) } }, enabled = !actions.busy) { Text("Refresh source details") }
        TextButton({ uri.openUri(current.sourceUrl) }) { Text("View food data source") }
      }
    }
  }
}

@Composable fun FoodEditorScreen(app: AppContainer, nav: NavHostController, date: String, meal: String, id: String, barcode: String, quick: Boolean) {
  var original by remember { mutableStateOf<FoodEntity?>(null) }
  var ready by remember { mutableStateOf(id == "new") }
  var hydrated by rememberSaveable { mutableStateOf(false) }
  var name by rememberSaveable { mutableStateOf(if (quick) "Quick add" else "") }
  var brand by rememberSaveable { mutableStateOf("") }
  var code by rememberSaveable { mutableStateOf(barcode) }
  var basis by rememberSaveable { mutableStateOf("1") }
  var unit by rememberSaveable { mutableStateOf("serving") }
  var values by rememberSaveable { mutableStateOf(List(7) { "" }) }
  val actions = rememberActions()
  val newFoodId = rememberSaveable { newId() }
  LaunchedEffect(id) { if (id != "new") {
    original = app.nutrition.food(id)
    if (!hydrated) original?.let { f -> name = f.name; brand = f.brand; code = f.barcode; basis = f.basisQuantity.toString(); unit = f.basisUnit; values = f.nutrients.values().map { it?.toString().orEmpty() }; hydrated = true }
    ready = true
  } }
  NutritionPage {
    item { ScreenTitle(if (quick) "Quick add nutrition" else if (id == "new") "Create food" else "Edit food", "Enter the nutrition facts for the serving below. Unknown nutrients may stay blank.") }
    if (!ready) item { CircularProgressIndicator() } else {
      item { AppTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Food name") }) }
      item { AppTextField(brand, { brand = it }, Modifier.fillMaxWidth(), label = { Text("Brand (optional)") }) }
      item { AppTextField(code, { code = it }, Modifier.fillMaxWidth(), label = { Text("Barcode (optional)") }) }
      item { DecimalField("Nutrition facts per quantity", basis) { basis = it }; Options(listOf("g", "ml", "serving", "piece", "package", "cup", "tablespoon"), unit) { unit = it } }
      listOf("Calories (kcal)", "Protein (g)", "Carbohydrates (g)", "Fat (g)", "Fiber (g, optional)", "Sugar (g, optional)", "Sodium (mg, optional)").forEachIndexed { index, label ->
        item { DecimalField(label, values[index]) { text -> values = values.toMutableList().also { it[index] = text } } }
      }
      item { ActionMessage(actions) }
      item { AppButton({ actions.run {
        require(id == "new" || original != null) { "This custom food is no longer available" }
        val n = values.map(::optional)
        val food = (original ?: FoodEntity(id = newFoodId, ownerId = app.nutrition.owner, name = name, nutrients = Nutrients())).copy(
          name = name, brand = brand, barcode = code, basisQuantity = number(basis), basisUnit = unit,
          nutrients = Nutrients(n[0], n[1], n[2], n[3], n[4], n[5], n[6]))
        val saved = app.nutrition.saveFood(food)
        nav.navigate(foodPath(date, meal, saved.id)) { popUpTo("nutrition/edit/{date}/{meal}/{id}?barcode={barcode}&quick={quick}") { inclusive = true } }
      } }, enabled = !actions.busy) { Text("Save food") } }
    }
  }
}
