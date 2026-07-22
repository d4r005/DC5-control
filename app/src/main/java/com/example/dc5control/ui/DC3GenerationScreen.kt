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
fun DC3GenerationScreen(user: User, onBack: () -> Unit, isExpanded: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedAgent by remember { mutableStateOf<Agent?>(null) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }
    var companyDropdownExpanded by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var showSignaturePad by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    val courses = remember { mutableStateListOf<Course>() }
    val agents = remember { mutableStateListOf<Agent>() }
    val employees = remember { mutableStateListOf<Employee>() }
    val companies = remember { mutableStateListOf<Company>() }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { fetched -> 
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            courses.clear(); courses.addAll(filtered) 
        }
        SupabaseRepository.fetchData("agents", Agent.serializer()) { agents.clear(); agents.addAll(it) }
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            employees.clear(); employees.addAll(filtered) 
        }
        SupabaseRepository.fetchData("companies", Company.serializer()) { companies.clear(); companies.addAll(it) }
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
                                    agent = selectedAgent!!,
                                    companyName = selectedCompany!!.name,
                                    companyRfc = selectedCompany!!.rfc,
                                    companyPatron = selectedCompany!!.representanteLegal,
                                    companyRepresentante = selectedCompany!!.representanteTrabajadores,
                                    startDate = startDate,
                                    endDate = endDate,
                                    signatureBitmap = bitmap,
                                    logoBitmap = null
                                )

                                try {
                                    CloudflareHelper.uploadPdfSuspend(file)
                                } catch (e: Exception) {}
                                
                                val record = DC3Record(
                                    workerId   = employee.curp,
                                    workerName = "${employee.apellidoPaterno} ${employee.nombres}",
                                    workerPos  = employee.position,
                                    courseName = selectedCourse!!.name,
                                    durationHours = selectedCourse!!.durationHours,
                                    thematicArea = selectedCourse!!.thematicArea,
                                    companyName = selectedCompany!!.name,
                                    agentName = selectedAgent!!.name,
                                    agentStps = selectedAgent!!.stps,
                                    startDate  = startDate,
                                    endDate    = endDate,
                                    creatorEmail = employee.creatorEmail
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

            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionEmpresa(
                            selectedCompany,
                            companies,
                            onCompanySelected = { selectedCompany = it },
                            expanded = companyDropdownExpanded,
                            onExpandedChange = { companyDropdownExpanded = it }
                        )

                        Text("Curso", style = MaterialTheme.typography.titleMedium)
                        courses.forEach { course ->
                            SelectableButton(
                                text = "${course.name} (${course.durationHours})",
                                isSelected = selectedCourse == course,
                                onClick = { selectedCourse = course }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Agente Capacitador / Instructor", style = MaterialTheme.typography.titleMedium)
                        agents.forEach { agent ->
                            SelectableButton(
                                text = "${agent.name} — ${agent.stps}",
                                isSelected = selectedAgent == agent,
                                onClick = { selectedAgent = agent }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        SectionFechas(
                            startDate, { startDate = it },
                            endDate, { endDate = it }
                        )

                        Spacer(modifier = Modifier.weight(1f))
                        
                        InfoAndGenerateButton(
                            employees.count { it.active },
                            enabled = selectedCourse != null && selectedAgent != null && selectedCompany != null && startDate.isNotEmpty() && endDate.isNotEmpty(),
                            onClick = { showSignaturePad = true }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionEmpresa(
                        selectedCompany,
                        companies,
                        onCompanySelected = { selectedCompany = it },
                        expanded = companyDropdownExpanded,
                        onExpandedChange = { companyDropdownExpanded = it }
                    )

                    Text("Curso", style = MaterialTheme.typography.titleMedium)
                    courses.forEach { course ->
                        SelectableButton(
                            text = "${course.name} (${course.durationHours})",
                            isSelected = selectedCourse == course,
                            onClick = { selectedCourse = course }
                        )
                    }

                    Text("Agente Capacitador / Instructor", style = MaterialTheme.typography.titleMedium)
                    agents.forEach { agent ->
                        SelectableButton(
                            text = "${agent.name} — ${agent.stps}",
                            isSelected = selectedAgent == agent,
                            onClick = { selectedAgent = agent }
                        )
                    }

                    SectionFechas(
                        startDate, { startDate = it },
                        endDate, { endDate = it }
                    )

                    InfoAndGenerateButton(
                        employees.count { it.active },
                        enabled = selectedCourse != null && selectedAgent != null && selectedCompany != null && startDate.isNotEmpty() && endDate.isNotEmpty(),
                        onClick = { showSignaturePad = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionEmpresa(
    selectedCompany: Company?,
    companies: List<Company>,
    onCompanySelected: (Company) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    Text("Empresa", style = MaterialTheme.typography.titleMedium)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedCompany?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleccionar empresa") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            if (companies.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No hay empresas registradas") },
                    onClick = { onExpandedChange(false) }
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
                        onCompanySelected(company)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }

    if (selectedCompany != null) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Patrón o Representante Legal:", style = MaterialTheme.typography.labelMedium)
                Text(selectedCompany.representanteLegal.ifBlank { "(no especificado)" }, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Representante de los Trabajadores:", style = MaterialTheme.typography.labelMedium)
                Text(selectedCompany.representanteTrabajadores ?: "(no especificado)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SectionFechas(
    startDate: String, onStartChange: (String) -> Unit,
    endDate: String, onEndChange: (String) -> Unit
) {
    Text("Período de Ejecución", style = MaterialTheme.typography.titleMedium)
    TextField(
        value = startDate,
        onValueChange = onStartChange,
        label = { Text("Fecha Inicio (dd/MM/yyyy)") },
        modifier = Modifier.fillMaxWidth()
    )
    TextField(
        value = endDate,
        onValueChange = onEndChange,
        label = { Text("Fecha Fin (dd/MM/yyyy)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SelectableButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
fun InfoAndGenerateButton(activeCount: Int, enabled: Boolean, onClick: () -> Unit) {
    if (activeCount > 0) {
        Text(
            "Se generarán $activeCount constancias DC-3 (empleados activos)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text("Firmar y Generar DC-3")
    }
}
