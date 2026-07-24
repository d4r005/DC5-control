package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.dc5control.data.model.DC3Record
import com.example.dc5control.data.model.Employee
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesForEmployeeScreen(
    employee: Employee,
    user: User,
    onBack: () -> Unit,
    onGenerateDC3: (Employee) -> Unit
) {
    val context = LocalContext.current
val dc3Records = remember { mutableStateListOf<DC3Record>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchDataFiltered("dc3_records", "worker_id=eq.${employee.id}&order=created_at.desc", DC3Record.serializer()) { list ->
            dc3Records.clear(); dc3Records.addAll(list)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${employee.nombres} ${employee.apellidoPaterno}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text("Constancias DC-3", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = { onGenerateDC3(employee) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Generar DC-3", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Resumen del trabajador
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!employee.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(employee.photoUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                                .then(Modifier.padding(end = 12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(56.dp).padding(end = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null,
                                modifier = Modifier.size(40.dp), tint = NavyLight)
                        }
                    }
                    Column {
                        Text("CURP: ${employee.curp ?: "N/A"}", style = MaterialTheme.typography.bodySmall.copy(color = Gray700))
                        Text("Puesto: ${employee.position ?: "N/A"}", style = MaterialTheme.typography.bodySmall.copy(color = Gray700))
                        Text("Constancias emitidas: ${dc3Records.size}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary))
                    }
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NavyPrimary)
                }
            } else if (dc3Records.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = Gray400)
                        Spacer(Modifier.height(8.dp))
                        Text("Sin constancias DC-3", style = MaterialTheme.typography.titleMedium.copy(color = Gray500))
                        Spacer(Modifier.height(4.dp))
                        Text("Usa el botón 'Generar DC-3' para crear una", style = MaterialTheme.typography.bodySmall.copy(color = Gray400))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dc3Records) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null,
                                    tint = NavyPrimary, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(record.courseName.ifBlank { "Curso" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Gray900))
                                    Text("${record.startDate ?: ""} – ${record.endDate ?: ""}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500))
                                    Text("Agente: ${record.agentName ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
