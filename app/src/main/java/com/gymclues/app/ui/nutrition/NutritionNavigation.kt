package com.gymclues.app.ui.nutrition

import android.net.Uri
import androidx.navigation.*
import androidx.navigation.compose.composable
import com.gymclues.app.data.AppContainer
import java.time.LocalDate

internal fun foodPath(date: String, meal: String, id: String) = "nutrition/food/$date/${Uri.encode(meal)}/${Uri.encode(id)}"
internal fun searchPath(date: String, meal: String) = "nutrition/search/$date/${Uri.encode(meal)}"
internal fun editorPath(date: String, meal: String, id: String = "new", barcode: String = "", quick: Boolean = false) =
  "nutrition/edit/$date/${Uri.encode(meal)}/$id?barcode=${Uri.encode(barcode)}&quick=$quick"
internal fun bundlePath(kind: String, date: String, meal: String = "Snacks", id: String = "new") = "nutrition/bundle/$kind/$date/${Uri.encode(meal)}/$id"

fun NavGraphBuilder.nutritionRoutes(app: AppContainer, nav: NavHostController) {
  composable("nutrition?date={date}", listOf(navArgument("date") { nullable = true; defaultValue = null })) {
    NutritionDashboard(app, nav, it.arguments?.getString("date"))
  }
  composable("nutrition/search/{date}/{meal}") { FoodSearchScreen(app, nav, it.arguments!!.getString("date")!!, it.arguments!!.getString("meal")!!) }
  composable("nutrition/food/{date}/{meal}/{id}") { FoodDetailScreen(app, nav, it.arguments!!.getString("date")!!, it.arguments!!.getString("meal")!!, it.arguments!!.getString("id")!!) }
  composable("nutrition/edit/{date}/{meal}/{id}?barcode={barcode}&quick={quick}", listOf(navArgument("barcode") { defaultValue = "" }, navArgument("quick") { defaultValue = "false" })) {
    FoodEditorScreen(app, nav, it.arguments!!.getString("date")!!, it.arguments!!.getString("meal")!!, it.arguments!!.getString("id")!!,
      it.arguments!!.getString("barcode").orEmpty(), it.arguments!!.getString("quick") == "true")
  }
  composable("nutrition/goals/{date}") { NutritionGoalsScreen(app, it.arguments!!.getString("date")!!) }
  composable("nutrition/saved/{date}/{meal}") { SavedFoodsScreen(app, nav, it.arguments!!.getString("date")!!, it.arguments!!.getString("meal")!!) }
  composable("nutrition/bundle/{kind}/{date}/{meal}/{id}") { BundleEditorScreen(app, nav, it.arguments!!.getString("kind")!!, it.arguments!!.getString("date")!!,
    it.arguments!!.getString("meal")!!, it.arguments!!.getString("id")!!) }
  composable("nutrition/history") { NutritionHistoryScreen(app, nav) }
  composable("nutrition/health/{date}") { NutritionHealthScreen(app, it.arguments!!.getString("date")!!) }
}
