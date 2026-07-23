package com.example.dc5control.ui

import android.content.Context
import android.graphics.Bitmap
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
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*
import com.example.dc5control.util.CloudflareHelper
import com.example.dc5control.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3GenerationScreen(
    user: User = User("Admin", "admin@example.com", "ADMIN"),
    isExpanded: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
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
    var selectedEmployees by remember { mutableStateOf(setOf<Employee>()) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            courses.clear()
            courses.addAll(filtered)
        }
        SupabaseRepository.fetchData("agents", Agent.serializer()) { fetched ->
            agents.clear()
            agents.addAll(fetched)
        }
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            employees.clear()
            // Active employees only
            employees.addAll(filtered.filter { it.active })
        }
        SupabaseRepository.fetchData("companies", Company.serializer()) { fetched ->
            companies.clear()
            companies.addAll(fetched)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        if (isGenerating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Generando Constancias DC-3...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Por favor espera mientras creamos y subimos los archivos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Violet600,
                        trackColor = Gray200
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray700
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with custom title & subtitle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Gray900
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Generar Constancia DC-3",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Gray900,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "Formato oficial STPS · Art. 153-A LFT",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Gray500,
                                fontSize = 14.sp
                            )
                        )
                    }
                }

                if (isExpanded) {
                    // Two-column layout for Tablet/Landscape
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: worker selection + company
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            WorkerSelectionSection(
                                employees = employees,
                                selectedEmployees = selectedEmployees,
                                onSelectionChanged = { selectedEmployees = it }
                            )

                            CompanySelectionSection(
                                companies = companies,
                                selectedCompany = selectedCompany,
                                onCompanySelected = { selectedCompany = it }
                            )
                        }

                        // Right Column: agent + course + dates + generate button
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AgentSelectionSection(
                                agents = agents,
                                selectedAgent = selectedAgent,
                                onAgentSelected = { selectedAgent = it }
                            )

                            CourseSelectionSection(
                                courses = courses,
                                selectedCourse = selectedCourse,
                                onCourseSelected = { selectedCourse = it }
                            )

                            DatesSelectionSection(
                                startDate = startDate,
                                onStartDateSelected = { startDate = it },
                                endDate = endDate,
                                onEndDateSelected = { endDate = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ActionButtonsSection(
                                onBack = onBack,
                                onGenerate = {
                                    scope.launch {
                                        isGenerating = true
                                        try {
                                            val total = selectedEmployees.size
                                            selectedEmployees.forEachIndexed { index, employee ->
                                                statusText = "Procesando ${index + 1} de $total: ${employee.nombres}..."
                                                
                                                try {
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
                                                        signatureBitmap = null,
                                                        logoBitmap = null
                                                    )
                                                    
                                                    try {
                                                        CloudflareHelper.uploadPdfSuspend(file)
                                                    } catch (cfEx: Exception) {
                                                        android.util.Log.e("DC3", "Error upload: ${cfEx.message}")
                                                    }
                                                } catch (pdfEx: Exception) {
                                                    android.util.Log.e("DC3", "Error PDF: ${pdfEx.message}")
                                                }

                                                val record = DC3Record(
                                                    workerId = employee.curp,
                                                    workerName = "${employee.apellidoPaterno} ${employee.nombres}".trim(),
                                                    workerPos = employee.position,
                                                    courseName = selectedCourse!!.name,
                                                    durationHours = selectedCourse!!.durationHours,
                                                    thematicArea = selectedCourse!!.thematicArea ?: "",
                                                    companyName = selectedCompany!!.name,
                                                    agentName = selectedAgent!!.name,
                                                    agentStps = selectedAgent!!.stps,
                                                    startDate = startDate,
                                                    endDate = endDate,
                                                    creatorEmail = employee.creatorEmail ?: user.email
                                                )
                                                SupabaseRepository.insertDataSuspend("dc3_records", record, DC3Record.serializer())
                                            }
                                            
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Constancias DC-3 generadas con éxito", Toast.LENGTH_LONG).show()
                                                onBack()
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("DC3", "Error general: ${e.message}")
                                        } finally {
                                            isGenerating = false
                                        }
                                    }
                                },
                                enabled = selectedEmployees.isNotEmpty() && selectedCompany != null && selectedAgent != null && selectedCourse != null && startDate.isNotEmpty() && endDate.isNotEmpty()
                            )
                        }
                    }
                } else {
                    // Compact Single Column layout for Portrait/Phone
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WorkerSelectionSection(
                            employees = employees,
                            selectedEmployees = selectedEmployees,
                            onSelectionChanged = { selectedEmployees = it }
                        )

                        CompanySelectionSection(
                            companies = companies,
                            selectedCompany = selectedCompany,
                            onCompanySelected = { selectedCompany = it }
                        )

                        AgentSelectionSection(
                            agents = agents,
                            selectedAgent = selectedAgent,
                            onAgentSelected = { selectedAgent = it }
                        )

                        CourseSelectionSection(
                            courses = courses,
                            selectedCourse = selectedCourse,
                            onCourseSelected = { selectedCourse = it }
                        )

                        DatesSelectionSection(
                            startDate = startDate,
                            onStartDateSelected = { startDate = it },
                            endDate = endDate,
                            onEndDateSelected = { endDate = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ActionButtonsSection(
                            onBack = onBack,
                            onGenerate = {
                                scope.launch {
                                    isGenerating = true
                                    try {
                                        val total = selectedEmployees.size
                                        selectedEmployees.forEachIndexed { index, employee ->
                                            statusText = "Procesando ${index + 1} de $total: ${employee.nombres}..."
                                            
                                            try {
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
                                                    signatureBitmap = null,
                                                    logoBitmap = null
                                                )
                                                
                                                try {
                                                    CloudflareHelper.uploadPdfSuspend(file)
                                                } catch (cfEx: Exception) {
                                                    android.util.Log.e("DC3", "Error upload: ${cfEx.message}")
                                                }
                                            } catch (pdfEx: Exception) {
                                                android.util.Log.e("DC3", "Error PDF: ${pdfEx.message}")
                                            }

                                            val record = DC3Record(
                                                workerId = employee.curp,
                                                workerName = "${employee.apellidoPaterno} ${employee.nombres}".trim(),
                                                workerPos = employee.position,
                                                courseName = selectedCourse!!.name,
                                                durationHours = selectedCourse!!.durationHours,
                                                thematicArea = selectedCourse!!.thematicArea ?: "",
                                                companyName = selectedCompany!!.name,
                                                agentName = selectedAgent!!.name,
                                                agentStps = selectedAgent!!.stps,
                                                startDate = startDate,
                                                endDate = endDate,
                                                creatorEmail = employee.creatorEmail ?: user.email
                                            )
                                            SupabaseRepository.insertDataSuspend("dc3_records", record, DC3Record.serializer())
                                        }
                                        
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Constancias DC-3 generadas con éxito", Toast.LENGTH_LONG).show()
                                            onBack()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("DC3", "Error general: ${e.message}")
                                    } finally {
                                        isGenerating = false
                                    }
                                }
                            },
                            enabled = selectedEmployees.isNotEmpty() && selectedCompany != null && selectedAgent != null && selectedCourse != null && startDate.isNotEmpty() && endDate.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkerSelectionSection(
    employees: List<Employee>,
    selectedEmployees: Set<Employee>,
    onSelectionChanged: (Set<Employee>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredEmployees = employees.filter {
        it.nombres.contains(searchQuery, ignoreCase = true) ||
        it.apellidoPaterno.contains(searchQuery, ignoreCase = true) ||
        it.curp.contains(searchQuery, ignoreCase = true)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Seleccionar Trabajadores",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar trabajador...", color = Gray400) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gray400) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Gray200,
                    focusedBorderColor = NavyPrimary,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = Gray100,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
            ) {
                if (filteredEmployees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron trabajadores activos", color = Gray500, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(4.dp)
                    ) {
                        items(filteredEmployees) { employee ->
                            val isChecked = selectedEmployees.contains(employee)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectionChanged(
                                            if (isChecked) selectedEmployees - employee else selectedEmployees + employee
                                        )
                                    }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        onSelectionChanged(
                                            if (checked == true) selectedEmployees + employee else selectedEmployees - employee
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "${employee.apellidoPaterno} ${employee.apellidoMaterno} ${employee.nombres}".trim(),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Gray900
                                    )
                                    Text(
                                        "CURP: ${employee.curp}",
                                        fontSize = 11.sp,
                                        color = Gray500
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (selectedEmployees.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                if (selectedEmployees.size == 1) {
                    val worker = selectedEmployees.first()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Blue50),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "${worker.apellidoPaterno} ${worker.nombres}".trim(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    "CURP: ${worker.curp}",
                                    fontSize = 11.sp,
                                    color = Gray700
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Blue50),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Group,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Generación Masiva",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    "${selectedEmployees.size} trabajadores seleccionados",
                                    fontSize = 11.sp,
                                    color = Gray700
                                )
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
fun CompanySelectionSection(
    companies: List<Company>,
    selectedCompany: Company?,
    onCompanySelected: (Company) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Empresa",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCompany?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona empresa...", color = Gray400) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = NavyPrimary,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (companies.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay empresas registradas") },
                            onClick = { expanded = false }
                        )
                    } else {
                        companies.forEach { company ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(company.name, fontWeight = FontWeight.Medium)
                                        Text("RFC: ${company.rfc}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                                    }
                                },
                                onClick = {
                                    onCompanySelected(company)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedCompany != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Gray100,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "Patrón / Representante:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Gray700)
                        )
                        Text(
                            selectedCompany.representanteLegal.ifBlank { "No especificado" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray900
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionSection(
    courses: List<Course>,
    selectedCourse: Course?,
    onCourseSelected: (Course) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Curso",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCourse?.let { "${it.name} (${it.durationHours} hrs)" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona curso...", color = Gray400) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = NavyPrimary,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (courses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay cursos registrados") },
                            onClick = { expanded = false }
                        )
                    } else {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(course.name, fontWeight = FontWeight.Medium)
                                        Text("Duración: ${course.durationHours} hrs | Área: ${course.thematicArea ?: "–"}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                                    }
                                },
                                onClick = {
                                    onCourseSelected(course)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentSelectionSection(
    agents: List<Agent>,
    selectedAgent: Agent?,
    onAgentSelected: (Agent) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Agente Capacitador / Instructor",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedAgent?.let { "${it.name} (${it.stps})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona agente...", color = Gray400) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = NavyPrimary,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (agents.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay agentes registrados") },
                            onClick = { expanded = false }
                        )
                    } else {
                        agents.forEach { agent ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(agent.name, fontWeight = FontWeight.Medium)
                                        Text("STPS: ${agent.stps}", style = MaterialTheme.typography.bodySmall, color = Gray500)
                                    }
                                },
                                onClick = {
                                    onAgentSelected(agent)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DatesSelectionSection(
    startDate: String,
    onStartDateSelected: (String) -> Unit,
    endDate: String,
    onEndDateSelected: (String) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    if (showStartDatePicker) {
        MyDatePickerDialog(
            onDateSelected = onStartDateSelected,
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        MyDatePickerDialog(
            onDateSelected = onEndDateSelected,
            onDismiss = { showEndDatePicker = false }
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = BorderStroke(1.dp, Gray200),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Período de Ejecución",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha Inicio", fontSize = 11.sp) },
                    placeholder = { Text("dd/MM/yyyy", fontSize = 12.sp, color = Gray400) },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Fecha Inicio", tint = Gray500)
                        }
                    },
                    modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = NavyPrimary,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = endDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha Fin", fontSize = 11.sp) },
                    placeholder = { Text("dd/MM/yyyy", fontSize = 12.sp, color = Gray400) },
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Fecha Fin", tint = Gray500)
                        }
                    },
                    modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Gray200,
                        focusedBorderColor = NavyPrimary,
                        focusedContainerColor = SurfaceWhite,
                        unfocusedContainerColor = SurfaceWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    onBack: () -> Unit,
    onGenerate: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gray700),
                border = BorderStroke(1.dp, Gray300),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = { /* Previsualizar */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Previsualizar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = NavyPrimary,
                disabledContainerColor = Gray200,
                disabledContentColor = Gray400
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Generar PDF DC-3", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val dateStr = formatter.format(java.util.Date(millis))
                        onDateSelected(dateStr)
                    }
                    onDismiss()
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
