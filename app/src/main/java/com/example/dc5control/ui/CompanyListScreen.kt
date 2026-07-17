package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.model.Company
import com.example.dc5control.data.repository.SupabaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyListScreen(onBack: () -> Unit) {
    val companies = remember { mutableStateListOf<Company>() }
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("companies", Company.serializer()) { fetched ->
            companies.clear()
            companies.addAll(fetched)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Empresas") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("←") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Empresa")
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (companies.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay empresas registradas.\nToca + para agregar una.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(companies) { company ->
                    ListItem(
                        headlineContent = { Text(company.name) },
                        supportingContent = {
                            Column {
                                Text("RFC: ${company.rfc}", style = MaterialTheme.typography.bodySmall)
                                if (company.patron.isNotBlank()) {
                                    Text("Patrón: ${company.patron}", style = MaterialTheme.typography.bodySmall)
                                }
                                company.representante?.let {
                                    if (it.isNotBlank()) {
                                        Text("Rep. Trabajadores: $it", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        },
                        leadingContent = { Icon(Icons.Default.Business, contentDescription = null) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddCompanyDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, rfc, patron, representante ->
                val newCompany = Company(
                    name = name,
                    rfc = rfc,
                    patron = patron,
                    representante = representante.ifBlank { null }
                )
                SupabaseRepository.insertData("companies", newCompany, Company.serializer()) { success ->
                    if (success) {
                        companies.add(newCompany)
                        showAddDialog = false
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompanyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, rfc: String, patron: String, representante: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var patron by remember { mutableStateOf("") }
    var representante by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Empresa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre o Razón Social") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rfc,
                    onValueChange = { rfc = it.uppercase() },
                    label = { Text("RFC con homoclave") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Datos para firma DC-3", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = patron,
                    onValueChange = { patron = it },
                    label = { Text("Patrón o Representante Legal") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = representante,
                    onValueChange = { representante = it },
                    label = { Text("Representante de los Trabajadores") },
                    supportingText = { Text("Obligatorio para empresas con >50 trabajadores", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, rfc, patron, representante) },
                enabled = name.isNotBlank() && rfc.isNotBlank() && patron.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
