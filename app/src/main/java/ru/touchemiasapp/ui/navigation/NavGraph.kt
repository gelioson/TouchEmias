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
import ru.touchemiasapp.ui.doctors.DoctorsScreen
import ru.touchemiasapp.ui.logs.LogsScreen
import ru.touchemiasapp.ui.monitor.MonitorScreen
import ru.touchemiasapp.ui.onboarding.OnboardingScreen
import ru.touchemiasapp.ui.onboarding.OnboardingViewModel
import ru.touchemiasapp.ui.schedule.ScheduleScreen
import ru.touchemiasapp.ui.specialities.SpecialitiesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val isRegistered by onboardingVm.isRegistered.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isRegistered) Screen.Monitor.route else Screen.Onboarding.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = hiltViewModel(),
                onSuccess = { navController.navigate(Screen.Monitor.route) { popUpTo(0) } }
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
                    navController.navigate(Screen.Monitor.route) { popUpTo(Screen.Monitor.route) { inclusive = true } }
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
