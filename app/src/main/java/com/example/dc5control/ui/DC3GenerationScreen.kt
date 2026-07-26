package com.example.dc5control.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*
import com.example.dc5control.util.CloudflareHelper
import com.example.dc5control.util.CourseDefaults
import com.example.dc5control.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Helper to load bitmap from URL or Base64
suspend fun loadBitmap(context: Context, source: String?): Bitmap? {
    if (source.isNullOrBlank()) return null
    return try {
        if (source.startsWith("data:image")) {
            val base64String = source.substringAfter("base64,")
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } else {
            val loader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(source)
                .allowHardware(false) // Required for PDF drawing
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            (result as? android.graphics.drawable.BitmapDrawable)?.bitmap
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3GenerationScreen(
    user: User = User("Admin", "admin@example.com", "ADMIN"),
    isExpanded: Boolean,
    onBack: () -> Unit,
    preselectedEmployees: List<Employee>? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCourses by remember { mutableStateOf(setOf<Course>()) }
    var selectedAgent by remember { mutableStateOf<Agent?>(null) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var isGenerating by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    val courses = remember { mutableStateListOf<Course>() }
    val agents = remember { mutableStateListOf<Agent>() }
    val employees = remember { mutableStateListOf<Employee>() }
    val companies = remember { mutableStateListOf<Company>() }
    var selectedEmployees by remember { mutableStateOf(preselectedEmployees?.toSet() ?: emptySet()) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { fetched ->
            CourseDefaults.cleanupDatabase(fetched) {
                val merged = CourseDefaults.mergeWithDefaults(fetched, user.email)
                val filtered = if (user.role == "ADMIN") merged else merged.filter { it.creatorEmail == user.email }
                courses.clear()
                courses.addAll(filtered)
            }
        }
        SupabaseRepository.fetchData("agents", Agent.serializer()) { fetched ->
            agents.clear()
            agents.addAll(fetched)
        }
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            employees.clear()
            employees.addAll(filtered.filter { it.active })
        }
        SupabaseRepository.fetchData("companies", Company.serializer()) { fetched ->
            companies.clear()
            companies.addAll(fetched)
        }
    }

    suspend fun generateOrPreview(isPreview: Boolean) {
        val agent = selectedAgent ?: return
        val company = selectedCompany ?: return
        val design = SupabaseRepository.fetchDataFilteredSuspend("agent_designs", "creator_email=eq.${agent.creatorEmail ?: user.email}", AgentDesign.serializer()).firstOrNull()
        
        val signature = loadBitmap(context, design?.firmaBase64)
        val logo = loadBitmap(context, design?.logoBase64)
        val hLogo = loadBitmap(context, design?.headerLogoBase64)

        if (isPreview) {
            val employee = selectedEmployees.firstOrNull() ?: return
            val course = selectedCourses.firstOrNull() ?: return
            val photo = loadBitmap(context, employee.photoUrl)
            val file = PdfGenerator.generateDC3(
                context, employee, course, agent, company.name, company.rfc,
                company.representanteLegal, company.representanteTrabajadores,
                startDate, endDate, signature, logo, photo, hLogo, design?.headerSlogan
            )
            PdfGenerator.openPdf(context, file)
        } else {
            isGenerating = true
            try {
                var currentDoc = 0
                val totalDocs = selectedEmployees.size * selectedCourses.size
                selectedEmployees.forEach { employee ->
                    val photo = loadBitmap(context, employee.photoUrl)
                    selectedCourses.forEach { course ->
                        currentDoc++
                        statusText = "Procesando $currentDoc de $totalDocs: ${employee.nombres}..."
                        val file = PdfGenerator.generateDC3(
                            context, employee, course, agent, company.name, company.rfc,
                            company.representanteLegal, company.representanteTrabajadores,
                            startDate, endDate, signature, logo, photo, hLogo, design?.headerSlogan
                        )
                        PdfGenerator.saveToDownloads(context, file)
                        if (totalDocs == 1) PdfGenerator.openPdf(context, file)

                        val record = DC3Record(
                            workerId = employee.curp,
                            workerName = "${employee.apellidoPaterno} ${employee.nombres}".trim(),
                            workerPos = employee.position,
                            courseName = course.name,
                            durationHours = course.durationHours,
                            thematicArea = course.thematicArea ?: "",
                            companyName = company.name,
                            agentName = agent.name,
                            agentStps = agent.stps,
                            startDate = startDate,
                            endDate = endDate,
                            creatorEmail = employee.creatorEmail ?: user.email
                        )
                        SupabaseRepository.insertDataSuspend("dc3_records", record, DC3Record.serializer())
                        try { CloudflareHelper.uploadPdfSuspend(file) } catch(e: Exception) {}
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Documentos guardados en Descargas", Toast.LENGTH_LONG).show()
                    onBack()
                }
            } catch (e: Exception) {
            } finally { isGenerating = false }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight).padding(16.dp)) {
        if (isGenerating) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Generando Constancias DC-3...", fontWeight = FontWeight.Bold, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(8.dp), color = Violet600)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(statusText, style = MaterialTheme.typography.bodySmall, color = Gray700)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Generar Constancia DC-3", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Formato oficial STPS · Art. 153-A LFT", color = Gray500, fontSize = 14.sp)
                    }
                }
                if (isExpanded) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            WorkerSelectionSection(employees, selectedEmployees) { selectedEmployees = it }
                            CompanySelectionSection(companies, selectedCompany) { selectedCompany = it }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            AgentSelectionSection(agents, selectedAgent) { selectedAgent = it }
                            CourseSelectionSection(courses, selectedCourses) { selectedCourses = it }
                            DatesSelectionSection(startDate, { startDate = it }, endDate, { endDate = it })
                            Spacer(modifier = Modifier.height(8.dp))
                            ActionButtonsSection(onBack, onPreview = { scope.launch { generateOrPreview(true) } }, onGenerate = { scope.launch { generateOrPreview(false) } },
                                enabled = selectedEmployees.isNotEmpty() && selectedCompany != null && selectedAgent != null && selectedCourses.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty())
                        }
                    }
                } else {
                    WorkerSelectionSection(employees, selectedEmployees) { selectedEmployees = it }
                    CompanySelectionSection(companies, selectedCompany) { selectedCompany = it }
                    AgentSelectionSection(agents, selectedAgent) { selectedAgent = it }
                    CourseSelectionSection(courses, selectedCourses) { selectedCourses = it }
                    DatesSelectionSection(startDate, { startDate = it }, endDate, { endDate = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    ActionButtonsSection(onBack, onPreview = { scope.launch { generateOrPreview(true) } }, onGenerate = { scope.launch { generateOrPreview(false) } },
                        enabled = selectedEmployees.isNotEmpty() && selectedCompany != null && selectedAgent != null && selectedCourses.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty())
                }
            }
        }
    }
}

