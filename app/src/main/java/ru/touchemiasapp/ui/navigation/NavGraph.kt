package ru.touchemiasapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.touchemiasapp.data.auth.AuthRepository
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import ru.touchemiasapp.ui.auth.LoginScreen
import ru.touchemiasapp.ui.auth.OmsEntryScreen
import ru.touchemiasapp.ui.doctors.DoctorsScreen
import ru.touchemiasapp.ui.logs.LogsScreen
import ru.touchemiasapp.ui.monitor.MonitorScreen
import ru.touchemiasapp.ui.schedule.ScheduleScreen
import ru.touchemiasapp.ui.specialities.SpecialitiesScreen

@Composable
fun NavGraph(authRepository: AuthRepository, userPrefsDataStore: UserPreferencesDataStore) {
    val navController = rememberNavController()
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = false)
    val userPrefs by userPrefsDataStore.userPreferences.collectAsState(
        initial = ru.touchemiasapp.data.preferences.UserPreferences()
    )

    val startDestination = when {
        !isLoggedIn -> Screen.Login.route
        !userPrefs.isComplete -> Screen.OmsEntry.route
        else -> Screen.Monitor.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onSuccess = {
                    navController.navigate(Screen.OmsEntry.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.OmsEntry.route) {
            OmsEntryScreen(
                viewModel = hiltViewModel(),
                onSaved = {
                    navController.navigate(Screen.Monitor.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.Monitor.route) {
            MonitorScreen(
                viewModel = hiltViewModel(),
                onNavigateSpecialities = { navController.navigate(Screen.Specialities.route) },
                onNavigateLogs = { navController.navigate(Screen.Logs.route) }
            )
        }

        composable(Screen.Specialities.route) {
            SpecialitiesScreen(
                viewModel = hiltViewModel(),
                onSpecialitySelected = { id, name ->
                    navController.navigate(Screen.Doctors.create(id, name))
                }
            )
        }

        composable(
            route = Screen.Doctors.route,
            arguments = listOf(
                navArgument("specialityId") { type = NavType.LongType },
                navArgument("specialityName") { type = NavType.StringType }
            )
        ) {
            DoctorsScreen(
                viewModel = hiltViewModel(),
                onNext = { navController.navigate(Screen.Schedule.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Schedule.route) {
            ScheduleScreen(
                viewModel = hiltViewModel(),
                onStartMonitoring = {
                    navController.navigate(Screen.Monitor.route) {
                        popUpTo(Screen.Monitor.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Logs.route) {
            LogsScreen(
                viewModel = hiltViewModel(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
