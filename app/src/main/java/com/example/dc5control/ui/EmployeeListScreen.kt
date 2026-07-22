package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.model.Employee
import com.example.dc5control.data.repository.SupabaseRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(onBack: () -> Unit) {
    val employees = remember { mutableStateListOf<Employee>() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("employees", Employee.serializer()) { fetched ->
            employees.clear()
            employees.addAll(fetched)
        }
    }

    val filteredEmployees = employees.filter {
        it.firstName.contains(searchQuery, ignoreCase = true) ||
        it.lastName.contains(searchQuery, ignoreCase = true) ||
        it.curp.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Personal") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<-")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar por nombre o CURP") },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredEmployees) { employee ->
                    ListItem(
                        headlineContent = { Text("${employee.lastName} ${employee.firstName}") },
                        supportingContent = { Text("CURP: ${employee.curp} | Puesto: ${employee.position}") },
                        trailingContent = {
                            Badge(
                                containerColor = if (employee.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            ) {
                                Text(if (employee.active) "Activo" else "Baja")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
