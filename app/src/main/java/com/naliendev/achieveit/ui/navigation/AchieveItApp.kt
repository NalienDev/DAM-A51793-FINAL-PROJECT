package com.naliendev.achieveit.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.naliendev.achieveit.ui.screens.GameDetailsScreen
import com.naliendev.achieveit.ui.screens.HomeScreen
import com.naliendev.achieveit.ui.screens.LibraryScreen
import com.naliendev.achieveit.ui.screens.LoginScreen
import com.naliendev.achieveit.ui.theme.BackgroundDark
import com.naliendev.achieveit.ui.theme.PurplePrimary
import com.naliendev.achieveit.ui.theme.TextSecondary
import com.naliendev.achieveit.ui.theme.SurfaceDark
import com.google.firebase.auth.FirebaseAuth

enum class Screen(val route: String, val title: String, val icon: ImageVector?) {
    Login("login", "Login", null),
    Home("home", "Home", Icons.Filled.Home),
    Library("library", "Library", Icons.Filled.LibraryBooks), /* Actually icon is a library */
    Social("social", "Social", Icons.Filled.Public),
    Profile("profile", "Profile", Icons.Filled.Person),
    GameDetails("game_details", "Game Details", null),
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
        Screen.Social,
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
                        navController.navigate(Screen.GameDetails.route)
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
                    onGameClick = {
                        navController.navigate(Screen.GameDetails.route)
                    }
                )
            }
            composable(Screen.GameDetails.route) {
                GameDetailsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Social.route) {
                // Placeholder
            }
            composable(Screen.Profile.route) {
                // Placeholder
            }
        }
    }
}
