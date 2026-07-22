package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.util.ExcelHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: User,
    onNavigateToCourses: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToCompanies: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToGenerate: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var workersCount by remember { mutableStateOf("–") }
    var dc3Count by remember { mutableStateOf("–") }
    var companiesCount by remember { mutableStateOf("–") }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val count = if (user.role == "ADMIN") fetched.size else fetched.count { it.creatorEmail == user.email }
            workersCount = count.toString()
        }
        SupabaseRepository.fetchData("dc3_records", DC3Record.serializer()) { dc3Count = it.size.toString() }
        SupabaseRepository.fetchData("companies", Company.serializer()) { companiesCount = it.size.toString() }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val workers = ExcelHelper.readWorkersFromExcel(context, it)
                    val workersWithEmail = workers.map { w -> w.copy(creatorEmail = user.email) }
                    SupabaseRepository.insertWorkers(workersWithEmail) { success -> 
                        if (success) {
                             SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
                                 val count = if (user.role == "ADMIN") fetched.size else fetched.count { it.creatorEmail == user.email }
                                 workersCount = count.toString()
                             }
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Personal", workersCount, modifier = Modifier.weight(1f))
                StatCard("DC-3", dc3Count, modifier = Modifier.weight(1f))
                StatCard("Empresas", companiesCount, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardButton("Personal", Icons.Default.People, onNavigateToEmployees)
                DashboardButton("Empresas", Icons.Default.Business, onNavigateToCompanies)
                DashboardButton("Cursos", Icons.Default.Book, onNavigateToCourses)
                DashboardButton("Generar DC-3", Icons.Default.AddChart, onNavigateToGenerate)
                DashboardButton("Historial DC-3", Icons.Default.History, onNavigateToHistory)
                DashboardButton("Agentes Cap.", Icons.Default.Badge, onNavigateToAgents)
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text)
    }
}
