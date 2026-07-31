package com.example.dc5control.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*
import com.example.dc5control.util.PdfGenerator
import com.example.dc5control.util.DiplomaGenerator
import kotlinx.coroutines.launch

@Composable
fun DC3HistoryScreen(user: User, isExpanded: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val records = remember { mutableStateListOf<DC3Record>() }
    val agents = remember { mutableStateListOf<Agent>() }
    var selectedAgentFilter by remember { mutableStateOf<Agent?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("DC-3", "Diplomas")
    var isLoading by remember { mutableStateOf(true) }
    var isAgentsExpanded by remember { mutableStateOf(false) }

    fun refresh() {
        isLoading = true
        SupabaseRepository.fetchData("dc3_records", DC3Record.serializer()) { fetched ->
            records.clear()
            records.addAll(fetched)
            isLoading = false
        }
        SupabaseRepository.fetchData("agents", Agent.serializer()) { fetchedAgents ->
            // Normalización para evitar duplicados sin alterar el orden del nombre
            fun normalize(s: String): String = s.uppercase()
                .replace("ING.", "")
                .replace(Regex("\\s+"), " ")
                .trim()

            val uniqueAgents = mutableListOf<Agent>()
            val seenNorms = mutableSetOf<String>()

            fetchedAgents.forEach { agent ->
                val norm = normalize(agent.name)
                // Preferimos nombres que ya tienen "Jesus Dario" si hay conflicto
                if (norm !in seenNorms) {
                    uniqueAgents.add(agent)
                    seenNorms.add(norm)
                } else if (agent.name.contains("Jesus", ignoreCase = true)) {
                    // Si ya vimos el nombre pero esta versión tiene "Jesus", reemplazamos la anterior
                    uniqueAgents.removeAll { normalize(it.name) == norm }
                    uniqueAgents.add(agent)
                }
            }

            agents.clear()
            agents.addAll(uniqueAgents)
        }
    }

    LaunchedEffect(Unit) {
        refresh()
    }

    fun handleView(record: DC3Record) {
        scope.launch {
            try {
                // We need to fetch details to reconstruct PDF or use a URL if available
                // For now, let's assume we can generate it again from record data
                // This is a bit simplified, ideally we fetch the Employee/Agent object
                // but let's use a simplified generator if needed or fetch them.
                
                // For a quick fix, we'll try to find the data
                SupabaseRepository.fetchData("workers", Employee.serializer()) { employees ->
                    val employee = employees.find { it.curp == record.workerId }
                    SupabaseRepository.fetchData("agents", Agent.serializer()) { agents ->
                        val agent = agents.find { it.name == record.agentName }
                        
                        if (employee != null && agent != null) {
                            val file = if (selectedTab == 1) {
                                DiplomaGenerator.generateDiploma(
                                    context = context,
                                    employee = employee,
                                    course = Course(name = record.courseName, durationHours = record.durationHours, thematicArea = record.thematicArea),
                                    agent = agent,
                                    startDate = record.startDate,
                                    endDate = record.endDate,
                                    folio = record.folio,
                                    qrUrl = if (record.id != null) "https://ace-control.pages.dev/?v=${record.id}" else null
                                )
                            } else {
                                PdfGenerator.generateDC3(
                                    context = context,
                                    employee = employee,
                                    course = Course(name = record.courseName, durationHours = record.durationHours, thematicArea = record.thematicArea),
                                    agent = agent,
                                    companyName = record.companyName,
                                    companyRfc = "", // Need RFC in record really
                                    companyPatron = "",
                                    companyRepresentante = null,
                                    startDate = record.startDate,
                                    endDate = record.endDate,
                                    signatureBitmap = null,
                                    logoBitmap = null,
                                    qrUrl = if (record.id != null) "https://ace-control.pages.dev/?v=${record.id}" else null,
                                    folio = record.folioDc3 ?: record.folio
                                )
                            }
                            PdfGenerator.openPdf(context, file)
                        }
                    }
                }
            } catch (e: Exception) { }
        }
    }

    fun handleDownload(record: DC3Record) {
        scope.launch {
            SupabaseRepository.fetchData("workers", Employee.serializer()) { employees ->
                val employee = employees.find { it.curp == record.workerId }
                SupabaseRepository.fetchData("agents", Agent.serializer()) { agents ->
                    val agent = agents.find { it.name == record.agentName }
                    if (employee != null && agent != null) {
                        val file = if (selectedTab == 1) {
                            DiplomaGenerator.generateDiploma(
                                context = context,
                                employee = employee,
                                course = Course(name = record.courseName, durationHours = record.durationHours, thematicArea = record.thematicArea),
                                agent = agent,
                                startDate = record.startDate,
                                endDate = record.endDate,
                                folio = record.folio,
                                qrUrl = if (record.id != null) "https://ace-control.pages.dev/?v=${record.id}" else null
                            )
                        } else {
                            PdfGenerator.generateDC3(
                                context = context,
                                employee = employee,
                                course = Course(name = record.courseName, durationHours = record.durationHours, thematicArea = record.thematicArea),
                                agent = agent,
                                companyName = record.companyName,
                                companyRfc = "",
                                companyPatron = "",
                                companyRepresentante = null,
                                startDate = record.startDate,
                                endDate = record.endDate,
                                signatureBitmap = null,
                                logoBitmap = null,
                                qrUrl = if (record.id != null) "https://ace-control.pages.dev/?v=${record.id}" else null,
                                folio = record.folioDc3 ?: record.folio
                            )
                        }
                        PdfGenerator.saveToDownloads(context, file)
                        android.widget.Toast.makeText(context, "Archivo guardado en Descargas", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        // Header
        Surface(
            color = SurfaceWhite,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isExpanded) 32.dp else 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Gray900)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Constancias", fontSize = if (isExpanded) 20.sp else 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                        Text("Historial de documentos generados", fontSize = 14.sp, color = Gray400)
                    }
                    
                    if (agents.isNotEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.width(250.dp)) {
                            @OptIn(ExperimentalMaterial3Api::class)
                            ExposedDropdownMenuBox(
                                expanded = isAgentsExpanded,
                                onExpandedChange = { isAgentsExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedAgentFilter?.name ?: "Todos los Agentes",
                                    onValueChange = {},
                                    readOnly = true,
                                    placeholder = { Text("Filtrar por agente") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isAgentsExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NavyPrimary,
                                        unfocusedBorderColor = Gray200
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = isAgentsExpanded,
                                    onDismissRequest = { isAgentsExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Todos los Agentes") },
                                        onClick = { selectedAgentFilter = null; isAgentsExpanded = false }
                                    )
                                    agents.forEach { agent ->
                                        DropdownMenuItem(
                                            text = { Text(agent.name) },
                                            onClick = { selectedAgentFilter = agent; isAgentsExpanded = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = Gray200)

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceWhite,
                    contentColor = NavyPrimary,
                    divider = { HorizontalDivider(color = Gray200) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            return@Column
        }

        val filteredRecords = remember(records, selectedAgentFilter, selectedTab) {
            val byType = if (selectedTab == 0) {
                records.filter { it.documentType == "DC3" || it.documentType == "BOTH" }
            } else {
                records.filter { it.documentType == "DIPLOMA" || it.documentType == "BOTH" }
            }
            
            if (selectedAgentFilter == null) byType
            else byType.filter { it.agentName == selectedAgentFilter?.name }
        }

        if (filteredRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin historial de ${tabs[selectedTab]}", color = Gray300, fontSize = 14.sp)
            }
            return@Column
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isExpanded) 24.dp else 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, Gray200)
        ) {
            if (isExpanded) {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TRABAJADOR", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("CURSO", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("EMPRESA", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("FECHAS", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    if (selectedTab == 1) {
                        Text("FOLIO", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    }
                    Text("ACCIONES", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                }
                HorizontalDivider(color = Gray100)
            }

            LazyColumn {
                items(filteredRecords) { record ->
                    val contextualFolio = if (selectedTab == 0) record.folioDc3 ?: record.folio else record.folio
                    if (isExpanded) {
                        DC3RecordRow(record, showFolio = true, contextualFolio = contextualFolio, onView = { handleView(it) }, onDownload = { handleDownload(it) }, onDelete = { refresh() })
                    } else {
                        DC3RecordCard(record, contextualFolio = contextualFolio, onView = { handleView(it) }, onDownload = { handleDownload(it) }, onDelete = { refresh() })
                    }
                    HorizontalDivider(color = Gray50, modifier = Modifier.padding(horizontal = if (isExpanded) 0.dp else 16.dp))
                }
            }
        }
    }
}

@Composable
fun DC3RecordRow(record: DC3Record, showFolio: Boolean = false, contextualFolio: String? = null, onView: (DC3Record) -> Unit, onDownload: (DC3Record) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(record.workerName.ifBlank { "–" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
            Text(record.workerId, fontSize = 12.sp, color = Gray400, fontFamily = FontFamily.Monospace)
        }
        Text(record.courseName.ifBlank { "–" }, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Gray700)
        Text(record.companyName.ifBlank { "–" }, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Gray500)
        Text("${record.startDate} – ${record.endDate}", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Gray400)
        if (showFolio) {
            Text(contextualFolio ?: "—", modifier = Modifier.weight(0.8f), fontSize = 12.sp, color = NavyPrimary, fontFamily = FontFamily.Monospace)
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { onView(record) },
                label = { Text("Ver", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = NavySurface,
                    labelColor = NavyPrimary
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
            AssistChip(
                onClick = { onDownload(record) },
                label = { Text("PDF", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = NavyPrimary,
                    labelColor = Color.White
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray500, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = {
                record.id?.let { id ->
                    SupabaseRepository.deleteData("dc3_records", id.toString()) { onDelete() }
                }
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DC3RecordCard(record: DC3Record, contextualFolio: String? = null, onView: (DC3Record) -> Unit, onDownload: (DC3Record) -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.workerName.ifBlank { "–" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(record.workerId, fontSize = 12.sp, color = Gray400, fontFamily = FontFamily.Monospace)
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SuccessSurface
            ) {
                Text(
                    record.resultText.ifBlank { "Acreditado" },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Book, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(record.courseName, fontSize = 13.sp, color = Gray700)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(record.companyName, fontSize = 13.sp, color = Gray500)
            Spacer(modifier = Modifier.weight(1f))
            Text("${record.startDate} – ${record.endDate}", fontSize = 12.sp, color = Gray400)
        }
        if (!contextualFolio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tag, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(contextualFolio, fontSize = 12.sp, color = NavyPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { onView(record) }) {
                Text("Ver", color = NavyPrimary, fontSize = 13.sp)
            }
            TextButton(onClick = { onDownload(record) }) {
                Text("PDF", color = NavyPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            record.id?.let { id ->
                IconButton(onClick = { SupabaseRepository.deleteData("dc3_records", id.toString()) { onDelete() } }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
