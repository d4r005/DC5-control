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
    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    var companyDropdownExpanded by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var showSignaturePad by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    val courses = remember { mutableStateListOf<Course>() }
    val instructors = remember { mutableStateListOf<Instructor>() }
    val employees = remember { mutableStateListOf<Employee>() }
    val companies = remember { mutableStateListOf<Company>() }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { courses.addAll(it) }
        SupabaseRepository.fetchData("instructors", Instructor.serializer()) { instructors.addAll(it) }
        SupabaseRepository.fetchData("employees", Employee.serializer()) { employees.addAll(it) }
        SupabaseRepository.fetchData("companies", Company.serializer()) { companies.addAll(it) }
    }

    if (showSignaturePad) {
        SignaturePad(
            onSave = { bitmap ->
                scope.launch {
                    isGenerating = true
                    try {
                        withContext(Dispatchers.IO) {
                            val activeEmployees = employees.filter { it.active }
                            activeEmployees.forEach { employee ->
                                val file = PdfGenerator.generateDC3(
                                    context = context,
                                    employee = employee,
                                    course = selectedCourse!!,
                                    instructor = selectedInstructor!!,
                                    companyName = selectedCompany!!.name,
                                    companyRfc = selectedCompany!!.rfc,
                                    companyPatron = selectedCompany!!.patron,
                                    companyRepresentante = selectedCompany!!.representante,
                                    startDate = startDate,
                                    endDate = endDate,
                                    signatureBitmap = bitmap,
                                    logoBitmap = null
                                )

                                // Esperar a que se suba y se guarde el registro
                                CloudflareHelper.uploadPdfSuspend(file)
                                
                                val record = DC3Record(
                                    workerId   = employee.curp,
                                    workerName = "${employee.lastName} ${employee.firstName}",
                                    courseName = selectedCourse!!.name,
                                    companyName = selectedCompany!!.name,
                                    companyRfc = selectedCompany!!.rfc,
                                    companyPatron = selectedCompany!!.patron,
                                    instructorName = selectedInstructor!!.fullName,
                                    startDate  = startDate,
                                    endDate    = endDate
                                )
                                SupabaseRepository.insertDataSuspend("dc3_records", record, DC3Record.serializer())
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("DC3", "Error durante la generación: ${e.message}")
                    } finally {
                        isGenerating = false
                        showSignaturePad = false
                        onBack()
                    }
                }
            },
            onDismiss = { showSignaturePad = false }
        )
    }
else {
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
                        Text("Generando constancias DC-3...")
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
                // EMPRESA (selección de lista)
                // ─────────────────────────────────────────────────────────────
                Text("Empresa", style = MaterialTheme.typography.titleMedium)

                ExposedDropdownMenuBox(
                    expanded = companyDropdownExpanded,
                    onExpandedChange = { companyDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCompany?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar empresa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = companyDropdownExpanded,
                        onDismissRequest = { companyDropdownExpanded = false }
                    ) {
                        if (companies.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No hay empresas registradas") },
                                onClick = { companyDropdownExpanded = false }
                            )
                        }
                        companies.forEach { company ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(company.name)
                                        Text("RFC: ${company.rfc}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedCompany = company
                                    companyDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Mostrar datos del patrón y representante de la empresa seleccionada
                if (selectedCompany != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Patrón o Representante Legal:", style = MaterialTheme.typography.labelMedium)
                            Text(
                                selectedCompany!!.patron.ifBlank { "(no especificado)" },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Representante de los Trabajadores:", style = MaterialTheme.typography.labelMedium)
                            Text(
                                selectedCompany!!.representante ?: "(no especificado)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ─────────────────────────────────────────────────────────────
                // CURSO
                // ─────────────────────────────────────────────────────────────
                Text("Curso", style = MaterialTheme.typography.titleMedium)
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
                // INSTRUCTOR
                // ─────────────────────────────────────────────────────────────
                Text("Agente Capacitador / Instructor", style = MaterialTheme.typography.titleMedium)
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
                    Text(
                        "Se generarán $activeCount constancias DC-3 (empleados activos)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ─────────────────────────────────────────────────────────────
                // BOTÓN
                // ─────────────────────────────────────────────────────────────
                Button(
                    onClick = { showSignaturePad = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCourse != null
                          && selectedInstructor != null
                          && selectedCompany != null
                          && startDate.isNotEmpty()
                          && endDate.isNotEmpty()
                ) {
                    Text("Firmar y Generar DC-3")
                }
            }
        }
    }
}
