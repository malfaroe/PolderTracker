package com.mae.poldertracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mae.poldertracker.ui.calendar.CalendarScreen
import com.mae.poldertracker.ui.home.HomeScreen
import com.mae.poldertracker.ui.reminder.ReminderScreen
import com.mae.poldertracker.ui.session.ActiveSessionScreen
import com.mae.poldertracker.ui.session.CloseSessionScreen
import com.mae.poldertracker.ui.stats.StatsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Calendar : Screen("calendar", "Historial", Icons.Default.CalendarMonth)
    object Stats : Screen("stats", "Estadísticas", Icons.Default.BarChart)
}

private val bottomNavItems = listOf(Screen.Home, Screen.Calendar, Screen.Stats)
private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

@Composable
fun PolderTrackerNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartSession = { navController.navigate("active_session") },
                    onNavigateToReminder = { navController.navigate("reminder") }
                )
            }
            composable(Screen.Calendar.route) { CalendarScreen() }
            composable(Screen.Stats.route) { StatsScreen() }

            composable("reminder") {
                ReminderScreen(onNavigateUp = { navController.navigateUp() })
            }

            composable("active_session") {
                ActiveSessionScreen(
                    onSessionFinished = { duration, startTs ->
                        navController.navigate("close_session/$duration/$startTs") {
                            popUpTo("active_session") { inclusive = true }
                        }
                    },
                    onNavigateUp = { navController.navigateUp() }
                )
            }

            composable(
                "close_session/{duration}/{startTs}",
                arguments = listOf(
                    navArgument("duration") { type = NavType.IntType },
                    navArgument("startTs") { type = NavType.LongType }
                )
            ) { backStack ->
                val duration = backStack.arguments?.getInt("duration") ?: 0
                val startTs = backStack.arguments?.getLong("startTs") ?: 0L
                CloseSessionScreen(
                    durationSeconds = duration,
                    startTimestamp = startTs,
                    onSaved = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
