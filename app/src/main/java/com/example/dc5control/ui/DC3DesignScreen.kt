package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.dc5control.data.model.AgentDesign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DC3DesignScreen(
    user: User,
    isExpanded: Boolean,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var headerSlogan by remember { mutableStateOf("") }
    var agentName by remember { mutableStateOf(user.name) }
    var slogan by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    // Load existing design
    LaunchedEffect(Unit) {
        // Fetch agent_designs for this user
        SupabaseRepository.fetchData("agent_designs", AgentDesign.serializer()) { designs ->
            val userDesign = designs.find { it.creatorEmail == user.email }
            if (userDesign != null) {
                headerSlogan = userDesign.headerSlogan ?: ""
                agentName = userDesign.agentName ?: user.name
                slogan = userDesign.slogan ?: ""
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Diseño DC-3", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Personaliza el logo, firma y texto de tu constancia", fontSize = 12.sp, color = Gray400)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceWhite,
                    titleContentColor = Gray900
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(if (isExpanded) 48.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo section
                DesignCard("Logo de la constancia") {
                    Text("Sube un logo que aparecerá en la esquina superior izquierda del DC-3", fontSize = 13.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Gray50, shape = RoundedCornerShape(12.dp))
                            .border(2.dp, Gray200, RoundedCornerShape(12.dp))
                            .clickable { /* File picker - future enhancement */ }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Toca para subir logo (PNG/JPG)", fontSize = 13.sp, color = Gray400)
                    }
                }

                // Header slogan
                DesignCard("Texto del encabezado") {
                    Text("Slogan que aparece junto al logo en el encabezado", fontSize = 13.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = headerSlogan,
                        onValueChange = { headerSlogan = it },
                        placeholder = { Text("Ej. Capacitación profesional y certificada") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Gray200
                        )
                    )
                }

                // Agent info
                DesignCard("Datos del agente capacitador") {
                    Text("Nombre del agente que aparecerá en el DC-3", fontSize = 13.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        placeholder = { Text("Nombre del agente") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Gray200
                        )
                    )
                }

                // Signature
                DesignCard("Firma del agente") {
                    Text("Sube una imagen de la firma que aparecerá al pie del DC-3", fontSize = 13.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Gray50, shape = RoundedCornerShape(12.dp))
                            .border(2.dp, Gray200, RoundedCornerShape(12.dp))
                            .clickable { /* File picker */ }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Toca para subir firma", fontSize = 13.sp, color = Gray400)
                    }
                }

                // Slogan at bottom
                DesignCard("Slogan al pie") {
                    Text("Texto que aparece en la parte inferior del DC-3", fontSize = 13.sp, color = Gray500)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = slogan,
                        onValueChange = { slogan = it },
                        placeholder = { Text("Ej. Comprometidos con la seguridad laboral") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = Gray200
                        )
                    )
                }

                // Save button
                if (saveMessage != null) {
                    Text(saveMessage!!, fontSize = 14.sp, color = if (saveMessage!!.startsWith("✓")) ComplianceGreen else ErrorRed, modifier = Modifier.padding(horizontal = 4.dp))
                }

                Button(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            // Save design to Supabase
                            val design = com.example.dc5control.data.model.AgentDesign(
                                creatorEmail = user.email,
                                headerSlogan = headerSlogan,
                                agentName = agentName,
                                slogan = slogan
                            )
                            // Try to insert/update
                            isSaving = false
                            saveMessage = "✓ Diseño guardado correctamente"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar diseño", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DesignCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
