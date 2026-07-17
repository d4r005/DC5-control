package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.components.SignaturePad
import com.example.dc5control.util.CloudflareHelper
import com.example.dc5control.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3GenerationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedInstructor by remember { mutableStateOf<Instructor?>(null) }
    var companyName by remember { mutableStateOf("") }
    var companyRfc by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    var showSignaturePad by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }

    val courses = remember { mutableStateListOf<Course>() }
    val instructors = remember { mutableStateListOf<Instructor>() }
    val employees = remember { mutableStateListOf<Employee>() }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("courses", Course.serializer()) { fetched ->
            courses.addAll(fetched)
        }
        SupabaseRepository.fetchData("instructors", Instructor.serializer()) { fetched ->
            instructors.addAll(fetched)
        }
        SupabaseRepository.fetchData("employees", Employee.serializer()) { fetched ->
            employees.addAll(fetched)
        }
    }

    if (showSignaturePad) {
        SignaturePad(
            onSave = { bitmap ->
                scope.launch {
                    isGenerating = true
                    withContext(Dispatchers.IO) {
                        // Cargar logo si existe en assets (opcional)
                        val logoBitmap = try {
                            context.assets.open("logo_luber.png").use { 
                                android.graphics.BitmapFactory.decodeStream(it)
                            }
                        } catch (e: Exception) { null }

                        // Generar DC-3 con PDFBox para cada empleado activo
                        employees.filter { it.active }.forEach { employee ->
                            val file = PdfGenerator.generateDC3(
                                context = context,
                                employee = employee,
                                course = selectedCourse!!,
                                instructor = selectedInstructor!!,
                                companyName = companyName,
                                companyRfc = companyRfc,
                                startDate = startDate,
                                endDate = endDate,
                                signatureBitmap = bitmap,
                                logoBitmap = logoBitmap
                            )

                            // Subir PDF a Cloudflare
                            CloudflareHelper.uploadPdf(
                                file = file,
                                onSuccess = { 
                                    android.util.Log.d("Cloudflare", "PDF subidó con éxito: ${file.name}")
                                },
                                onError = { error ->
                                    android.util.Log.e("Cloudflare", "Error al subir PDF: $error")
                                }
                            )

                            // Guardar registro del DC-3 generado
                            val record = DC3Record(
                                workerId = employee.curp,
                                workerName = "${employee.lastName} ${employee.firstName}",
                                courseName = selectedCourse!!.name,
                                companyName = companyName,
                                startDate = startDate,
                                endDate = endDate
                            )
                            SupabaseRepository.insertData("dc3_records", record, DC3Record.serializer()) { success ->
                                if (success) {
                                    android.util.Log.d("Supabase", "Registro guardado en Cloudflare/Supabase")
                                } else {
                                    android.util.Log.e("Supabase", "Error al guardar registro")
                                }
                            }
                        }
                    }
                    isGenerating = false
                    showSignaturePad = false
                    onBack()
                }
            },
            onDismiss = { showSignaturePad = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Generar DC-3") },
                    navigationIcon = {
                        TextButton(onClick = onBack) { Text("←") }
                    }
                )
            }
        ) { padding ->
            if (isGenerating) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Generando constancias DC-3 con PDFBox...")
                    }
                }
                return@Scaffold
            }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- Datos de la Empresa ---
                Text("Datos de la Empresa", style = MaterialTheme.typography.titleMedium)
                TextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Nombre o Razón Social") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = companyRfc,
                    onValueChange = { companyRfc = it },
                    label = { Text("RFC") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Curso y Agente Capacitador", style = MaterialTheme.typography.titleMedium)

                // --- Selección de Curso ---
                Text("Cursos disponibles:", style = MaterialTheme.typography.bodySmall)
                courses.forEach { course ->
                    OutlinedButton(
                        onClick = { selectedCourse = course },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedCourse == course)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(course.name)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Selección de Instructor ---
                Text("Agente capacitador:", style = MaterialTheme.typography.bodySmall)
                instructors.forEach { instructor ->
                    OutlinedButton(
                        onClick = { selectedInstructor = instructor },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedInstructor == instructor)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(instructor.fullName)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Fechas ---
                TextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Fecha Inicio (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Fecha Fin (dd/MM/yyyy)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- Info ---
                Text(
                    "Se generarán ${employees.count { it.active }} constancias DC-3 (una por empleado activo)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showSignaturePad = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedCourse != null && selectedInstructor != null &&
                             companyName.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()
                ) {
                    Text("Firmar y Generar DC-3")
                }
            }
        }
    }
}
