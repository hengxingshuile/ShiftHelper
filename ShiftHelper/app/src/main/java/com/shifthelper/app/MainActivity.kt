package com.shifthelper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shifthelper.app.data.ScheduleRepository
import com.shifthelper.app.ui.screens.CalendarScreen
import com.shifthelper.app.ui.screens.HomeScreen
import com.shifthelper.app.ui.screens.ImportScreen
import com.shifthelper.app.ui.screens.SettingsScreen
import com.shifthelper.app.ui.theme.ShiftHelperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as ShiftHelperApp).repository

        setContent {
            ShiftHelperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShiftHelperAppNavigation(repository)
                }
            }
        }
    }
}

@Composable
fun ShiftHelperAppNavigation(repository: ScheduleRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                repository = repository,
                onNavigateToCalendar = { navController.navigate("calendar") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToImport = { navController.navigate("import") }
            )
        }
        composable("calendar") {
            CalendarScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("import") {
            ImportScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
