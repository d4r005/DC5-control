package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.model.TrainingAgent
import com.example.dc5control.data.repository.SupabaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentListScreen(onBack: () -> Unit) {
    val agents = remember { mutableStateListOf<TrainingAgent>() }
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("agents", TrainingAgent.serializer()) { fetchedAgents ->
            agents.clear()
            agents.addAll(fetchedAgents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agentes Capacitadores") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<-")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Agente")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(agents) { agent ->
                ListItem(
                    headlineContent = { Text(agent.name) },
                    supportingContent = { Text("Registro: ${agent.stpsRegistry}") }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        AddAgentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, registry ->
                val newAgent = TrainingAgent(name = name, stpsRegistry = registry)
                SupabaseRepository.insertData("agents", newAgent, TrainingAgent.serializer()) { success ->
                    if (success) {
                        agents.add(newAgent)
                        showAddDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddAgentDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var registry by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Agente") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                TextField(value = registry, onValueChange = { registry = it }, label = { Text("Registro STPS") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, registry) }) {
                Text("Agregar")
            }
        }
    )
}
