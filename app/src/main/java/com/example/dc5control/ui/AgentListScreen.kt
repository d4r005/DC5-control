package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.model.Agent
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentListScreen(user: User, isExpanded: Boolean, onBack: () -> Unit) {
    val agents = remember { mutableStateListOf<Agent>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAgent by remember { mutableStateOf<Agent?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshAgents() {
        isLoading = true
        SupabaseRepository.fetchData("agents", Agent.serializer()) { fetchedAgents ->
            agents.clear()
            agents.addAll(fetchedAgents)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshAgents()
    }

    // Filter agents: ADMIN sees all, USER sees only their creatorEmail agents
    val filteredAgents = remember(agents, user) {
        if (user.role == "ADMIN") {
            agents
        } else {
            agents.filter { it.creatorEmail == user.email }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = NavyPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Agentes Cap.",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Text(
                            text = "Agentes capacitadores",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray500
                        )
                    }
                }

                // Header add button on expanded/desktop layout
                if (isExpanded) {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Agregar", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Main Content Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                } else if (filteredAgents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay agentes registrados.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray500
                        )
                    }
                } else {
                    if (isExpanded) {
                        // Expanded Table Layout
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Gray50)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Nombre",
                                    modifier = Modifier.weight(0.5f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Registro STPS",
                                    modifier = Modifier.weight(0.3f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Acciones",
                                    modifier = Modifier.weight(0.2f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                            HorizontalDivider(color = Gray200)

                            // Table Rows
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredAgents) { agent ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = agent.name,
                                            modifier = Modifier.weight(0.5f),
                                            fontWeight = FontWeight.Bold,
                                            color = Gray900,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = agent.stps,
                                            modifier = Modifier.weight(0.3f),
                                            color = NavyPrimary,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            modifier = Modifier.weight(0.2f),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { editingAgent = agent }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = NavyPrimary
                                                )
                                            }
                                            IconButton(onClick = {
                                                agent.id?.let { id ->
                                                    SupabaseRepository.deleteData("agents", id.toString()) { success ->
                                                        if (success) {
                                                            refreshAgents()
                                                        }
                                                    }
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = ErrorRed
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Gray100)
                                }
                            }
                        }
                    } else {
                        // Compact List Card Layout
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(filteredAgents) { agent ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = agent.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Gray900
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "STPS: ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray500
                                        )
                                        Text(
                                            text = agent.stps,
                                            color = NavyPrimary,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = { editingAgent = agent }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = NavyPrimary
                                            )
                                        }
                                        IconButton(onClick = {
                                            agent.id?.let { id ->
                                                SupabaseRepository.deleteData("agents", id.toString()) { success ->
                                                    if (success) {
                                                        refreshAgents()
                                                    }
                                                }
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = ErrorRed
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = Gray200)
                            }
                        }
                    }
                }
            }
        }

        // FAB for adding agents on compact layout
        if (!isExpanded) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = NavyPrimary,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Agente")
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AgentAddEditDialog(
            agent = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, stps ->
                val newAgent = Agent(
                    name = name,
                    stps = stps,
                    creatorEmail = user.email
                )
                SupabaseRepository.insertData("agents", newAgent, Agent.serializer()) { success ->
                    if (success) {
                        refreshAgents()
                        showAddDialog = false
                    }
                }
            }
        )
    }

    // Edit Dialog
    editingAgent?.let { agent ->
        AgentAddEditDialog(
            agent = agent,
            onDismiss = { editingAgent = null },
            onSave = { name, stps ->
                val updated = agent.copy(
                    name = name,
                    stps = stps
                )
                agent.id?.let { id ->
                    SupabaseRepository.updateData("agents", id.toString(), updated, Agent.serializer()) { success ->
                        if (success) {
                            refreshAgents()
                            editingAgent = null
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun AgentAddEditDialog(
    agent: Agent?,
    onDismiss: () -> Unit,
    onSave: (name: String, stps: String) -> Unit
) {
    var name by remember { mutableStateOf(agent?.name ?: "") }
    var stps by remember { mutableStateOf(agent?.stps ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (agent == null) "Agregar Agente" else "Editar Agente",
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Agente") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        focusedLabelColor = NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = stps,
                    onValueChange = { stps = it },
                    label = { Text("Registro STPS") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        focusedLabelColor = NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && stps.isNotBlank()) {
                        onSave(name, stps.toUpperCase())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Gray500)
            }
        },
        shape = RoundedCornerShape(12.dp),
        containerColor = SurfaceWhite
    )
}
