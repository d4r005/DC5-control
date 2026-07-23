package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.DC3Record
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@Composable
fun DC3HistoryScreen(user: User, isExpanded: Boolean, onBack: () -> Unit) {
    val records = remember { mutableStateListOf<DC3Record>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("dc3_records", DC3Record.serializer()) { fetched ->
            records.clear()
            records.addAll(fetched)
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        // Header
        Surface(
            color = SurfaceWhite,
            shadowElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (isExpanded) 32.dp else 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Gray900)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Constancias DC-3", fontSize = if (isExpanded) 20.sp else 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                        Text("Historial de constancias generadas", fontSize = 14.sp, color = Gray400)
                    }
                }
                HorizontalDivider(color = Gray200)
            }
        }

        // Content
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
            return@Column
        }

        if (records.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sin historial de constancias", color = Gray300, fontSize = 14.sp)
            }
            return@Column
        }

        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isExpanded) 24.dp else 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, Gray200)
        ) {
            if (isExpanded) {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TRABAJADOR", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("CURSO", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("EMPRESA", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("FECHAS", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                    Text("ACCIONES", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Gray400)
                }
                HorizontalDivider(color = Gray100)
            }

            LazyColumn {
                items(records) { record ->
                    if (isExpanded) {
                        DC3RecordRow(record)
                    } else {
                        DC3RecordCard(record)
                    }
                    HorizontalDivider(color = Gray50, modifier = Modifier.padding(horizontal = if (isExpanded) 0.dp else 16.dp))
                }
            }
        }
    }
}

@Composable
fun DC3RecordRow(record: DC3Record) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(record.workerName.ifBlank { "–" }, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
            Text(record.workerId, fontSize = 12.sp, color = Gray400, fontFamily = FontFamily.Monospace)
        }
        Text(record.courseName.ifBlank { "–" }, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Gray700)
        Text(record.companyName.ifBlank { "–" }, modifier = Modifier.weight(1f), fontSize = 14.sp, color = Gray500)
        Text("${record.startDate} – ${record.endDate}", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Gray400)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { },
                label = { Text("Ver", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = NavySurface,
                    labelColor = NavyPrimary
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
            AssistChip(
                onClick = { },
                label = { Text("PDF", fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = NavyPrimary,
                    labelColor = Color.White
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Gray500, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = {
                record.id?.let { id ->
                    SupabaseRepository.deleteData("dc3_records", id.toString()) { }
                }
            }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun DC3RecordCard(record: DC3Record) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(record.workerName.ifBlank { "–" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(record.workerId, fontSize = 12.sp, color = Gray400, fontFamily = FontFamily.Monospace)
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SuccessSurface
            ) {
                Text(
                    record.resultText.ifBlank { "Acreditado" },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Book, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(record.courseName, fontSize = 13.sp, color = Gray700)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = Gray400, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(record.companyName, fontSize = 13.sp, color = Gray500)
            Spacer(modifier = Modifier.weight(1f))
            Text("${record.startDate} – ${record.endDate}", fontSize = 12.sp, color = Gray400)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { }) {
                Text("Ver", color = NavyPrimary, fontSize = 13.sp)
            }
            TextButton(onClick = { }) {
                Text("PDF", color = NavyPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            record.id?.let { id ->
                IconButton(onClick = { SupabaseRepository.deleteData("dc3_records", id.toString()) { } }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
