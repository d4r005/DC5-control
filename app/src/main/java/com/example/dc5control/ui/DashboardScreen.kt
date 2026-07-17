package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.util.ExcelHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToAgents: () -> Unit,
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
                    SupabaseRepository.insertWorkers(workers) { success ->
                        // TODO: Mostrar Snackbar de éxito o error
                    }
                } catch (e: Exception) {
                }
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
            DashboardButton(
                text = "Catálogo de Cursos",
                icon = Icons.Default.List,
                onClick = onNavigateToCourses
            )
            DashboardButton(
                text = "Agentes Capacitadores",
                icon = Icons.Default.Person,
                onClick = onNavigateToAgents
            )
            DashboardButton(
                text = "Cargar Excel de Personal",
                icon = Icons.Default.Upload,
                onClick = { launcher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") }
            )
            DashboardButton(
                text = "Generar DC-3",
                icon = Icons.Default.Add,
                onClick = onNavigateToGenerate
            )
            DashboardButton(
                text = "Ver Historial (PDFs)",
                icon = Icons.Default.PictureAsPdf,
                onClick = onNavigateToHistory
            )
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
