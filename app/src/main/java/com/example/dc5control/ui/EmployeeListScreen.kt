package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.Employee
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(user: User, onBack: () -> Unit) {
    val employees = remember { mutableStateListOf<Employee>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            employees.clear()
            employees.addAll(filtered)
            isLoading = false
        }
    }

    val filteredEmployees = employees.filter {
        it.nombres.contains(searchQuery, ignoreCase = true) ||
        it.apellidoPaterno.contains(searchQuery, ignoreCase = true) ||
        it.curp.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Personal", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Gray900))
                        Text("Gestión de trabajadores", style = MaterialTheme.typography.bodySmall.copy(color = Gray400))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Gray900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add logic */ },
                containerColor = NavyPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Personal")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            
            if (selectedIds.isNotEmpty()) {
                Surface(
                    color = Color(0xFFE8EEF8),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${selectedIds.size} seleccionados",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { /* Bulk DC3 */ },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Generar PDF Masivo", fontSize = 12.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o CURP", color = Gray400) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gray400) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Gray100,
                    focusedBorderColor = NavyPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gray100),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                } else if (filteredEmployees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron resultados", color = Gray400)
                    }
                } else {
                    LazyColumn {
                        items(filteredEmployees) { employee ->
                            EmployeeListItem(
                                employee = employee,
                                isSelected = selectedIds.contains(employee.id.toString()),
                                onSelectToggle = {
                                    val id = employee.id.toString()
                                    selectedIds = if (selectedIds.contains(id)) {
                                        selectedIds - id
                                    } else {
                                        selectedIds + id
                                    }
                                }
                            )
                            HorizontalDivider(color = Gray100, modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmployeeListItem(employee: Employee, isSelected: Boolean, onSelectToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelectToggle() },
            colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Surface(
            color = Gray100,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Gray400, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${employee.nombres} ${employee.apellidoPaterno}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Gray900)
            )
            Text(
                "CURP: ${employee.curp}",
                style = MaterialTheme.typography.bodySmall.copy(color = Gray400)
            )
        }
        
        Surface(
            color = if (employee.active) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (employee.active) "Activo" else "Baja",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (employee.active) SuccessGreen else ErrorRed
                )
            )
        }
    }
}
