package com.example.password_vault.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.password_vault.security.SessionManager
import com.example.password_vault.ui.screens.AboutScreen
import com.example.password_vault.ui.screens.AccountDetailScreen
import com.example.password_vault.ui.screens.AddUpdateScreen
import com.example.password_vault.ui.screens.EditProfileScreen
import com.example.password_vault.ui.screens.GroupDetailScreen
import com.example.password_vault.ui.screens.HomeScreen
import com.example.password_vault.ui.screens.ProfileScreen
import com.example.password_vault.ui.screens.SettingsScreen
import com.example.password_vault.ui.screens.SplashScreen
import com.example.password_vault.ui.theme.PassVaultTheme
import kotlinx.coroutines.delay

@Composable
fun PassVaultNavGraph(
    sessionManager: SessionManager
) {
    val navController = rememberNavController()

    val sessionExpired by sessionManager.sessionExpired.collectAsState()
    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            navController.navigate(Screen.Splash.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            sessionManager.checkTimeout()
            delay(15_000L)
        }
    }

    PassVaultTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    while (true) {
                        awaitPointerEventScope {
                            awaitPointerEvent(PointerEventPass.Initial)
                            sessionManager.onUserActivity()
                        }
                    }
                }
        ) {
            NavHost(navController = navController, startDestination = Screen.Splash.route) {

            composable(Screen.Splash.route) {
                SplashScreen(
                    onAuthSuccess = {
                        sessionManager.onAuthenticated()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onAuthFailed = { /* stay on splash or exit handled inside screen */ }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onGroupClick = { groupId, groupName ->
                        navController.navigate(Screen.Group.createRoute(groupId, groupName))
                    },
                    onAddClick = { navController.navigate(Screen.AddPassword.createRoute()) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }

            composable(
                route = Screen.Group.route,
                arguments = listOf(
                    navArgument("groupId") { type = NavType.LongType },
                    navArgument("groupName") { type = NavType.StringType }
                )
            ) { backStack ->
                val groupName = backStack.arguments?.getString("groupName") ?: ""
                GroupDetailScreen(
                    groupName = groupName,
                    onBack = { navController.popBackStack() },
                    onAccountClick = { accountId ->
                        navController.navigate(Screen.AccountDetail.createRoute(accountId))
                    },
                    onAddClick = {
                        navController.navigate(Screen.AddPassword.createRoute(groupName))
                    }
                )
            }

            composable(
                route = Screen.AccountDetail.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                AccountDetailScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToUpdate = { accountId ->
                        navController.navigate(Screen.UpdatePassword.createRoute(accountId))
                    }
                )
            }

            composable(
                route = Screen.AddPassword.route,
                arguments = listOf(
                    navArgument("prefillUrl") { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) {
                AddUpdateScreen(
                    isUpdate = false,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() },
                    onDuplicateFound = { accountId ->
                        navController.navigate(Screen.UpdatePassword.createRoute(accountId)) {
                            popUpTo(Screen.AddPassword.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = Screen.UpdatePassword.route,
                arguments = listOf(navArgument("accountId") { type = NavType.LongType })
            ) {
                AddUpdateScreen(
                    isUpdate = true,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() },
                    onDuplicateFound = { /* shouldn't happen in update */ navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onNavigateEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateAbout = { navController.navigate(Screen.About.route) }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            }
        }
    }
}
