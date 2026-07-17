package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.components.SignaturePad
import com.example.dc5control.util.CloudflareHelper
import com.example.dc5control.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3GenerationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedInstructor by remember { mutableStateOf<Instructor?>(null) }

    // Datos de la empresa
    var companyName by remember { mutableStateOf("") }
    var companyRfc by remember { mutableStateOf("") }
    var companyPatron by remember { mutableStateOf("") }
    var companyRepresentante by remember { mutableStateOf("") }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var showSignaturePad by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    val courses = remember { mutableStateListOf<Course>() }
    val instructors = remember { mutableStateListOf<Instructor>() }
    val employees = remember { mutableStateListOf<Employee>() }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { courses.addAll(it) }
        SupabaseRepository.fetchData("instructors", Instructor.serializer()) { instructors.addAll(it) }
        SupabaseRepository.fetchData("employees", Employee.serializer()) { employees.addAll(it) }
    }

    if (showSignaturePad) {
        SignaturePad(
            onSave = { bitmap ->
                scope.launch {
                    isGenerating = true
                    statusMessage = "Generando constancias DC-3..."
                    withContext(Dispatchers.IO) {
                        employees.filter { it.active }.forEach { employee ->
                            val file = PdfGenerator.generateDC3(
                                context = context,
                                employee = employee,
                                course = selectedCourse!!,
                                instructor = selectedInstructor!!,
                                companyName = companyName,
                                companyRfc = companyRfc,
                                companyPatron = companyPatron,
                                companyRepresentante = companyRepresentante.ifBlank { null },
                                startDate = startDate,
                                endDate = endDate,
                                signatureBitmap = bitmap,  // firma dibujada en pantalla (opcional, sobreescribe asset)
                                logoBitmap = null          // null → carga logo_luber.png desde assets
                            )

                            CloudflareHelper.uploadPdf(
                                file = file,
                                onSuccess = { android.util.Log.d("DC3", "PDF subido: ${file.name}") },
                                onError  = { e -> android.util.Log.e("DC3", "Error al subir: $e") }
                            )

                            val record = DC3Record(
                                workerId   = employee.curp,
                                workerName = "${employee.lastName} ${employee.firstName}",
                                courseName = selectedCourse!!.name,
                                companyName = companyName,
                                startDate  = startDate,
                                endDate    = endDate
                            )
                            SupabaseRepository.insertData("dc3_records", record, DC3Record.serializer()) {}
                        }
                    }
                    isGenerating = false
                    showSignaturePad = false
                    onBack()
                }
            },
            onDismiss = { showSignaturePad = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Generar DC-3") },
                    navigationIcon = { TextButton(onClick = onBack) { Text("←") } }
                )
            }
        ) { padding ->
            if (isGenerating) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(statusMessage.ifBlank { "Generando constancias DC-3..." })
                    }
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ─────────────────────────────────────────────────────────────
                // DATOS DE LA EMPRESA
                // ─────────────────────────────────────────────────────────────
                Text("Datos de la Empresa", style = MaterialTheme.typography.titleMedium)

                TextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nombre o Razón Social") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = companyRfc,
                    onValueChange = { companyRfc = it },
                    label = { Text("RFC de la Empresa") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = companyPatron,
                    onValueChange = { companyPatron = it },
                    label = { Text("Patrón o Representante Legal") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = companyRepresentante,
                    onValueChange = { companyRepresentante = it },
                    label = { Text("Representante de los Trabajadores (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ─────────────────────────────────────────────────────────────
                // CURSO
                // ─────────────────────────────────────────────────────────────
                Text("Curso", style = MaterialTheme.typography.titleMedium)
                if (courses.isEmpty()) {
                    Text("Cargando cursos...", style = MaterialTheme.typography.bodySmall)
                }
                courses.forEach { course ->
                    OutlinedButton(
                        onClick = { selectedCourse = course },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedCourse == course)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${course.name} (${course.duration} h)")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ─────────────────────────────────────────────────────────────
                // AGENTE CAPACITADOR / INSTRUCTOR
                // ─────────────────────────────────────────────────────────────
                Text("Agente Capacitador / Instructor", style = MaterialTheme.typography.titleMedium)
                if (instructors.isEmpty()) {
                    Text("Cargando instructores...", style = MaterialTheme.typography.bodySmall)
                }
                instructors.forEach { instructor ->
                    OutlinedButton(
                        onClick = { selectedInstructor = instructor },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedInstructor == instructor)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${instructor.fullName}${if (instructor.stpsNumber != null) " — ${instructor.stpsNumber}" else ""}")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ─────────────────────────────────────────────────────────────
                // FECHAS
                // ─────────────────────────────────────────────────────────────
                Text("Período de Ejecución", style = MaterialTheme.typography.titleMedium)
                TextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Fecha Inicio (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Fecha Fin (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // ─────────────────────────────────────────────────────────────
                // INFO
                // ─────────────────────────────────────────────────────────────
                val activeCount = employees.count { it.active }
                if (activeCount > 0) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Se generarán $activeCount constancias DC-3 (empleados activos)",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─────────────────────────────────────────────────────────────
                // BOTÓN GENERAR
                // ─────────────────────────────────────────────────────────────
                Button(
                    onClick = { showSignaturePad = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCourse != null
                          && selectedInstructor != null
                          && companyName.isNotEmpty()
                          && companyPatron.isNotEmpty()
                          && startDate.isNotEmpty()
                          && endDate.isNotEmpty()
                ) {
                    Text("Firmar y Generar DC-3")
                }
            }
        }
    }
}
