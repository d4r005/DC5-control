package com.example.dc5control

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.User
import com.example.dc5control.ui.*
import com.example.dc5control.ui.theme.*

enum class Screen {
    Dashboard, Workers, Companies, Courses, DC3, DC3History, Agents, DC3Design, Users
}

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val navItems = listOf(
    NavItem(Screen.Dashboard, "Dashboard", Icons.Default.Dashboard),
    NavItem(Screen.Workers, "Personal", Icons.Default.People),
    NavItem(Screen.Companies, "Empresas", Icons.Default.Business),
    NavItem(Screen.Courses, "Cursos", Icons.Default.MenuBook),
    NavItem(Screen.DC3, "Constancias", Icons.Default.Description),
    NavItem(Screen.DC3History, "Historial", Icons.Default.History),
    NavItem(Screen.Agents, "Agentes", Icons.Default.Schedule),
    NavItem(Screen.DC3Design, "Diseño DC-3", Icons.Default.Palette)
)

// Items visible only for ADMIN
val adminNavItems = listOf(
    NavItem(Screen.Users, "Usuarios", Icons.Default.AdminPanelSettings)
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            ACEControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundLight
                ) {
                    MainApp(windowSizeClass)
                }
            }
        }
    }
}

@Composable
fun MainApp(windowSizeClass: WindowSizeClass) {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    // Restore session from SharedPreferences on startup
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("ace_session", Context.MODE_PRIVATE)
        val savedEmail = prefs.getString("email", null)
        val savedName = prefs.getString("name", null)
        val savedRole = prefs.getString("role", null)
        if (savedEmail != null && savedName != null && savedRole != null) {
            currentUser = User(name = savedName, email = savedEmail, role = savedRole)
            currentScreen = Screen.Dashboard
        }
    }

    val widthSizeClass = windowSizeClass.widthSizeClass
    val isExpanded = widthSizeClass == WindowWidthSizeClass.Expanded
    val isMedium = widthSizeClass == WindowWidthSizeClass.Medium

    if (currentUser == null) {
        LoginScreen(onLoginSuccess = { user, rememberMe ->
            // Save session if "recordar usuario" is checked
            if (rememberMe) {
                val prefs = context.getSharedPreferences("ace_session", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("email", user.email)
                    .putString("name", user.name)
                    .putString("role", user.role)
                    .apply()
            }
            currentUser = user
            currentScreen = Screen.Dashboard
        })
        return
    }

    val user = currentUser!!
    val navigateTo: (Screen) -> Unit = { currentScreen = it }
    val logout: () -> Unit = {
        val prefs = context.getSharedPreferences("ace_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        currentUser = null
    }
    val goBack: () -> Unit = { currentScreen = Screen.Dashboard }

    when {
        isExpanded -> {
            Row(modifier = Modifier.fillMaxSize()) {
                PermanentNavDrawer(user = user, currentScreen = currentScreen, onNavigate = navigateTo, onLogout = logout)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(currentScreen, user, isExpanded, goBack, navigateTo, logout)
                }
            }
        }
        isMedium -> {
            Row(modifier = Modifier.fillMaxSize()) {
                CompactNavRail(currentScreen = currentScreen, onNavigate = navigateTo)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(currentScreen, user, isExpanded, goBack, navigateTo, logout)
                }
            }
        }
        else -> {
            Scaffold(
                containerColor = BackgroundLight,
                bottomBar = {
                    NavigationBar(
                        containerColor = SurfaceWhite,
                        tonalElevation = 2.dp
                    ) {
                        navItems.take(5).forEach { item ->
                            NavigationBarItem(
                                selected = currentScreen == item.screen,
                                onClick = { currentScreen = item.screen },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, fontSize = 10.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NavyPrimary,
                                    selectedTextColor = NavyPrimary,
                                    indicatorColor = NavySurface,
                                    unselectedIconColor = Gray500,
                                    unselectedTextColor = Gray500
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    ScreenContent(currentScreen, user, isExpanded, goBack, navigateTo, logout)
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    screen: Screen,
    user: User,
    isExpanded: Boolean,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    when (screen) {
        Screen.Dashboard -> DashboardScreen(user = user, onNavigate = onNavigate, onLogout = onLogout)
        Screen.Workers -> EmployeeListScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.Companies -> CompanyListScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.Courses -> CourseListScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.DC3 -> DC3GenerationScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.DC3History -> DC3HistoryScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.Agents -> AgentListScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.DC3Design -> DC3DesignScreen(user = user, isExpanded = isExpanded, onBack = onBack)
        Screen.Users -> UsersScreen(user = user, isExpanded = isExpanded, onBack = onBack)
    }
}

@Composable
fun PermanentNavDrawer(
    user: User,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(SurfaceWhite)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(bottom = 24.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(NavyPrimary, shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("ACE-Control", fontWeight = FontWeight.Bold, color = Gray900, fontSize = 14.sp)
        }

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(if (isSelected) NavySurface else Color.Transparent)
                    .clickable { onNavigate(item.screen) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(18.dp), tint = if (isSelected) NavyPrimary else Gray500)
                Spacer(modifier = Modifier.width(10.dp))
                Text(item.label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) NavyPrimary else Gray500)
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        // Admin-only items
        if (user.role == "ADMIN") {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Gray100)
            Spacer(modifier = Modifier.height(8.dp))
            adminNavItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(if (isSelected) NavySurface else Color.Transparent)
                        .clickable { onNavigate(item.screen) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(18.dp), tint = if (isSelected) NavyPrimary else Gray500)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(item.label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) NavyPrimary else Gray500)
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = Gray100)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(NavySurface, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(user.name.take(2).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                Text(user.role, fontSize = 11.sp, color = Gray400)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { onLogout() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", modifier = Modifier.size(16.dp), tint = ErrorRed)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Cerrar sesión", fontSize = 14.sp, color = ErrorRed)
        }
    }
}

@Composable
fun CompactNavRail(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationRail(
        containerColor = SurfaceWhite,
        contentColor = NavyPrimary
    ) {
        navItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 10.sp) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = NavyPrimary,
                    selectedTextColor = NavyPrimary,
                    indicatorColor = NavySurface,
                    unselectedIconColor = Gray500,
                    unselectedTextColor = Gray500
                )
            )
        }
    }
}
