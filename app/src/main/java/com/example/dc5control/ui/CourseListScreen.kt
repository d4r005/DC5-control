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
import com.example.dc5control.data.model.Course
import com.example.dc5control.data.repository.SupabaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(onBack: () -> Unit) {
    val courses = remember { mutableStateListOf<Course>() }
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { fetchedCourses ->
            courses.clear()
            courses.addAll(fetchedCourses)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de Cursos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<-")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Curso")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(courses) { course ->
                ListItem(
                    headlineContent = { Text(course.name) },
                    supportingContent = { Text("${course.durationHours} hrs - ${course.thematicArea}") }
                )
                HorizontalDivider()
            }
        }
    }

    if (showAddDialog) {
        AddCourseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, hours, area ->
                val newCourse = Course(name = name, durationHours = hours, thematicArea = area)
                SupabaseRepository.insertData("courses", newCourse, Course.serializer()) { success ->
                    if (success) {
                        courses.add(newCourse)
                        showAddDialog = false
                    }
                }
            }
        )
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Curso") },
        text = {
            Column {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                TextField(value = hours, onValueChange = { hours = it }, label = { Text("Horas") })
                TextField(value = area, onValueChange = { area = it }, label = { Text("Área Temática") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, hours.toIntOrNull() ?: 0, area) }) {
                Text("Agregar")
            }
        }
    )
}
