package com.example.dc5control.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        val dir = context.getExternalFilesDir(null)
        files = dir?.listFiles { _, name -> name.endsWith(".pdf") }
            ?.sortedByDescending { it.lastModified() }
            ?.toList() ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de DC-3") },
                navigationIcon = { IconButton(onClick = onBack) { Text("<-") } }
            )
        }
    ) { padding ->
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No hay documentos generados aún.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(files) { file ->
                    ListItem(
                        headlineContent = { Text(file.name) },
                        supportingContent = { Text("Tamaño: ${file.length() / 1024} KB") },
                        leadingContent = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                        trailingContent = {
                            Button(onClick = { /* TODO: Open PDF */ }) {
                                Text("Abrir")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
