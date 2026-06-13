package com.naliendev.achieveit.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naliendev.achieveit.ui.screens.GameDetailsScreen
import com.naliendev.achieveit.ui.screens.HomeScreen
import com.naliendev.achieveit.ui.screens.LibraryScreen
import com.naliendev.achieveit.ui.screens.LoginScreen
import com.naliendev.achieveit.ui.screens.SettingsScreen
import com.naliendev.achieveit.ui.theme.BackgroundDark
import com.naliendev.achieveit.ui.theme.PurplePrimary
import com.naliendev.achieveit.ui.theme.TextSecondary
import com.naliendev.achieveit.ui.theme.SurfaceDark
import com.google.firebase.auth.FirebaseAuth
import com.naliendev.achieveit.ui.screens.TrophieScreen

enum class Screen(val route: String, val title: String, val icon: ImageVector?) {
    Login("login", "Login", null),
    Home("home", "Home", Icons.Filled.Home),
    Library("library", "Library", Icons.AutoMirrored.Filled.LibraryBooks),
    Trophie("trophie", "Trophie", Icons.Filled.AutoAwesome),
    Profile("profile", "Profile", Icons.Filled.Person),
    GameDetails("game_details/{gameId}", "Game Details", null),
    Settings("settings", "Settings", null),
    SignUp("signup", "Sign Up", null)
}

@Composable
fun AchieveItApp() {
    val navController = rememberNavController()
    
    val auth = FirebaseAuth.getInstance()
    val startDest = if (auth.currentUser != null) Screen.Home.route else Screen.Login.route
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Bottom bar items
    val items = listOf(
        Screen.Home,
        Screen.Library,
        Screen.Trophie,
        Screen.Profile
    )
    
    val showBottomBar = items.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = BackgroundDark,
                    contentColor = PurplePrimary
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PurplePrimary,
                                unselectedIconColor = TextSecondary,
                                selectedTextColor = PurplePrimary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = SurfaceDark
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    }
                )
            }
            composable(Screen.SignUp.route) {
                com.naliendev.achieveit.ui.screens.SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onGameClick = {
                        // Home screen doesn't have real game IDs yet; navigate without ID
                    },
                    onLogoutClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onGameClick = { gameId ->
                        navController.navigate("game_details/$gameId")
                    },
                    onSettingsClick = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.GameDetails.route,
                arguments = listOf(navArgument("gameId") { type = NavType.StringType })
            ) { backStackEntry ->
                val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
                GameDetailsScreen(
                    gameId = gameId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Trophie.route) {
                TrophieScreen()
            }
            composable(Screen.Profile.route) {
                // Placeholder
            }
        }
    }
}
