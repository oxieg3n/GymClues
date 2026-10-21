package com.gymclues.app.ui
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gymclues.app.data.AppContainer

/** Route helpers so every screen builds URLs the same way (and encodes user text safely). */
object Routes {
  fun session(id: String) = "session/${Uri.encode(id)}"
  fun list(m: String) = "list/${Uri.encode(m)}"
  fun exercise(id: String) = "ex/${Uri.encode(id)}"
  fun equipment(id: String, exerciseId: String? = null) = "equip/${Uri.encode(id)}" + (exerciseId?.let { "?ex=${Uri.encode(it)}" } ?: "")
  fun runTemplate(id: String) = "run/template/${Uri.encode(id)}"
  fun runCustom(id: String) = "run/custom/${Uri.encode(id)}"
  fun builder(id: String? = null) = "builder" + (id?.let { "?id=${Uri.encode(it)}" } ?: "")
  fun runProgram(planId: String) = "run/program/${Uri.encode(planId)}"
  fun programType(split: String) = "programs/${Uri.encode(split)}"
  fun program(id: String) = "program/${Uri.encode(id)}"
  fun programDay(planId: String, date: String?) = "pday/${Uri.encode(planId)}" + (date?.let { "?date=$it" } ?: "")
  fun addExercise(id: String? = null) = "addex" + (id?.let { "?id=${Uri.encode(it)}" } ?: "")
  fun gym(id: String) = "gym/${Uri.encode(id)}"
  fun addEquipment(gymId: String, editId: String? = null) = "gym/${Uri.encode(gymId)}/add" + (editId?.let { "?edit=${Uri.encode(it)}" } ?: "")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun App(app: AppContainer) {
  val nav = rememberNavController()
  val backStackEntry by nav.currentBackStackEntryAsState()
  val route = backStackEntry?.destination?.route
  val off by app.dao.unavailable().collectAsState(emptyList())
  val offIds = off.map { it.equipmentId }.toSet()
  val prefs = LocalContext.current.getSharedPreferences("ui", android.content.Context.MODE_PRIVATE)
  val tour = remember { TourState(if (prefs.getBoolean("tourDone", false)) -1 else 0) }
  var menuOpen by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) { app.session.ensureUser(); app.workouts.adoptMigrated() }
  LaunchedEffect(tour.step) { if (tour.step < 0) prefs.edit().putBoolean("tourDone", true).apply() }
  val touring = tour.step >= 0 && route == "home"
  val menuVisible = if (touring) tour.target == "menu" else menuOpen
  CompositionLocalProvider(LocalTour provides tour.takeIf { touring }) {
  Scaffold { pad ->
    BoxWithConstraints(Modifier.fillMaxSize().padding(pad)) {
      val headerHeight = maxOf(maxHeight / 6, 96.dp)   // header + music controls: one sixth of the screen on every page
      Column(Modifier.fillMaxSize()) {
        AppHeader(app, showBack = route != null && route != "home", onBack = { nav.popBackStack() },
          openMenu = { menuOpen = !menuOpen }, go = { menuOpen = false; nav.navigate(it) }, modifier = Modifier.fillMaxWidth().height(headerHeight))
        Box(Modifier.weight(1f).fillMaxWidth()) {
          NavHost(nav, "home") {
      composable("home") { Home(app, nav::navigate) }
      composable("muscles") { ListScreen("Choose a primary muscle group", app.catalog.muscles, { nav.navigate(Routes.addExercise()) }) { nav.navigate(Routes.list(it)) } }
      composable("list/{m}") { val m = it.arguments?.getString("m").orEmpty()
        ExerciseList(app.catalog.exercises.filter { e -> m in e.primaryMuscles && app.catalog.usable(e, offIds) }, title = m) { id -> nav.navigate(Routes.exercise(id)) } }
      composable("ex/{id}") { val e = app.catalog.exercise(it.arguments?.getString("id").orEmpty())
        if (e == null) NotFound("Exercise not found.") else ExerciseDetail(app, e, offIds, nav) }
      composable("equip/{id}?ex={ex}", listOf(navArgument("ex") { type = NavType.StringType; nullable = true; defaultValue = null })) {
        EquipmentDetail(app, it.arguments?.getString("id").orEmpty(), it.arguments?.getString("ex"), nav) }
      composable("styles") { Workouts(app, nav) }
      composable("run/{kind}/{id}") { ActiveWorkout(app, it.arguments?.getString("kind").orEmpty(), it.arguments?.getString("id").orEmpty(), offIds, nav) {
        nav.popBackStack("home", false) } }
      composable("builder?id={id}", listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })) {
        WorkoutBuilder(app, it.arguments?.getString("id"), offIds, { nav.navigate(Routes.addExercise()) }) { nav.popBackStack() } }
      composable("equipment") { EquipmentScreen(app, offIds, nav) }
      composable("sessions") { CompletedSessions(app, nav) }
      composable("session/{id}") { SessionDetail(app, it.arguments?.getString("id").orEmpty(), nav) }
      composable("gyms") { GymSearch(app, nav) }
      composable("gym/{id}") { GymDetail(app, it.arguments?.getString("id").orEmpty(), nav) }
      composable("gym/{id}/add?edit={edit}", listOf(navArgument("edit") { type = NavType.StringType; nullable = true; defaultValue = null })) {
        AddGymEquipment(app, it.arguments?.getString("id").orEmpty(), it.arguments?.getString("edit")) { nav.popBackStack() } }
      composable("programs") { ProgramTypesScreen(app, nav) }
      composable("programs/{split}") { ProgramsScreen(app, nav, it.arguments?.getString("split").orEmpty()) }
      composable("program/{id}") { ProgramDetail(app, it.arguments?.getString("id").orEmpty(), nav) }
      composable("pday/{id}?date={date}", listOf(navArgument("date") { type = NavType.StringType; nullable = true; defaultValue = null })) {
        ProgramDayScreen(app, it.arguments?.getString("id").orEmpty(), it.arguments?.getString("date"), nav) }
      composable("restdays") { RestDaysScreen(app) }
      composable("week") { WeekCalendar(app, nav::navigate) }
      composable("month") { MonthCalendar(app, nav::navigate) }
      composable("myex") { MyExercises(app, nav) }
      composable("addex?id={id}", listOf(navArgument("id") { type = NavType.StringType; nullable = true; defaultValue = null })) {
        AddExercise(app, it.arguments?.getString("id")) { nav.popBackStack() } }
      composable("admin") { AdminHome(app, nav) }
          }
          if (menuVisible) {
            Box(Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { menuOpen = false })
            AppMenu(app, { menuOpen = false; nav.navigate(it) }, Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 2.dp))
          }
        }
      }
      if (touring) Walkthrough(tour) { tour.next() }
    }
  }
  }
}

@Composable fun NotFound(msg: String) = Text(msg, Modifier.padding(androidx.compose.ui.unit.Dp(16f)), style = MaterialTheme.typography.titleMedium)
