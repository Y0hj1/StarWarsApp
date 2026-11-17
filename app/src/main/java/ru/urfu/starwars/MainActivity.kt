package ru.urfu.starwars

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.urfu.starwars.presentation.navigation.AppNavHost
import ru.urfu.starwars.ui.theme.StarWarsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StarWarsTheme {
                AppNavHost()
            }
        }
    }
}
