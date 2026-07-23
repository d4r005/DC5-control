package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(user: User, isExpanded: Boolean, onBack: () -> Unit) {
    val courses = remember { mutableStateListOf<Course>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshCourses() {
        isLoading = true
        SupabaseRepository.fetchData("courses", Course.serializer()) { fetchedCourses ->
            courses.clear()
            courses.addAll(fetchedCourses)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshCourses()
    }

    // Filter courses: ADMIN sees all, USER sees only their creatorEmail courses
    val filteredCourses = remember(courses, user) {
        if (user.role == "ADMIN") {
            courses
        } else {
            courses.filter { it.creatorEmail == user.email }
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
                            text = "Cursos",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray900
                        )
                        Text(
                            text = "Catálogo de cursos",
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
                } else if (filteredCourses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay cursos registrados.",
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
                                    text = "Curso",
                                    modifier = Modifier.weight(0.4f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Duración",
                                    modifier = Modifier.weight(0.18f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Área Temática",
                                    modifier = Modifier.weight(0.25f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Acciones",
                                    modifier = Modifier.weight(0.17f),
                                    fontWeight = FontWeight.Bold,
                                    color = Gray700,
                                    style = MaterialTheme.typography.titleSmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                            HorizontalDivider(color = Gray200)

                            // Table Rows
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredCourses) { course ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = course.name,
                                            modifier = Modifier.weight(0.4f),
                                            fontWeight = FontWeight.Bold,
                                            color = Gray900,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Box(
                                            modifier = Modifier.weight(0.18f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(NavySurface)
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = course.durationHours,
                                                    color = NavyPrimary,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = course.thematicArea ?: "–",
                                            modifier = Modifier.weight(0.25f),
                                            color = Gray700,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Row(
                                            modifier = Modifier.weight(0.17f),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { editingCourse = course }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar",
                                                    tint = NavyPrimary
                                                )
                                            }
                                            IconButton(onClick = {
                                                course.id?.let { id ->
                                                    SupabaseRepository.deleteData("courses", id.toString()) { success ->
                                                        if (success) {
                                                            refreshCourses()
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
                            items(filteredCourses) { course ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = course.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Gray900
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(NavySurface)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = course.durationHours,
                                                color = NavyPrimary,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = course.thematicArea ?: "–",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray500
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = { editingCourse = course }) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = NavyPrimary
                                            )
                                        }
                                        IconButton(onClick = {
                                            course.id?.let { id ->
                                                SupabaseRepository.deleteData("courses", id.toString()) { success ->
                                                    if (success) {
                                                        refreshCourses()
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

        // FAB for adding courses (typically shown on compact, but can be fallback)
        if (!isExpanded) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = NavyPrimary,
                contentColor = SurfaceWhite
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        CourseAddEditDialog(
            course = null,
            user = user,
            onDismiss = { showAddDialog = false },
            onSave = { name, hours, area ->
                val newCourse = Course(
                    name = name,
                    durationHours = hours,
                    thematicArea = area,
                    creatorEmail = user.email
                )
                SupabaseRepository.insertData("courses", newCourse, Course.serializer()) { success ->
                    if (success) {
                        refreshCourses()
                        showAddDialog = false
                    }
                }
            }
        )
    }

    // Edit Dialog
    editingCourse?.let { course ->
        CourseAddEditDialog(
            course = course,
            user = user,
            onDismiss = { editingCourse = null },
            onSave = { name, hours, area ->
                val updated = course.copy(
                    name = name,
                    durationHours = hours,
                    thematicArea = area
                )
                course.id?.let { id ->
                    SupabaseRepository.updateData("courses", id.toString(), updated, Course.serializer()) { success ->
                        if (success) {
                            refreshCourses()
                            editingCourse = null
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun CourseAddEditDialog(
    course: Course?,
    user: User,
    onDismiss: () -> Unit,
    onSave: (name: String, hours: String, area: String) -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var hours by remember { mutableStateOf(course?.durationHours?.replace(" HORAS", "")?.replace(" hrs", "")?.trim() ?: "") }
    var area by remember { mutableStateOf(course?.thematicArea ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (course == null) "Agregar Curso" else "Editar Curso",
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
                    label = { Text("Nombre del Curso") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        focusedLabelColor = NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Duración en Horas (ej. 20)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyPrimary,
                        focusedLabelColor = NavyPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Área Temática") },
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
                    if (name.isNotBlank() && hours.isNotBlank() && area.isNotBlank()) {
                        val formattedHours = if (hours.contains("HORAS", ignoreCase = true)) hours else "$hours HORAS"
                        onSave(name, formattedHours, area)
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
