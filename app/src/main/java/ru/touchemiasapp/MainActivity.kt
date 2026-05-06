package ru.touchemiasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.touchemiasapp.ui.navigation.NavGraph
import ru.touchemiasapp.ui.theme.TouchEmiasTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TouchEmiasTheme {
                NavGraph()
            }
        }
    }
}
