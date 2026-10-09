package com.comiditas.familia.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.comiditas.familia.ui.screens.calendar.CalendarScreen
import com.comiditas.familia.ui.screens.home.HomeScreen
import com.comiditas.familia.ui.screens.meals.MealsScreen
import com.comiditas.familia.ui.screens.members.MembersScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Members : Screen("members")
    data object Meals : Screen("meals")
    data object Calendar : Screen("calendar")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.Members.route) {
            MembersScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Meals.route) {
            MealsScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
