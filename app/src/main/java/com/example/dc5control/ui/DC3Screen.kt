// ============================================================
// DC3Screen.kt
// Pantalla en Jetpack Compose con formulario para capturar
// los datos y generar el PDF del DC-3.
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
import com.example.dc5control.util.DC3Data
import com.example.dc5control.util.DC3Generator
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3Screen() {
    val context = LocalContext.current

    var nombreTrabajador by remember { mutableStateOf("") }
    var curp by remember { mutableStateOf("") }
    var ocupacion by remember { mutableStateOf("") }
    var puesto by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var nombreCurso by remember { mutableStateOf("") }
    var duracionHoras by remember { mutableStateOf("") }
    var periodoInicio by remember { mutableStateOf("") }
    var periodoFin by remember { mutableStateOf("") }
    var areaTematica by remember { mutableStateOf("") }
    var agenteCapacitador by remember { mutableStateOf("") }
    var instructorNombre by remember { mutableStateOf("") }
    var patronNombre by remember { mutableStateOf("") }
    var representanteTrabajadores by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Generar DC-3") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Datos del trabajador", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nombreTrabajador, { nombreTrabajador = it }, label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(curp, { curp = it }, label = { Text("CURP") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(ocupacion, { ocupacion = it }, label = { Text("Ocupación específica") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(puesto, { puesto = it }, label = { Text("Puesto") }, modifier = Modifier.fillMaxWidth())

            Text("Datos de la empresa", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(razonSocial, { razonSocial = it }, label = { Text("Razón social") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(rfc, { rfc = it }, label = { Text("RFC") }, modifier = Modifier.fillMaxWidth())

            Text("Datos del curso", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(nombreCurso, { nombreCurso = it }, label = { Text("Nombre del curso") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(duracionHoras, { duracionHoras = it }, label = { Text("Duración en horas") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(periodoInicio, { periodoInicio = it }, label = { Text("Periodo inicio (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(periodoFin, { periodoFin = it }, label = { Text("Periodo fin (dd/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(areaTematica, { areaTematica = it }, label = { Text("Área temática") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(agenteCapacitador, { agenteCapacitador = it }, label = { Text("Agente capacitador o STPS") }, modifier = Modifier.fillMaxWidth())

            Text("Firmas", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(instructorNombre, { instructorNombre = it }, label = { Text("Instructor o tutor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(patronNombre, { patronNombre = it }, label = { Text("Patrón o representante legal") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(representanteTrabajadores, { representanteTrabajadores = it }, label = { Text("Representante de los trabajadores (opcional)") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val data = DC3Data(
                        nombreTrabajador = nombreTrabajador,
                        curp = curp,
                        ocupacionEspecifica = ocupacion,
                        puesto = puesto,
                        razonSocial = razonSocial,
                        rfc = rfc,
                        nombreCurso = nombreCurso,
                        duracionHoras = duracionHoras,
                        periodoInicio = periodoInicio,
                        periodoFin = periodoFin,
                        areaTematica = areaTematica,
                        agenteCapacitador = agenteCapacitador,
                        instructorNombre = instructorNombre,
                        patronNombre = patronNombre,
                        representanteTrabajadoresNombre = representanteTrabajadores.ifBlank { null }
                    )

                    val outputFile = File(context.getExternalFilesDir(null), "DC3_${curp.ifBlank { "documento" }}.pdf")
                    try {
                        DC3Generator(context).generar(data, outputFile)

                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", outputFile
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al generar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generar PDF")
            }
        }
    }
}
