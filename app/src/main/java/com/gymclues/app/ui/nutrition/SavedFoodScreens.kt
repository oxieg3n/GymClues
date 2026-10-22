package com.gymclues.app.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.gymclues.app.data.*
import com.gymclues.app.data.nutrition.*
import com.gymclues.app.ui.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable fun SavedFoodsScreen(app: AppContainer, nav: NavHostController, date: String, initialMeal: String) {
  val bundles by remember(app) { app.nutrition.bundles() }.collectAsState(emptyList())
  var meal by rememberSaveable { mutableStateOf(initialMeal) }
  val actions = rememberActions()
  var selected by remember { mutableStateOf<FoodBundle?>(null) }
  var servings by remember { mutableStateOf("1") }
  val token = rememberSaveable(selected?.id) { newId() }
  NutritionPage {
    item { ScreenTitle("Meals & recipes", "Reusable foods · $date") }
    item { Text("Add to meal"); Options(MEALS, meal) { meal = it }; ActionMessage(actions) }
    item { AppButton({ nav.navigate(bundlePath("meal", date, "none")) }) { Text("Create saved meal") }
      AppOutlinedButton({ nav.navigate(bundlePath("recipe", date, "none")) }) { Text("Create recipe") } }
    if (bundles.isEmpty()) item { Text("No saved meals or recipes yet. Create one, or save foods from a diary meal.") }
    bundles.forEach { bundle -> item { AppCard(Modifier.fillMaxWidth()) {
      Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(bundle.name, style = MaterialTheme.typography.titleMedium)
        Muted(if (bundle.kind == "recipe") "Recipe · ${amount(bundle.servings)} servings" else "Saved meal")
        AppButton({ selected = bundle; servings = "1" }, enabled = !actions.busy) { Text("Add ${bundle.name}") }
        AppOutlinedButton({ nav.navigate(bundlePath(bundle.kind, date, meal, bundle.id)) }) { Text("Edit") }
        TextButton({ actions.run { app.nutrition.deleteBundle(bundle.id) } }, enabled = !actions.busy) { Text("Delete saved ${bundle.kind}") }
      }
    } } }
  }
  selected?.let { bundle -> NutritionDialog(onDismissRequest = { selected = null }, title = { Text("Add ${bundle.name}") },
    text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("$meal · $date"); DecimalField(if (bundle.kind == "recipe") "Recipe servings" else "Meal copies", servings) { servings = it }; ActionMessage(actions) } },
    confirmButton = { TextButton({ actions.run({ selected = null; actions.message = "Added to $meal" }) { app.nutrition.logBundle(bundle.id, date, meal, number(servings), token) } }, enabled = !actions.busy) { Text("Add to diary") } },
    dismissButton = { TextButton({ selected = null }, enabled = !actions.busy) { Text("Cancel") } }) }
}

