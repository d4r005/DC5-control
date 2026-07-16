package com.example.dc5control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.dc5control.ui.*

enum class Screen {
    Login, Dashboard, CourseList, AgentList, DC3Generation, DC3History
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf(Screen.Login) }

    when (currentScreen) {
        Screen.Login -> LoginScreen(onLoginSuccess = { currentScreen = Screen.Dashboard })
        Screen.Dashboard -> DashboardScreen(
            onNavigateToCourses = { currentScreen = Screen.CourseList },
            onNavigateToAgents = { currentScreen = Screen.AgentList },
            onNavigateToGenerate = { currentScreen = Screen.DC3Generation },
            onNavigateToHistory = { currentScreen = Screen.DC3History }
        )
        Screen.CourseList -> CourseListScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.AgentList -> AgentListScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.DC3Generation -> DC3GenerationScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.DC3History -> DC3HistoryScreen(onBack = { currentScreen = Screen.Dashboard })
    }
}
