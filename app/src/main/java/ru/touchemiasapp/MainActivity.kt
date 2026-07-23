package ru.touchemiasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.touchemiasapp.data.auth.AuthRepository
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import ru.touchemiasapp.ui.navigation.NavGraph
import ru.touchemiasapp.ui.theme.TouchEmiasTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userPrefsDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TouchEmiasTheme {
                NavGraph(authRepository, userPrefsDataStore)
            }
        }
    }
}
