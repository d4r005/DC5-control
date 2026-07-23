package com.example.dc5control.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.Company
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyListScreen(user: User, isExpanded: Boolean, onBack: () -> Unit) {
    val context = LocalContext.current
    
    val companies = remember { mutableStateListOf<Company>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    // Dialog states
    var companyToEdit by remember { mutableStateOf<Company?>(null) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var companyToDelete by remember { mutableStateOf<Company?>(null) }

    fun loadData() {
        isLoading = true
        SupabaseRepository.fetchData("companies", Company.serializer()) { fetched ->
            val filtered = if (user.role == "ADMIN") fetched else fetched.filter { it.creatorEmail == user.email }
            companies.clear()
            companies.addAll(filtered)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val filteredCompanies = companies.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.rfc.contains(searchQuery, ignoreCase = true) ||
        it.representanteLegal.contains(searchQuery, ignoreCase = true) ||
        (it.representanteTrabajadores?.contains(searchQuery, ignoreCase = true) == true)
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
                        text = "Empresas",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Gray900)
                    )
                    Text(
                        text = "Catálogo de empresas",
                        style = MaterialTheme.typography.bodySmall.copy(color = Gray500)
                    )
                }
                
                // Add Button in topbar matching web
                Button(
                    onClick = { 
                        companyToEdit = null
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

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por razón social o RFC...", color = Gray400) },
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
                } else if (filteredCompanies.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(48.dp), tint = Gray400)
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
                                Text("Razón Social", modifier = Modifier.weight(2.5f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("RFC", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("Rep. Legal", modifier = Modifier.weight(2.0f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("Rep. Trabajadores", modifier = Modifier.weight(2.0f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                                Text("Acciones", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, color = Gray700, fontSize = 14.sp)
                            }
                            
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredCompanies) { company ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Name (bold)
                                        Text(
                                            text = company.name,
                                            modifier = Modifier.weight(2.5f),
                                            fontWeight = FontWeight.Bold,
                                            color = Gray900,
                                            fontSize = 14.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // RFC (mono)
                                        Text(
                                            text = company.rfc,
                                            modifier = Modifier.weight(1.5f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 13.sp,
                                            color = Gray700
                                        )
                                        
                                        // Rep Legal
                                        Text(
                                            text = company.representanteLegal,
                                            modifier = Modifier.weight(2.0f),
                                            fontSize = 13.sp,
                                            color = Gray600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // Rep Trabajadores
                                        Text(
                                            text = company.representanteTrabajadores?.ifBlank { "—" } ?: "—",
                                            modifier = Modifier.weight(2.0f),
                                            fontSize = 13.sp,
                                            color = Gray600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        // Acciones: Edit, Delete
                                        Row(
                                            modifier = Modifier.weight(1.2f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Edit Button
                                            IconButton(
                                                onClick = { 
                                                    companyToEdit = company
                                                    showAddEditDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray500, modifier = Modifier.size(18.dp))
                                            }
                                            
                                            // Delete Button
                                            IconButton(
                                                onClick = { 
                                                    companyToDelete = company
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(18.dp))
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
                            items(filteredCompanies) { company ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Gray100)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Business, 
                                                contentDescription = null, 
                                                tint = NavyPrimary, 
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = company.name,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Gray900,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "RFC: ${company.rfc}",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 12.sp,
                                                    color = Gray500
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Column(
                                            modifier = Modifier.padding(start = 34.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row {
                                                Text("Rep. Legal: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray700)
                                                Text(company.representanteLegal, fontSize = 12.sp, color = Gray600)
                                            }
                                            Row {
                                                Text("Rep. Trab: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Gray700)
                                                Text(company.representanteTrabajadores?.ifBlank { "—" } ?: "—", fontSize = 12.sp, color = Gray600)
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { 
                                                    companyToEdit = company
                                                    showAddEditDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray500, modifier = Modifier.size(18.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { 
                                                    companyToDelete = company
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(18.dp))
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

        // FAB for adding company on mobile (aligned at bottom right)
        if (!isExpanded) {
            FloatingActionButton(
                onClick = { 
                    companyToEdit = null
                    showAddEditDialog = true 
                },
                containerColor = NavyPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Empresa")
            }
        }
    }

    // Dialog for Add/Edit Company
    if (showAddEditDialog) {
        AddEditCompanyDialog(
            company = companyToEdit,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { updated ->
                isLoading = true
                if (companyToEdit == null) {
                    // Create new company
                    val newComp = updated.copy(creatorEmail = user.email)
                    SupabaseRepository.insertData("companies", newComp, Company.serializer()) { success ->
                        if (success) {
                            Toast.makeText(context, "Empresa guardada exitosamente", Toast.LENGTH_SHORT).show()
                            loadData()
                        } else {
                            Toast.makeText(context, "Error al guardar la empresa", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                        showAddEditDialog = false
                    }
                } else {
                    // Update existing company
                    SupabaseRepository.updateData("companies", companyToEdit!!.id ?: return@Button, updated, Company.serializer()) { success ->
                        if (success) {
                            Toast.makeText(context, "Empresa actualizada exitosamente", Toast.LENGTH_SHORT).show()
                            loadData()
                        } else {
                            Toast.makeText(context, "Error al actualizar la empresa", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                        showAddEditDialog = false
                    }
                }
            }
        )
    }

    // Delete confirmation dialog
    if (companyToDelete != null) {
        DeleteCompanyConfirmationDialog(
            companyName = companyToDelete!!.name,
            onDismiss = { companyToDelete = null },
            onConfirm = {
                val comp = companyToDelete!!
                companyToDelete = null
                isLoading = true
                SupabaseRepository.deleteData("companies", comp.id ?: return@launch) { success ->
                    if (success) {
                        Toast.makeText(context, "Empresa eliminada", Toast.LENGTH_SHORT).show()
                        loadData()
                    } else {
                        Toast.makeText(context, "Error al eliminar la empresa", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddEditCompanyDialog(
    company: Company? = null,
    onDismiss: () -> Unit,
    onConfirm: (Company) -> Unit
) {
    var name by remember { mutableStateOf(company?.name ?: "") }
    var rfc by remember { mutableStateOf(company?.rfc ?: "") }
    var representanteLegal by remember { mutableStateOf(company?.representanteLegal ?: "") }
    var representanteTrabajadores by remember { mutableStateOf(company?.representanteTrabajadores ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (company == null) "Registrar Empresa" else "Editar Empresa", 
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Gray900)
            ) 
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre o Razón Social") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rfc,
                    onValueChange = { rfc = it.uppercase() },
                    label = { Text("RFC") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Gray100)
                Text("Datos para firma DC-3", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Gray700))
                OutlinedTextField(
                    value = representanteLegal,
                    onValueChange = { representanteLegal = it },
                    label = { Text("Patrón o Representante Legal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = representanteTrabajadores,
                    onValueChange = { representanteTrabajadores = it },
                    label = { Text("Representante de los Trabajadores") },
                    supportingText = { Text("Obligatorio para empresas con >50 trabajadores", style = MaterialTheme.typography.bodySmall.copy(color = Gray500)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = Company(
                        id = company?.id,
                        name = name,
                        rfc = rfc,
                        representanteLegal = representanteLegal,
                        representanteTrabajadores = representanteTrabajadores.ifBlank { null },
                        creatorEmail = company?.creatorEmail
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank() && rfc.isNotBlank() && representanteLegal.isNotBlank()
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
fun DeleteCompanyConfirmationDialog(
    companyName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Empresa", fontWeight = FontWeight.Bold, color = ErrorRed) },
        text = { Text("¿Estás seguro de que deseas eliminar permanentemente a $companyName? Esta acción no se puede deshacer.", color = Gray700) },
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
