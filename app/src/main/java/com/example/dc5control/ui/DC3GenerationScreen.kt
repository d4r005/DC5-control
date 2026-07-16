package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.AppDatabase
import com.example.dc5control.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.AtlasRepository
import com.example.dc5control.ui.components.SignaturePad
import com.example.dc5control.util.PdfGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3GenerationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedAgent by remember { mutableStateOf<TrainingAgent?>(null) }
    var companyName by remember { mutableStateOf("") }
    var companyRfc by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    
    var showSignaturePad by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    val courses = remember { mutableStateListOf<Course>() }
    val agents = remember { mutableStateListOf<TrainingAgent>() }

    LaunchedEffect(Unit) {
        AtlasRepository.fetchData("courses", Course.serializer()) { fetchedCourses ->
            courses.addAll(fetchedCourses)
        }
        AtlasRepository.fetchData("agents", TrainingAgent.serializer()) { fetchedAgents ->
            agents.addAll(fetchedAgents)
        }
    }

    if (showSignaturePad) {
        SignaturePad(
            onSave = { bitmap ->
                scope.launch {
                    isUploading = true
                    AtlasRepository.fetchData("workers", Worker.serializer()) { workers ->
                        workers.forEach { worker ->
                            PdfGenerator.generateDC3(
                                context, worker, selectedCourse!!, selectedAgent!!,
                                companyName, companyRfc, startDate, endDate
                            )
                            // Guardar registro en MongoDB para las métricas
                            val record = DC3Record(
                                workerId = worker.curp,
                                companyName = companyName,
                                courseName = selectedCourse!!.name,
                                agentName = selectedAgent!!.name,
                                startDate = startDate,
                                endDate = endDate
                            )
                            AtlasRepository.insertData("dc3_records", record, DC3Record.serializer()) { }
                        }
                        isUploading = false
                        showSignaturePad = false
                        onBack()
                    }
                }
            },
            onDismiss = { showSignaturePad = false }
        )
    } else {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Generar DC-3") }, navigationIcon = { IconButton(onClick = onBack) { Text("<-") } }) }
        ) { padding ->
            if (isUploading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Datos de la Empresa", style = MaterialTheme.typography.titleMedium)
                TextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Nombre o Razón Social") }, modifier = Modifier.fillMaxWidth())
                TextField(value = companyRfc, onValueChange = { companyRfc = it }, label = { Text("RFC") }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Curso y Agente", style = MaterialTheme.typography.titleMedium)
                
                Text("Cursos disponibles:")
                courses.forEach { course ->
                    OutlinedButton(onClick = { selectedCourse = course }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if(selectedCourse == course) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) { Text(course.name) }
                }

                Text("Agente capacitador:")
                agents.forEach { agent ->
                    OutlinedButton(onClick = { selectedAgent = agent }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if(selectedAgent == agent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) { Text(agent.name) }
                }

                TextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio") }, modifier = Modifier.fillMaxWidth())
                TextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSignaturePad = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCourse != null && selectedAgent != null && companyName.isNotEmpty()
                ) {
                    Text("Continuar a Firma y Generar")
                }
            }
        }
    }
}
                // ... (rest of the UI)
                Text("Datos de la Empresa", style = MaterialTheme.typography.titleMedium)
                TextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Nombre o Razón Social") }, modifier = Modifier.fillMaxWidth())
                TextField(value = companyRfc, onValueChange = { companyRfc = it }, label = { Text("RFC") }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Curso y Agente", style = MaterialTheme.typography.titleMedium)
                
                // Example simplified selection
                Text("Cursos disponibles:")
                courses.forEach { course ->
                    OutlinedButton(onClick = { selectedCourse = course }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if(selectedCourse == course) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) { Text(course.name) }
                }

                Text("Agente capacitador:")
                agents.forEach { agent ->
                    OutlinedButton(onClick = { selectedAgent = agent }, colors = ButtonDefaults.outlinedButtonColors(containerColor = if(selectedAgent == agent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) { Text(agent.name) }
                }

                TextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Fecha Inicio") }, modifier = Modifier.fillMaxWidth())
                TextField(value = endDate, onValueChange = { endDate = it }, label = { Text("Fecha Fin") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showSignaturePad = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCourse != null && selectedAgent != null && companyName.isNotEmpty()
                ) {
                    Text("Continuar a Firma y Generar")
                }
            }
        }
    }
}
