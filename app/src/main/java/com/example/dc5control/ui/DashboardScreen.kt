package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.util.ExcelHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToAgents: () -> Unit,
    onNavigateToCompanies: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToGenerate: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val workers = ExcelHelper.readWorkersFromExcel(context, it)
                    SupabaseRepository.insertWorkers(workers) { success -> }
                } catch (e: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Panel de Control DC5") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashboardButton("Empresas", Icons.Default.Business, onNavigateToCompanies)
            DashboardButton("Personal", Icons.Default.AccountBox, onNavigateToEmployees)
            DashboardButton("Catálogo de Cursos", Icons.Default.List, onNavigateToCourses)
            DashboardButton("Agentes Capacitadores", Icons.Default.Person, onNavigateToAgents)
            DashboardButton(
                "Cargar Excel de Personal",
                Icons.Default.Upload,
                { launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") }
            )
            DashboardButton("Generar DC-3", Icons.Default.Add, onNavigateToGenerate)
            DashboardButton("Ver Historial (PDFs)", Icons.Default.PictureAsPdf, onNavigateToHistory)
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text)
    }
}
