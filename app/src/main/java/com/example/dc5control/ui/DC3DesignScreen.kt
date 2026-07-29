package com.example.dc5control.ui

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dc5control.data.model.AgentDesign
import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignScreen(
    user: User,
    isExpanded: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("DC-3", "Diploma")

    val scope = rememberCoroutineScope()
    var headerSlogan by remember { mutableStateOf("") }
    var headerSloganX by remember { mutableStateOf(306f) }
    var headerSloganY by remember { mutableStateOf(18f) }
    var headerSloganSize by remember { mutableStateOf(9f) }
    var headerSloganFont by remember { mutableStateOf("Times-Italic") }
    var agentName by remember { mutableStateOf(user.name) }
    var slogan by remember { mutableStateOf("") }
    var diplomaTemplateBase64 by remember { mutableStateOf<String?>(null) }
    var headerLogoBase64 by remember { mutableStateOf<String?>(null) }
    var signatureBase64 by remember { mutableStateOf<String?>(null) }
    var logoBase64 by remember { mutableStateOf<String?>(null) }

    // Position state variables for DC-3 elements
    var logoX by remember { mutableStateOf(16f) }
    var logoY by remember { mutableStateOf(16f) }
    var logoW by remember { mutableStateOf(80f) }
    var logoH by remember { mutableStateOf(80f) }

    var headerLogoX by remember { mutableStateOf(16f) }
    var headerLogoY by remember { mutableStateOf(16f) }
    var headerLogoW by remember { mutableStateOf(120f) }
    var headerLogoH by remember { mutableStateOf(40f) }

    var firmaX by remember { mutableStateOf(16f) }
    var firmaY by remember { mutableStateOf(500f) }
    var firmaW by remember { mutableStateOf(120f) }
    var firmaH by remember { mutableStateOf(40f) }

    var dipFolioX by remember { mutableStateOf(396f) }
    var dipFolioY by remember { mutableStateOf(550f) }
    var dipFolioSz by remember { mutableStateOf(10f) }

    var dipWorkerX by remember { mutableStateOf(396f) }
    var dipWorkerY by remember { mutableStateOf(245f) }
    var dipWorkerSz by remember { mutableStateOf(28f) }

    var dipCourseX by remember { mutableStateOf(396f) }
    var dipCourseY by remember { mutableStateOf(330f) }
    var dipCourseSz by remember { mutableStateOf(18f) }

    var dipDurationX by remember { mutableStateOf(396f) }
    var dipDurationY by remember { mutableStateOf(405f) }
    var dipDurationSz by remember { mutableStateOf(12f) }

    var dipDateX by remember { mutableStateOf(396f) }
    var dipDateY by remember { mutableStateOf(445f) }
    var dipDateSz by remember { mutableStateOf(11f) }

    var dipAgentX by remember { mutableStateOf(396f) }
    var dipAgentY by remember { mutableStateOf(572f) }
    var dipAgentSz by remember { mutableStateOf(10f) }

    var dipStpsX by remember { mutableStateOf(396f) }
    var dipStpsY by remember { mutableStateOf(584f) }
    var dipStpsSz by remember { mutableStateOf(8f) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    // Helpers to convert Uri to Base64
    fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val mimeType = context.contentResolver.getType(uri) ?: "image/png"
                "data:$mimeType;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) { null }
    }

    val headerLogoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { headerLogoBase64 = uriToBase64(it) }
    }
    val signatureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { signatureBase64 = uriToBase64(it) }
    }
    val logoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { logoBase64 = uriToBase64(it) }
    }
    val diplomaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { diplomaTemplateBase64 = uriToBase64(it) }
    }

    // Load existing design
    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("agent_designs", AgentDesign.serializer()) { designs ->
            val userDesign = designs.find { it.creatorEmail == user.email }
            if (userDesign != null) {
                headerSlogan = userDesign.headerSlogan ?: ""
                headerSloganX = userDesign.headerSloganX ?: 306f
                headerSloganY = userDesign.headerSloganY ?: 18f
                headerSloganSize = userDesign.headerSloganSize ?: 9f
                headerSloganFont = userDesign.headerSloganFont ?: "Times-Italic"
                
                agentName = userDesign.agentName ?: user.name
                slogan = userDesign.slogan ?: ""
                diplomaTemplateBase64 = userDesign.diplomaTemplateBase64
                headerLogoBase64 = userDesign.headerLogoBase64
                signatureBase64 = userDesign.firmaBase64
                logoBase64 = userDesign.logoBase64

                // Load position data with defaults
                logoX = userDesign.logoX ?: 16f
                logoY = userDesign.logoY ?: 16f
                logoW = userDesign.logoW ?: 80f
                logoH = userDesign.logoH ?: 80f

                headerLogoX = userDesign.headerLogoX ?: 16f
                headerLogoY = userDesign.headerLogoY ?: 16f
                headerLogoW = userDesign.headerLogoW ?: 120f
                headerLogoH = userDesign.headerLogoH ?: 40f

                firmaX = userDesign.firmaX ?: 16f
                firmaY = userDesign.firmaY ?: 500f
                firmaW = userDesign.firmaW ?: 120f
                firmaH = userDesign.firmaH ?: 40f

                dipFolioX = userDesign.dipFolioX ?: 396f
                dipFolioY = userDesign.dipFolioY ?: 550f
                dipFolioSz = userDesign.dipFolioSz ?: 10f

                dipWorkerX = userDesign.dipWorkerX ?: 396f
                dipWorkerY = userDesign.dipWorkerY ?: 245f
                dipWorkerSz = userDesign.dipWorkerSz ?: 28f

                dipCourseX = userDesign.dipCourseX ?: 396f
                dipCourseY = userDesign.dipCourseY ?: 330f
                dipCourseSz = userDesign.dipCourseSz ?: 18f

                dipDurationX = userDesign.dipDurationX ?: 396f
                dipDurationY = userDesign.dipDurationY ?: 405f
                dipDurationSz = userDesign.dipDurationSz ?: 12f

                dipDateX = userDesign.dipDateX ?: 396f
                dipDateY = userDesign.dipDateY ?: 445f
                dipDateSz = userDesign.dipDateSz ?: 11f

                dipAgentX = userDesign.dipAgentX ?: 396f
                dipAgentY = userDesign.dipAgentY ?: 572f
                dipAgentSz = userDesign.dipAgentSz ?: 10f

                dipStpsX = userDesign.dipStpsX ?: 396f
                dipStpsY = userDesign.dipStpsY ?: 584f
                dipStpsSz = userDesign.dipStpsSz ?: 8f
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Diseño", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Personaliza el formato de tus documentos", fontSize = 12.sp, color = Gray400)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
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
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceWhite,
                    contentColor = NavyPrimary,
                    divider = { HorizontalDivider(color = Gray200) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                if (selectedTab == 0) {
                    // DC-3 Design
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(if (isExpanded) 48.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Logo section
                        DesignCard("Logo de la constancia") {
                            Text("Sube un logo que aparecerá en la esquina superior izquierda del DC-3", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Position controls for logo
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = logoX.toString(),
                                    onValueChange = { logoX = it.toFloatOrNull() ?: logoX },
                                    label = { Text("X") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = logoY.toString(),
                                    onValueChange = { logoY = it.toFloatOrNull() ?: logoY },
                                    label = { Text("Y") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = logoW.toString(),
                                    onValueChange = { logoW = it.toFloatOrNull() ?: logoW },
                                    label = { Text("W") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = logoH.toString(),
                                    onValueChange = { logoH = it.toFloatOrNull() ?: logoH },
                                    label = { Text("H") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(Gray50, shape = RoundedCornerShape(12.dp))
                                    .border(2.dp, Gray200, RoundedCornerShape(12.dp))
                                    .clickable { logoLauncher.launch("image/*") }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (logoBase64 != null) {
                                    AsyncImage(model = logoBase64, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Toca para subir logo (PNG/JPG)", fontSize = 13.sp, color = Gray400)
                                }
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
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", headerSloganX) { headerSloganX = it }
                                CoordinateField("Y", headerSloganY) { headerSloganY = it }
                                CoordinateField("Tam.", headerSloganSize) { headerSloganSize = it }
                            }
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

                        // Header logo
                        DesignCard("Logo del encabezado") {
                            Text("Sube un logo que aparecerá en el encabezado del DC-3", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Position controls for header logo
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = headerLogoX.toString(),
                                    onValueChange = { headerLogoX = it.toFloatOrNull() ?: headerLogoX },
                                    label = { Text("X") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = headerLogoY.toString(),
                                    onValueChange = { headerLogoY = it.toFloatOrNull() ?: headerLogoY },
                                    label = { Text("Y") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = headerLogoW.toString(),
                                    onValueChange = { headerLogoW = it.toFloatOrNull() ?: headerLogoW },
                                    label = { Text("W") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = headerLogoH.toString(),
                                    onValueChange = { headerLogoH = it.toFloatOrNull() ?: headerLogoH },
                                    label = { Text("H") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Gray50, shape = RoundedCornerShape(12.dp))
                                    .border(2.dp, Gray200, RoundedCornerShape(12.dp))
                                    .clickable { headerLogoLauncher.launch("image/*") }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (headerLogoBase64 != null) {
                                    AsyncImage(model = headerLogoBase64, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Toca para subir logo", fontSize = 13.sp, color = Gray400)
                                }
                            }
                        }

                        // Signature
                        DesignCard("Firma del agente") {
                            Text("Sube una imagen de la firma que aparecerá al pie del DC-3", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Position controls for signature
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = firmaX.toString(),
                                    onValueChange = { firmaX = it.toFloatOrNull() ?: firmaX },
                                    label = { Text("X") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = firmaY.toString(),
                                    onValueChange = { firmaY = it.toFloatOrNull() ?: firmaY },
                                    label = { Text("Y") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = firmaW.toString(),
                                    onValueChange = { firmaW = it.toFloatOrNull() ?: firmaW },
                                    label = { Text("W") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                                OutlinedTextField(
                                    value = firmaH.toString(),
                                    onValueChange = { firmaH = it.toFloatOrNull() ?: firmaH },
                                    label = { Text("H") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyPrimary, unfocusedBorderColor = Gray200)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Gray50, shape = RoundedCornerShape(12.dp))
                                    .border(2.dp, Gray200, RoundedCornerShape(12.dp))
                                    .clickable { signatureLauncher.launch("image/*") }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (signatureBase64 != null) {
                                    AsyncImage(model = signatureBase64, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, tint = Gray400, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Toca para subir firma", fontSize = 13.sp, color = Gray400)
                                }
                            }
                        }

                        // Slogan at bottom
                        DesignCard("Slogan al pie") {
                            Text("Texto que aparece in the parte inferior del DC-3", fontSize = 13.sp, color = Gray500)
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
                                    val design = com.example.dc5control.data.model.AgentDesign(
                                        creatorEmail = user.email,
                                        headerSlogan = headerSlogan,
                                        headerSloganX = headerSloganX,
                                        headerSloganY = headerSloganY,
                                        headerSloganSize = headerSloganSize,
                                        headerSloganFont = headerSloganFont,
                                        agentName = agentName,
                                        slogan = slogan,
                                        logoBase64 = logoBase64,
                                        logoX = logoX,
                                        logoY = logoY,
                                        logoW = logoW,
                                        logoH = logoH,
                                        firmaBase64 = signatureBase64,
                                        firmaX = firmaX,
                                        firmaY = firmaY,
                                        firmaW = firmaW,
                                        firmaH = firmaH,
                                        headerLogoBase64 = headerLogoBase64,
                                        headerLogoX = headerLogoX,
                                        headerLogoY = headerLogoY,
                                        headerLogoW = headerLogoW,
                                        headerLogoH = headerLogoH,
                                        diplomaTemplateBase64 = diplomaTemplateBase64,
                                        dipFolioX = dipFolioX,
                                        dipFolioY = dipFolioY,
                                        dipFolioSz = dipFolioSz,
                                        dipWorkerX = dipWorkerX,
                                        dipWorkerY = dipWorkerY,
                                        dipWorkerSz = dipWorkerSz,
                                        dipCourseX = dipCourseX,
                                        dipCourseY = dipCourseY,
                                        dipCourseSz = dipCourseSz,
                                        dipDurationX = dipDurationX,
                                        dipDurationY = dipDurationY,
                                        dipDurationSz = dipDurationSz,
                                        dipDateX = dipDateX,
                                        dipDateY = dipDateY,
                                        dipDateSz = dipDateSz,
                                        dipAgentX = dipAgentX,
                                        dipAgentY = dipAgentY,
                                        dipAgentSz = dipAgentSz,
                                        dipStpsX = dipStpsX,
                                        dipStpsY = dipStpsY,
                                        dipStpsSz = dipStpsSz
                                    )

                                    try {
                                        val existing = SupabaseRepository.fetchDataFilteredSuspend("agent_designs", "creator_email=eq.${user.email}", AgentDesign.serializer()).firstOrNull()
                                        val success = if (existing != null) {
                                            SupabaseRepository.updateDataSuspend("agent_designs", existing.id!!, design, AgentDesign.serializer())
                                        } else {
                                            SupabaseRepository.insertDataSuspend("agent_designs", design, AgentDesign.serializer())
                                        }

                                        if (success) {
                                            saveMessage = "✓ Diseño guardado correctamente"
                                        } else {
                                            saveMessage = "⚠ Error al sincronizar con la nube. Verifica tu conexión."
                                        }
                                    } catch (e: Exception) {
                                        saveMessage = "⚠ Error: ${e.message}. ¿Añadiste la columna en Supabase?"
                                    }

                                    isSaving = false
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
                } else {
                    // Diploma Design
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(if (isExpanded) 48.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DesignCard("Plantilla de Diploma") {
                            Text("Esta es la plantilla que se utiliza para generar tus reconocimientos oficiales.", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.29f)
                                    .background(Gray50, shape = RoundedCornerShape(12.dp))
                                    .border(1.dp, Gray200, RoundedCornerShape(12.dp))
                                    .clickable { diplomaLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (diplomaTemplateBase64 != null) {
                                    AsyncImage(
                                        model = diplomaTemplateBase64,
                                        contentDescription = "Plantilla de Diploma Personalizada",
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    AsyncImage(
                                        model = "file:///android_asset/plantilla_diploma.png",
                                        contentDescription = "Plantilla de Diploma por defecto",
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { diplomaLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Subir nueva plantilla")
                            }

                            if (diplomaTemplateBase64 != null) {
                                TextButton(onClick = { diplomaTemplateBase64 = null }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Restablecer a plantilla por defecto", color = ErrorRed, fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = SuccessSurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Al guardar, se usará esta plantilla para todos tus diplomas.", fontSize = 12.sp, color = SuccessGreen)
                                }
                            }
                        }

                        // Folio Controls
                        DesignCard("Personalización de Folio") {
                            Text("Ajusta la posición y tamaño del identificador único del diploma.", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipFolioX) { dipFolioX = it }
                                CoordinateField("Y", dipFolioY) { dipFolioY = it }
                                CoordinateField("Tam.", dipFolioSz) { dipFolioSz = it }
                            }
                        }

                        DesignCard("Posiciones del Texto en Diploma") {
                            Text("Ajusta las coordenadas X, Y y el tamaño de cada campo.", fontSize = 13.sp, color = Gray500)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text("Nombre del Trabajador", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipWorkerX) { dipWorkerX = it }
                                CoordinateField("Y", dipWorkerY) { dipWorkerY = it }
                                CoordinateField("Tam.", dipWorkerSz) { dipWorkerSz = it }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Nombre del Curso", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipCourseX) { dipCourseX = it }
                                CoordinateField("Y", dipCourseY) { dipCourseY = it }
                                CoordinateField("Tam.", dipCourseSz) { dipCourseSz = it }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Duración", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipDurationX) { dipDurationX = it }
                                CoordinateField("Y", dipDurationY) { dipDurationY = it }
                                CoordinateField("Tam.", dipDurationSz) { dipDurationSz = it }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Fecha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipDateX) { dipDateX = it }
                                CoordinateField("Y", dipDateY) { dipDateY = it }
                                CoordinateField("Tam.", dipDateSz) { dipDateSz = it }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Agente Capacitador", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipAgentX) { dipAgentX = it }
                                CoordinateField("Y", dipAgentY) { dipAgentY = it }
                                CoordinateField("Tam.", dipAgentSz) { dipAgentSz = it }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Registro STPS", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CoordinateField("X", dipStpsX) { dipStpsX = it }
                                CoordinateField("Y", dipStpsY) { dipStpsY = it }
                                CoordinateField("Tam.", dipStpsSz) { dipStpsSz = it }
                            }
                        }

                        Button(
                            onClick = {
                                isSaving = true
                                scope.launch {
                                    val design = com.example.dc5control.data.model.AgentDesign(
                                        creatorEmail = user.email,
                                        headerSlogan = headerSlogan,
                                        headerSloganX = headerSloganX,
                                        headerSloganY = headerSloganY,
                                        headerSloganSize = headerSloganSize,
                                        headerSloganFont = headerSloganFont,
                                        agentName = agentName,
                                        slogan = slogan,
                                        logoBase64 = logoBase64,
                                        logoX = logoX,
                                        logoY = logoY,
                                        logoW = logoW,
                                        logoH = logoH,
                                        firmaBase64 = signatureBase64,
                                        firmaX = firmaX,
                                        firmaY = firmaY,
                                        firmaW = firmaW,
                                        firmaH = firmaH,
                                        headerLogoBase64 = headerLogoBase64,
                                        headerLogoX = headerLogoX,
                                        headerLogoY = headerLogoY,
                                        headerLogoW = headerLogoW,
                                        headerLogoH = headerLogoH,
                                        diplomaTemplateBase64 = diplomaTemplateBase64,
                                        dipFolioX = dipFolioX,
                                        dipFolioY = dipFolioY,
                                        dipFolioSz = dipFolioSz,
                                        dipWorkerX = dipWorkerX,
                                        dipWorkerY = dipWorkerY,
                                        dipWorkerSz = dipWorkerSz,
                                        dipCourseX = dipCourseX,
                                        dipCourseY = dipCourseY,
                                        dipCourseSz = dipCourseSz,
                                        dipDurationX = dipDurationX,
                                        dipDurationY = dipDurationY,
                                        dipDurationSz = dipDurationSz,
                                        dipDateX = dipDateX,
                                        dipDateY = dipDateY,
                                        dipDateSz = dipDateSz,
                                        dipAgentX = dipAgentX,
                                        dipAgentY = dipAgentY,
                                        dipAgentSz = dipAgentSz,
                                        dipStpsX = dipStpsX,
                                        dipStpsY = dipStpsY,
                                        dipStpsSz = dipStpsSz
                                    )
                                    val existing = SupabaseRepository.fetchDataFilteredSuspend("agent_designs", "creator_email=eq.${user.email}", AgentDesign.serializer()).firstOrNull()
                                    if (existing != null) {
                                        SupabaseRepository.updateDataSuspend("agent_designs", existing.id!!, design, AgentDesign.serializer())
                                    } else {
                                        SupabaseRepository.insertDataSuspend("agent_designs", design, AgentDesign.serializer())
                                    }
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
                                Text("Guardar diseño de diploma", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.CoordinateField(label: String, value: Float, onValueChange: (Float) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { onValueChange(it.toFloatOrNull() ?: value) },
        label = { Text(label) },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyPrimary,
            unfocusedBorderColor = Gray200
        )
    )
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