@Composable
fun WorkerSelectionSection(employees: List<Employee>, selectedEmployees: Set<Employee>, onSelectionChanged: (Set<Employee>) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = employees.filter { it.nombres.contains(searchQuery, ignoreCase = true) || it.apellidoPaterno.contains(searchQuery, ignoreCase = true) || it.curp.contains(searchQuery, ignoreCase = true) }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Gray200), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Seleccionar Trabajadores", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Buscar...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Gray100, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp)) {
                LazyColumn(modifier = Modifier.padding(4.dp)) {
                    items(filtered) { emp ->
                        val checked = selectedEmployees.contains(emp)
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSelectionChanged(if (checked) selectedEmployees - emp else selectedEmployees + emp) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = { onSelectionChanged(if (it == true) selectedEmployees + emp else selectedEmployees - emp) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("${emp.apellidoPaterno} ${emp.nombres}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("CURP: ${emp.curp}", fontSize = 11.sp, color = Gray500)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanySelectionSection(companies: List<Company>, selectedCompany: Company?, onCompanySelected: (Company) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Gray200), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Empresa", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(value = selectedCompany?.name ?: "", onValueChange = {}, readOnly = true, placeholder = { Text("Selecciona...") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(8.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    companies.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { onCompanySelected(it); expanded = false }) }
                }
            }
        }
    }
}

@Composable
fun CourseSelectionSection(courses: List<Course>, selectedCourses: Set<Course>, onSelectionChanged: (Set<Course>) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = courses.filter { it.name.contains(searchQuery, ignoreCase = true) }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Gray200), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Seleccionar Cursos", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, placeholder = { Text("Buscar curso...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(color = Gray100, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp)) {
                LazyColumn(modifier = Modifier.padding(4.dp)) {
                    items(filtered) { crs ->
                        val checked = selectedCourses.contains(crs)
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSelectionChanged(if (checked) selectedCourses - crs else selectedCourses + crs) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = { onSelectionChanged(if (it == true) selectedCourses + crs else selectedCourses - crs) })
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(crs.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("${crs.durationHours} | ${crs.thematicArea ?: ""}", fontSize = 11.sp, color = Gray500)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSelectionSection(agents: List<Agent>, selectedAgent: Agent?, onAgentSelected: (Agent) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Gray200), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Agente Capacitador", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(value = selectedAgent?.name ?: "", onValueChange = {}, readOnly = true, placeholder = { Text("Selecciona...") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(8.dp))
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    agents.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { onAgentSelected(it); expanded = false }) }
                }
            }
        }
    }
}

@Composable
fun DatesSelectionSection(startDate: String, onStart: (String) -> Unit, endDate: String, onEnd: (String) -> Unit) {
    var showStart by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }
    if (showStart) MyDatePickerDialog(onStart) { showStart = false }
    if (showEnd) MyDatePickerDialog(onEnd) { showEnd = false }
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceWhite), border = BorderStroke(1.dp, Gray200), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Período de Ejecución", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = startDate, onValueChange = {}, readOnly = true, label = { Text("Inicio") }, modifier = Modifier.weight(1f).clickable { showStart = true }, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = endDate, onValueChange = {}, readOnly = true, label = { Text("Fin") }, modifier = Modifier.weight(1f).clickable { showEnd = true }, shape = RoundedCornerShape(8.dp))
            }
        }
    }
}

@Composable
fun ActionButtonsSection(onBack: () -> Unit, onPreview: () -> Unit, onGenerate: () -> Unit, enabled: Boolean) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Cancelar") }
            Button(onClick = onPreview, modifier = Modifier.weight(1f), enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary), shape = RoundedCornerShape(8.dp)) { Text("Previsualizar") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = enabled, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)) { Text("Generar PDF DC-3", fontWeight = FontWeight.Bold) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(onDateSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { val f = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()); f.timeZone = java.util.TimeZone.getTimeZone("UTC"); onDateSelected(f.format(java.util.Date(it))) }; onDismiss() }) { Text("OK") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }) { DatePicker(state) }
}
