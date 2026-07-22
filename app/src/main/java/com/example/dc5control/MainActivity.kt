package com.example.dc5control

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.dc5control.ui.*

enum class Screen {
    Login, Dashboard, CourseList, AgentList, CompanyList, DC3Generation, DC3History, EmployeeList
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(windowSizeClass)
                }
            }
        }
    }
}

@Composable
fun MainApp(windowSizeClass: WindowSizeClass) {
    var currentScreen by remember { mutableStateOf(Screen.Login) }
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    when (currentScreen) {
        Screen.Login -> LoginScreen(onLoginSuccess = { currentScreen = Screen.Dashboard })
        Screen.Dashboard -> DashboardScreen(
            onNavigateToCourses = { currentScreen = Screen.CourseList },
            onNavigateToAgents = { currentScreen = Screen.AgentList },
            onNavigateToCompanies = { currentScreen = Screen.CompanyList },
            onNavigateToEmployees = { currentScreen = Screen.EmployeeList },
            onNavigateToGenerate = { currentScreen = Screen.DC3Generation },
            onNavigateToHistory = { currentScreen = Screen.DC3History }
        )
        Screen.CourseList -> CourseListScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.AgentList -> AgentListScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.CompanyList -> CompanyListScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.EmployeeList -> EmployeeListScreen(onBack = { currentScreen = Screen.Dashboard })
        Screen.DC3Generation -> DC3GenerationScreen(
            onBack = { currentScreen = Screen.Dashboard },
            isExpanded = isExpanded
        )
        Screen.DC3History -> DC3HistoryScreen(onBack = { currentScreen = Screen.Dashboard })
    }
}
