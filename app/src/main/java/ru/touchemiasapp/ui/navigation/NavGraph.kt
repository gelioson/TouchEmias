package ru.touchemiasapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.touchemiasapp.data.auth.AuthRepository
import ru.touchemiasapp.data.preferences.UserPreferences
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
    val userPrefs by userPrefsDataStore.userPreferences.collectAsState(initial = UserPreferences())

    // NavHost always starts at Login. LaunchedEffect fires when DataStore emits real values
    // and navigates already-authenticated users to the correct screen without a loading state.
    LaunchedEffect(isLoggedIn, userPrefs.isComplete) {
        if (isLoggedIn) {
            if (userPrefs.isComplete) {
                navController.navigate(Screen.Monitor.route) { popUpTo(0) }
            } else {
                navController.navigate(Screen.OmsEntry.route) { popUpTo(0) }
            }
        }
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
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