@Composable fun BundleEditorScreen(app: AppContainer, nav: NavHostController, kind: String, date: String, meal: String, id: String) {
  val actions = rememberActions()
  val bundleId = rememberSaveable { if (id == "new") newId() else id }
  var loaded by rememberSaveable { mutableStateOf(false) }
  var name by rememberSaveable { mutableStateOf("") }
  var servings by rememberSaveable { mutableStateOf("1") }
  var instructions by rememberSaveable { mutableStateOf("") }
  var itemJson by rememberSaveable { mutableStateOf("[]") }
  val items = remember(itemJson) { Json.decodeFromString<List<BundleItem>>(itemJson) }
  fun change(list: List<BundleItem>) { itemJson = Json.encodeToString(list) }
  var picking by remember { mutableStateOf(false) }
  var food by remember { mutableStateOf<FoodEntity?>(null) }
  var portionIndex by remember { mutableIntStateOf(0) }
  var quantity by remember { mutableStateOf("1") }
  var editing by remember { mutableStateOf<Int?>(null) }
  var editQuantity by remember { mutableStateOf("") }
  LaunchedEffect(id) { if (!loaded) {
    if (id != "new") app.nutrition.bundle(id)?.let { (bundle, ingredients) ->
      name = bundle.name; servings = bundle.servings.toString(); instructions = bundle.instructions; change(ingredients)
    } else if (meal in MEALS) change(app.nutrition.dao.dayOnce(app.nutrition.owner, date).filter { it.meal == meal }.map {
      BundleItem(bundleId = bundleId, snapshot = it.snapshot, quantity = it.quantity, unit = it.unit, canonicalPerUnit = it.canonicalPerUnit)
    })
    loaded = true
  } }
  NutritionPage {
    item { ScreenTitle(if (kind == "recipe") "Recipe" else "Saved meal", "Ingredients keep their nutrition and source information.") }
    item { AppTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }) }
    if (kind == "recipe") {
      item { DecimalField("Number of recipe servings", servings) { servings = it } }
      item { AppTextField(instructions, { instructions = it }, Modifier.fillMaxWidth(), label = { Text("Instructions (optional)") }) }
    }
    items.forEachIndexed { index, item -> item(key = item.id) {
      AppCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(item.snapshot.name, style = MaterialTheme.typography.titleMedium)
        Text("${amount(item.quantity)} × ${item.unit}"); NutrientValues(item.total); Muted(item.snapshot.license)
        AppOutlinedButton({ editing = index; editQuantity = item.quantity.toString() }) { Text("Edit ingredient quantity") }
        TextButton({ change(items.filterIndexed { i, _ -> i != index }) }) { Text("Remove ingredient") }
      } }
    } }
    item { AppOutlinedButton({ picking = true }) { Text("Add ingredient") } }
    item { val n = nutritionTotal(items.map { it.total }); val count = servings.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
      Text(if (kind == "recipe") "Per recipe serving" else "Entire meal", style = MaterialTheme.typography.titleMedium)
      if (kind != "recipe" || count != null) NutrientValues(if (kind == "recipe") n.scale(1 / count!!) else n) }
    item { ActionMessage(actions) }
    item { AppButton({ actions.run({ nav.popBackStack() }) {
      app.nutrition.saveBundle(FoodBundle(bundleId, app.nutrition.owner, name, kind, if (kind == "recipe") number(servings) else 1.0, instructions), items)
    } }, enabled = loaded && !actions.busy) { Text(if (kind == "recipe") "Save recipe" else "Save meal") } }
  }
  if (picking) Dialog(onDismissRequest = { picking = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer,
      modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(.92f).heightIn(max = (LocalConfiguration.current.screenHeightDp * .85).dp)) {
      NutritionPage {
        item { TextButton({ picking = false }) { Text("Close ingredient search") }; FoodSearchContent(app) { selected -> food = selected; portionIndex = 0; quantity = "1"; picking = false } }
      }
    }
  }
  food?.let { selected ->
    val portions = portions(selected); val portion = portions.getOrElse(portionIndex) { portions.first() }
    NutritionDialog(onDismissRequest = { food = null }, title = { Text(selected.name) }, text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { DecimalField("Ingredient quantity", quantity) { quantity = it }
        Options(portions.map { it.label }, portion.label) { value -> portionIndex = portions.indexOfFirst { it.label == value } }; ActionMessage(actions) }
    }, confirmButton = { TextButton({ actions.run({ food = null }) {
      val qty = number(quantity); validQuantity(qty)
      change(items + BundleItem(bundleId = bundleId, snapshot = selected.snapshot(), quantity = qty, unit = portion.label, canonicalPerUnit = portion.amount))
    } }) { Text("Add ingredient") } }, dismissButton = { TextButton({ food = null }) { Text("Cancel") } })
  }
  editing?.let { index -> NutritionDialog(onDismissRequest = { editing = null }, title = { Text("Ingredient quantity") },
    text = { Column { DecimalField("Quantity", editQuantity) { editQuantity = it }; ActionMessage(actions) } },
    confirmButton = { TextButton({ actions.run({ editing = null }) { val qty = number(editQuantity); validQuantity(qty); change(items.mapIndexed { i, item -> if (i == index) item.copy(quantity = qty) else item }) } }) { Text("Save quantity") } },
    dismissButton = { TextButton({ editing = null }) { Text("Cancel") } }) }
}
