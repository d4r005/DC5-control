// ============================================================
// DC3Screen.kt  — Formulario Jetpack Compose para generar DC-3
// Usa PdfGenerator.generate() que rellena la plantilla oficial STPS
// ============================================================

package com.example.dc5control.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.dc5control.util.DC3FormData
import com.example.dc5control.util.PdfGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3Screen() {
    val context = LocalContext.current

    // Datos del trabajador
    var nombreTrabajador by remember { mutableStateOf("") }
    var curp             by remember { mutableStateOf("") }
    var ocupacion        by remember { mutableStateOf("") }
    var puesto           by remember { mutableStateOf("") }

    // Datos de la empresa
    var razonSocial by remember { mutableStateOf("") }
    var rfc         by remember { mutableStateOf("") }

    // Datos del curso
    var nombreCurso  by remember { mutableStateOf("") }
    var duracion     by remember { mutableStateOf("") }
    var fechaInicio  by remember { mutableStateOf("") }   // dd/mm/yyyy
    var fechaFin     by remember { mutableStateOf("") }   // dd/mm/yyyy
    var areaTematica by remember { mutableStateOf("") }
    var agente       by remember { mutableStateOf("") }

    // Firmas
    var instructor    by remember { mutableStateOf("") }
    var patron        by remember { mutableStateOf("") }
    var representante by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Generar DC-3") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── TRABAJADOR ──────────────────────────────────────────────────
            Text("Datos del trabajador", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = nombreTrabajador, onValueChange = { nombreTrabajador = it },
                label = { Text("Nombre completo (AP, AM, Nombre)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = curp, onValueChange = { curp = it.uppercase() },
                label = { Text("CURP (18 caracteres)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ocupacion, onValueChange = { ocupacion = it },
                label = { Text("Ocupación específica (CNO)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = puesto, onValueChange = { puesto = it },
                label = { Text("Puesto") },
                modifier = Modifier.fillMaxWidth()
            )

            // ── EMPRESA ─────────────────────────────────────────────────────
            Text("Datos de la empresa", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = razonSocial, onValueChange = { razonSocial = it },
                label = { Text("Razón social") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rfc, onValueChange = { rfc = it.uppercase() },
                label = { Text("RFC con homoclave") },
                modifier = Modifier.fillMaxWidth()
            )

            // ── CURSO ───────────────────────────────────────────────────────
            Text("Datos del programa de capacitación", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = nombreCurso, onValueChange = { nombreCurso = it },
                label = { Text("Nombre del curso") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = duracion, onValueChange = { duracion = it },
                label = { Text("Duración en horas") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fechaInicio, onValueChange = { fechaInicio = it },
                label = { Text("Fecha inicio (dd/mm/yyyy)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fechaFin, onValueChange = { fechaFin = it },
                label = { Text("Fecha fin (dd/mm/yyyy)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = areaTematica, onValueChange = { areaTematica = it },
                label = { Text("Área temática del curso (catálogo STPS)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = agente, onValueChange = { agente = it },
                label = { Text("Nombre del agente capacitador o STPS") },
                modifier = Modifier.fillMaxWidth()
            )

            // ── FIRMAS ──────────────────────────────────────────────────────
            Text("Firmas", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = instructor, onValueChange = { instructor = it },
                label = { Text("Instructor o tutor") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = patron, onValueChange = { patron = it },
                label = { Text("Patrón o representante legal") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = representante, onValueChange = { representante = it },
                label = { Text("Representante de los trabajadores (opcional, >50 empleados)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── BOTÓN GENERAR ───────────────────────────────────────────────
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    try {
                        val data = DC3FormData(
                            nombreTrabajador = nombreTrabajador,
                            curp             = curp,
                            ocupacion        = ocupacion,
                            puesto           = puesto,
                            razonSocial      = razonSocial,
                            rfc              = rfc,
                            nombreCurso      = nombreCurso,
                            duracionHoras    = duracion,
                            fechaInicio      = fechaInicio,
                            fechaFin         = fechaFin,
                            areaTematica     = areaTematica,
                            agenteCapacitador = agente,
                            instructor       = instructor,
                            representanteLegal = patron,
                            representanteTrabajadores = representante.ifBlank { null }
                        )

                        val file = PdfGenerator.generate(context, data)
                        val uri  = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)

                    } catch (e: Exception) {
                        Toast.makeText(
                            context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG
                        ).show()
                    }
                }
            ) {
                Text("⬇  Generar PDF DC-3 oficial")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
