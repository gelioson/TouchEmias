package ru.touchemiasapp.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object PolicySelection : Screen("policy_selection")
    data object Specialities : Screen("specialities")
    data object Doctors : Screen("doctors/{specialityId}/{specialityName}") {
        fun create(specialityId: Long, specialityName: String) =
            "doctors/$specialityId/${specialityName.encode()}"
    }
    data object Schedule : Screen("schedule")
    data object Monitor : Screen("monitor")
    data object Logs : Screen("logs")
}

private fun String.encode() = java.net.URLEncoder.encode(this, "UTF-8")
