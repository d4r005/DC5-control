package com.example.dc5control.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.Screen
import com.example.dc5control.data.model.Employee
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*
import com.example.dc5control.util.ExcelHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(user: User, isExpanded: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val employees = remember { mutableStateListOf<Employee>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    
    // Dialog states
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    fun loadData() {
        isLoading = true
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            employees.clear()
            employees.addAll(filtered)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val filteredEmployees = employees.filter {
        it.nombres.contains(searchQuery, ignoreCase = true) ||
        it.apellidoPaterno.contains(searchQuery, ignoreCase = true) ||
        it.apellidoMaterno.contains(searchQuery, ignoreCase = true) ||
        it.curp.contains(searchQuery, ignoreCase = true) ||
        it.occupation.contains(searchQuery, ignoreCase = true) ||
        it.position.contains(searchQuery, ignoreCase = true)
    }

    val excelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                isLoading = true
                try {
                    val workers = ExcelHelper.readWorkersFromExcel(context, it)
                    val workersWithEmail = workers.map { w ->
                        val nameParts = w.name.trim().split("\\s+".toRegex())
                        val nombres = if (nameParts.isNotEmpty()) nameParts.last() else ""
                        val apellidoPaterno = if (nameParts.size > 1) nameParts[0] else ""
                        val apellidoMaterno = if (nameParts.size > 2) nameParts[1] else ""
                        w.copy(
                            creatorEmail = user.email,
                            nombres = w.nombres.ifBlank { nombres },
                            apellidoPaterno = w.apellidoPaterno.ifBlank { apellidoPaterno },
                            apellidoMaterno = w.apellidoMaterno.ifBlank { apellidoMaterno }
                        )
                    }
                    SupabaseRepository.insertWorkers(workersWithEmail) { success ->
                        if (success) {
                            Toast.makeText(context, "Importación exitosa", Toast.LENGTH_SHORT).show()
                            loadData()
                        } else {
                            Toast.makeText(context, "Error al guardar registros en Supabase", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al leer Excel: ${e.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Gray900)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Personal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
                    )
                    Text(
                        text = "Gestión de trabajadores",
                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                    )
                }
                
                // Topbar Buttons matching web
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { excelLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") },
                        colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.FilePresent, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar Excel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = { 
                            employeeToEdit = null
                            showAddEditDialog = true 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Selection Bar shown when items are selected
            if (selectedIds.isNotEmpty()) {
                Surface(
                    color = NavySurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${selectedIds.size} seleccionados",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { 
                                Toast.makeText(context, "Generando PDF Masivo para ${selectedIds.size} trabajadores...", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Generar PDF Masivo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre o CURP...", color = Gray400) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gray400) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Gray200,
                    focusedBorderColor = NavyPrimary,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Main List/Table container Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Gray200),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                } else if (filteredEmployees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Gray400)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No se encontraron resultados", color = Gray500)
                        }
                    }
                } else {
                    if (isExpanded) {
                        // Table View for tablets / landscape
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Gray50)
                                    .border(BorderStroke(0.5.dp, Gray200))
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val allSelected = filteredEmployees.isNotEmpty() && filteredEmployees.all { it.id != null && selectedIds.contains(it.id) }
                                Checkbox(
                                    checked = allSelected,
                                    onCheckedChange = { checked ->
                                        selectedIds = if (checked) {
                                            selectedIds + filteredEmployees.mapNotNull { it.id }.toSet()
                                        } else {
                                            selectedIds - filteredEmployees.mapNotNull { it.id }.toSet()
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Trabajador", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("CURP", modifier = Modifier.weight(1.8f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("Ocupación", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("Puesto", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("Acciones", modifier = Modifier.weight(2.2f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                            }
                            
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredEmployees) { employee ->
                                    val isSelected = employee.id != null && selectedIds.contains(employee.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (isSelected) NavySurface.copy(alpha = 0.3f) else Color.White)
                                            .padding(vertical = 8.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                val idStr = employee.id ?: ""
                                                selectedIds = if (isSelected) selectedIds - idStr else selectedIds + idStr
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        // Photo circle + Name
                                        Row(
                                            modifier = Modifier.weight(2.5f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Gray100)
                                                    .border(BorderStroke(1.dp, Gray200), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Gray500, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                val fullName = "${employee.apellidoPaterno} ${employee.apellidoMaterno} ${employee.nombres}".trim()
                                                Text(
                                                    text = if (fullName.isEmpty()) employee.name else fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Gray900,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                // Status Indicator
                                                Surface(
                                                    color = if (employee.active) SuccessSurface else ErrorSurface,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (employee.active) "Activo" else "Baja",
                                                        color = if (employee.active) SuccessGreen else ErrorRed,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // CURP (Mono)
                                        Text(
                                            text = employee.curp,
                                            modifier = Modifier.weight(1.8f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            color = Gray700
                                        )
                                        
                                        // Ocupación
                                        Text(
                                            text = employee.occupation,
                                            modifier = Modifier.weight(1.5f),
                                            fontSize = 13.sp,
                                            color = Gray600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // Puesto
                                        Text(
                                            text = employee.position,
                                            modifier = Modifier.weight(1.5f),
                                            fontSize = 13.sp,
                                            color = Gray600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // Acciones: DC-3 in Navy, +Cursos, Edit, Delete
                                        Row(
                                            modifier = Modifier.weight(2.2f),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // DC-3 Button
                                            Button(
                                                onClick = { 
                                                    Toast.makeText(context, "Generar DC-3 para ${employee.nombres}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("DC-3", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            
                                            // +Cursos Button
                                            Button(
                                                onClick = { 
                                                    Toast.makeText(context, "Ver cursos de ${employee.nombres}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Gray100, contentColor = Gray700),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+Cursos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            
                                            // Edit Button
                                            IconButton(
                                                onClick = { 
                                                    employeeToEdit = employee
                                                    showAddEditDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray500, modifier = Modifier.size(16.dp))
                                            }
                                            
                                            // Delete Button
                                            IconButton(
                                                onClick = { 
                                                    employeeToDelete = employee
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = Gray100)
                                }
                            }
                        }
                    } else {
                        // Compact Card List for phones
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredEmployees) { employee ->
                                val isSelected = employee.id != null && selectedIds.contains(employee.id)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) NavySurface.copy(alpha = 0.2f) else Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) NavyPrimary.copy(alpha = 0.5f) else Gray100)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    val idStr = employee.id ?: ""
                                                    selectedIds = if (isSelected) selectedIds - idStr else selectedIds + idStr
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Gray100),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Gray500, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                val fullName = "${employee.apellidoPaterno} ${employee.apellidoMaterno} ${employee.nombres}".trim()
                                                Text(
                                                    text = if (fullName.isEmpty()) employee.name else fullName,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Gray900,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "CURP: ${employee.curp}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp,
                                                    color = Gray500
                                                )
                                            }
                                            
                                            Surface(
                                                color = if (employee.active) SuccessSurface else ErrorSurface,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = if (employee.active) "Activo" else "Baja",
                                                    color = if (employee.active) SuccessGreen else ErrorRed,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.padding(start = 40.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column {
                                                Text("Ocupación", fontSize = 11.sp, color = Gray400)
                                                Text(employee.occupation.ifBlank { "—" }, fontSize = 12.sp, color = Gray700)
                                            }
                                            Column {
                                                Text("Puesto", fontSize = 11.sp, color = Gray400)
                                                Text(employee.position.ifBlank { "—" }, fontSize = 12.sp, color = Gray700)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 8.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { 
                                                    Toast.makeText(context, "Generar DC-3 para ${employee.nombres}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary, contentColor = Color.White),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("DC-3", fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Button(
                                                onClick = { 
                                                    Toast.makeText(context, "Ver cursos de ${employee.nombres}", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Gray100, contentColor = Gray700),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+Cursos", fontSize = 11.sp)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { 
                                                    employeeToEdit = employee
                                                    showAddEditDialog = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray500, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { 
                                                    employeeToDelete = employee
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB for adding employee on mobile (aligned at bottom right)
        if (!isExpanded) {
            FloatingActionButton(
                onClick = { 
                    employeeToEdit = null
                    showAddEditDialog = true 
                },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Personal")
            }
        }
    }

    // Dialog for Add/Edit
    if (showAddEditDialog) {
        AddEditEmployeeDialog(
            employee = employeeToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { updated ->
                isLoading = true
                if (employeeToEdit == null) {
                    // Create new employee
                    val newEmp = updated.copy(creatorEmail = user.email)
                    SupabaseRepository.insertData("workers", newEmp, Employee.serializer()) { success ->
                        if (success) {
                            Toast.makeText(context, "Empleado guardado exitosamente", Toast.LENGTH_SHORT).show()
                            loadData()
                        } else {
                            Toast.makeText(context, "Error al guardar el empleado", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                        showAddEditDialog = false
                    }
                } else {
                    // Update existing employee
                    val editId = employeeToEdit!!.id ?: ""
                    if (editId.isBlank()) { showAddEditDialog = false; return@onConfirm }
                    SupabaseRepository.updateData("workers", editId, updated, Employee.serializer()) { success ->
                        if (success) {
                            Toast.makeText(context, "Empleado actualizado exitosamente", Toast.LENGTH_SHORT).show()
                            loadData()
                        } else {
                            Toast.makeText(context, "Error al actualizar el empleado", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                        showAddEditDialog = false
                    }
                }
            }
        )
    }

    // Delete confirmation dialog
    if (employeeToDelete != null) {
        DeleteConfirmationDialog(
            itemName = "${employeeToDelete!!.nombres} ${employeeToDelete!!.apellidoPaterno}",
            onDismiss = { employeeToDelete = null },
            onConfirm = {
                val emp = employeeToDelete!!
                employeeToDelete = null
                isLoading = true
                val delId = emp.id ?: ""
                if (delId.isBlank()) { isLoading = false; return@onConfirm }
                SupabaseRepository.deleteData("workers", delId) { success ->
                    if (success) {
                        Toast.makeText(context, "Empleado eliminado", Toast.LENGTH_SHORT).show()
                        loadData()
                    } else {
                        Toast.makeText(context, "Error al eliminar empleado", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddEditEmployeeDialog(
    employee: Employee? = null,
    onDismiss: () -> Unit,
    onConfirm: (Employee) -> Unit
) {
    var nombres by remember { mutableStateOf(employee?.nombres ?: "") }
    var apellidoPaterno by remember { mutableStateOf(employee?.apellidoPaterno ?: "") }
    var apellidoMaterno by remember { mutableStateOf(employee?.apellidoMaterno ?: "") }
    var curp by remember { mutableStateOf(employee?.curp ?: "") }
    var occupation by remember { mutableStateOf(employee?.occupation ?: "") }
    var position by remember { mutableStateOf(employee?.position ?: "") }
    var photoUrl by remember { mutableStateOf(employee?.photoUrl ?: "") }
    var active by remember { mutableStateOf(employee?.active ?: true) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUrl = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (employee == null) "Agregar Trabajador" else "Editar Trabajador", 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Gray900)
            ) 
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = { Text("Nombre(s)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = apellidoPaterno,
                    onValueChange = { apellidoPaterno = it },
                    label = { Text("Apellido Paterno") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = apellidoMaterno,
                    onValueChange = { apellidoMaterno = it },
                    label = { Text("Apellido Materno") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = curp,
                    onValueChange = { if (it.length <= 18) curp = it.uppercase() },
                    label = { Text("CURP") },
                    placeholder = { Text("18 caracteres") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = occupation,
                    onValueChange = { occupation = it },
                    label = { Text("Ocupación") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Puesto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                
                // Photo upload row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { photoLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Subir Foto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    if (photoUrl.isNotEmpty()) {
                        Text(
                            text = "Seleccionada",
                            color = SuccessGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Sin foto",
                            color = Gray400,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estado Activo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium.copy(color = Gray700))
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = SuccessGreen, checkedTrackColor = SuccessSurface)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fullName = "$apellidoPaterno $apellidoMaterno $nombres".trim()
                    val emp = Employee(
                        id = employee?.id,
                        nombres = nombres,
                        apellidoPaterno = apellidoPaterno,
                        apellidoMaterno = apellidoMaterno,
                        name = fullName,
                        curp = curp,
                        occupation = occupation,
                        position = position,
                        photoUrl = photoUrl.ifBlank { null },
                        active = active,
                        creatorEmail = employee?.creatorEmail ?: ""
                    )
                    onConfirm(emp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                enabled = nombres.isNotBlank() && apellidoPaterno.isNotBlank() && curp.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Gray500)
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Registro", fontWeight = FontWeight.Bold, color = ErrorRed) },
        text = { Text("¿Estás seguro de que deseas eliminar permanentemente a $itemName? Esta acción no se puede deshacer.", color = Gray700) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Eliminar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Gray500)
            }
        }
    )
}
