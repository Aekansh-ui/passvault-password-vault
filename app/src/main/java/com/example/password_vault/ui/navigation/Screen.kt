package com.example.password_vault.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Group : Screen("group/{groupId}/{groupName}") {
        fun createRoute(groupId: Long, groupName: String) =
            "group/$groupId/${Uri.encode(groupName)}"
    }
    object AccountDetail : Screen("account/{accountId}") {
        fun createRoute(accountId: Long) = "account/$accountId"
    }
    object AddPassword : Screen("add?prefillUrl={prefillUrl}") {
        fun createRoute(prefillUrl: String = "") =
            if (prefillUrl.isBlank()) "add" else "add?prefillUrl=${Uri.encode(prefillUrl)}"
    }
    object UpdatePassword : Screen("update/{accountId}") {
        fun createRoute(accountId: Long) = "update/$accountId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object About : Screen("about")
}
