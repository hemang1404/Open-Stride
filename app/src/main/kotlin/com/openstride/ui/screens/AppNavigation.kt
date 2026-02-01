package com.openstride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.openstride.ui.theme.*

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Tracking : Screen("tracking", Icons.Default.PlayArrow, "Record")
    object History : Screen("history", Icons.Default.List, "Activities")
    object Settings : Screen("settings", Icons.Default.Settings, "Settings")
    object Detail : Screen("detail/{sessionId}", Icons.Default.Info, "Detail")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            // Only show bottom bar on top-level screens
            if (currentRoute in listOf(Screen.Tracking.route, Screen.History.route, Screen.Settings.route)) {
                NavigationBar(
                    containerColor = StravaLight,
                    contentColor = StravaOrange,
                    tonalElevation = 8.dp
                ) {
                    listOf(Screen.Tracking, Screen.History, Screen.Settings).forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label, fontWeight = FontWeight.Bold, fontSize = 10.sp) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = StravaOrange,
                                selectedTextColor = StravaOrange,
                                unselectedIconColor = StravaTextSecondary,
                                unselectedTextColor = StravaTextSecondary,
                                indicatorColor = StravaOrange.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tracking.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Tracking.route) { MainScreen() }
            composable(Screen.History.route) { 
                HistoryScreen(onSessionClick = { id -> 
                    navController.navigate("detail/$id")
                }) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen()
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(androidx.navigation.navArgument("sessionId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                SessionDetailScreen(sessionId = sessionId)
            }
        }
    }
}
