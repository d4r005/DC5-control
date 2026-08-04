package com.example.dc5control.ui

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("DC-3", "Diploma")

    val scope = rememberCoroutineScope()

    // --- Estado compartido entre lista y editor ---
    val designs = remember { mutableStateListOf<AgentDesign>() }
    var selectedDesign by remember { mutableStateOf<AgentDesign?>(null) }
    var isLoadingDesigns by remember { mutableStateOf(false) }

    // Campos editables (se sincronizan con selectedDesign cuando cambia)
    var headerSlogan by remember { mutableStateOf("") }
    var dc3TemplateUrl by remember { mutableStateOf<String?>(null) }
    var diplomaTemplateUrl by remember { mutableStateOf<String?>(null) }
    var headerSloganX by remember { mutableFloatStateOf(306f) }
    var headerSloganY by remember { mutableStateOf(18f) }
    var headerSloganSize by remember { mutableStateOf(9f) }
    var headerSloganFont by remember { mutableStateOf("Times-Italic") }
    var agentName by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }
    var sloganX by remember { mutableStateOf(30f) }
    var sloganY by remember { mutableStateOf(445f) }
    var sloganSize by remember { mutableStateOf(7f) }
    var sloganFont by remember { mutableStateOf("Helvetica") }

    // Logo, header logo, firma, etc.
    var diplomaTemplateBase64 by remember { mutableStateOf<String?>(null) }
    var headerLogoBase64 by remember { mutableStateOf<String?>(null) }
    var signatureBase64 by remember { mutableStateOf<String?>(null) }
    var logoBase64 by remember { mutableStateOf<String?>(null) }

    var qrX by remember { mutableStateOf(480f) }
    var qrY by remember { mutableStateOf(60f) }
    var qrSz by remember { mutableStateOf(60f) }

    // Posición logo
    var logoX by remember { mutableStateOf(16f) }
    var logoY by remember { mutableStateOf(16f) }
    var logoW by remember { mutableStateOf(80f) }
    var logoH by remember { mutableStateOf(80f) }

    // Posición header logo
    var headerLogoX by remember { mutableStateOf(16f) }
    var headerLogoY by remember { mutableStateOf(16f) }
    var headerLogoW by remember { mutableStateOf(120f) }
    var headerLogoH by remember { mutableStateOf(40f) }

    // Posición firma
    var firmaX by remember { mutableStateOf(16f) }
    var firmaY by remember { mutableStateOf(500f) }
    var firmaW by remember { mutableStateOf(120f) }
    var firmaH by remember { mutableStateOf(40f) }

    // Posiciones diploma
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

    var dipCedulaX by remember { mutableStateOf(396f) }
    var dipCedulaY by remember { mutableStateOf(596f) }
    var dipCedulaSz by remember { mutableStateOf(8f) }

    var dipQrX by remember { mutableStateOf(680f) }
    var dipQrY by remember { mutableStateOf(500f) }
    var dipQrSz by remember { mutableStateOf(50f) }

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
    var newDc3TemplateUri by remember { mutableStateOf<Uri?>(null) }
    val dc3TemplateLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { newDc3TemplateUri = it }
    }
    var newDiplomaTemplateUri by remember { mutableStateOf<Uri?>(null) }
    val diplomaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            newDiplomaTemplateUri = it
            if (context.contentResolver.getType(it) != "application/pdf") {
                diplomaTemplateBase64 = uriToBase64(it)
            } else {
                diplomaTemplateBase64 = null // Clear preview if it's a PDF
            }
        }
    }

    // --- Carga de diseños según rol ---
    LaunchedEffect(Unit) {
        isLoadingDesigns = true
        val query = if (user.role == "ADMIN") "" else "creator_email=eq.${user.email}"
        SupabaseRepository.fetchData("agent_designs", AgentDesign.serializer()) { fetched ->
            // Deduplicar por creator_email — quedarse con el más reciente
            val grouped = fetched.groupBy { it.creatorEmail ?: "unknown" }
                .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt ?: "" } ?: list.first() }
                .values.toList()
            designs.clear()
            designs.addAll(grouped)
            // Preseleccionar diseño propio (admin incluido)
            val own = designs.find { it.creatorEmail == user.email }
            if (own != null) {
                selectedDesign = own
            }
            isLoadingDesigns = false
        }
    }

    // Sincronizar fields con selectedDesign cuando cambie
    LaunchedEffect(selectedDesign) {
        val design = selectedDesign
        if (design != null) {
            headerSlogan = design.headerSlogan ?: ""
            dc3TemplateUrl = design.dc3TemplateUrl
            diplomaTemplateUrl = design.diplomaTemplateUrl
            headerSloganX = design.headerSloganX ?: 306f
            headerSloganY = design.headerSloganY ?: 18f
            headerSloganSize = design.headerSloganSize ?: 9f
            headerSloganFont = design.headerSloganFont ?: "Times-Italic"
            agentName = design.agentName ?: user.name
            slogan = design.slogan ?: ""
            sloganX = design.sloganX ?: 30f
            sloganY = design.sloganY ?: 445f
            sloganSize = design.sloganSize ?: 7f
            sloganFont = design.sloganFont ?: "Helvetica"
            diplomaTemplateBase64 = design.diplomaTemplateBase64
            headerLogoBase64 = design.headerLogoBase64
            signatureBase64 = design.firmaBase64
            logoBase64 = design.logoBase64

            qrX = design.qrX ?: 480f
            qrY = design.qrY ?: 60f
            qrSz = design.qrSz ?: 60f

            logoX = design.logoX ?: 16f
            logoY = design.logoY ?: 16f
            logoW = design.logoW ?: 80f
            logoH = design.logoH ?: 80f

            headerLogoX = design.headerLogoX ?: 16f
            headerLogoY = design.headerLogoY ?: 16f
            headerLogoW = design.headerLogoW ?: 120f
            headerLogoH = design.headerLogoH ?: 40f

            firmaX = design.firmaX ?: 16f
            firmaY = design.firmaY ?: 500f
            firmaW = design.firmaW ?: 120f
            firmaH = design.firmaH ?: 40f

            dipFolioX = design.dipFolioX ?: 396f
            dipFolioY = design.dipFolioY ?: 550f
            dipFolioSz = design.dipFolioSz ?: 10f

            dipWorkerX = design.dipWorkerX ?: 396f
            dipWorkerY = design.dipWorkerY ?: 245f
            dipWorkerSz = design.dipWorkerSz ?: 28f

            dipCourseX = design.dipCourseX ?: 396f
            dipCourseY = design.dipCourseY ?: 330f
            dipCourseSz = design.dipCourseSz ?: 18f

            dipDurationX = design.dipDurationX ?: 396f
            dipDurationY = design.dipDurationY ?: 405f
            dipDurationSz = design.dipDurationSz ?: 12f

            dipDateX = design.dipDateX ?: 396f
            dipDateY = design.dipDateY ?: 445f
            dipDateSz = design.dipDateSz ?: 11f

            dipAgentX = design.dipAgentX ?: 396f
            dipAgentY = design.dipAgentY ?: 572f
            dipAgentSz = design.dipAgentSz ?: 10f

            dipStpsX = design.dipStpsX ?: 396f
            dipStpsY = design.dipStpsY ?: 584f
            dipStpsSz = design.dipStpsSz ?: 8f

            dipCedulaX = design.dipCedulaX ?: 396f
            dipCedulaY = design.dipCedulaY ?: 596f
            dipCedulaSz = design.dipCedulaSz ?: 8f

            dipQrX = design.dipQrX ?: 680f
            dipQrY = design.dipQrY ?: 500f
            dipQrSz = design.dipQrSz ?: 50f
        } else {
            // Valores por defecto (cuando no hay diseño seleccionado)
            headerSlogan = ""
            dc3TemplateUrl = null
            diplomaTemplateUrl = null
            headerSloganX = 306f
            headerSloganY = 18f
            headerSloganSize = 9f
            headerSloganFont = "Times-Italic"
            agentName = user.name
            slogan = ""
            sloganX = 30f
            sloganY = 445f
            sloganSize = 7f
            sloganFont = "Helvetica"
            diplomaTemplateBase64 = null
            headerLogoBase64 = null
            signatureBase64 = null
            logoBase64 = null

            qrX = 480f
            qrY = 60f
            qrSz = 60f

            logoX = 16f
            logoY = 16f
            logoW = 80f
            logoH = 80f

            headerLogoX = 16f
            headerLogoY = 16f
            headerLogoW = 120f
            headerLogoH = 40f

            firmaX = 16f
            firmaY = 500f
            firmaW = 120f
            firmaH = 40f

            dipFolioX = 396f
            dipFolioY = 550f
            dipFolioSz = 10f

            dipWorkerX = 396f
            dipWorkerY = 245f
            dipWorkerSz = 28f

            dipCourseX = 396f
            dipCourseY = 330f
            dipCourseSz = 18f

            dipDurationX = 396f
            dipDurationY = 405f
            dipDurationSz = 12f

            dipDateX = 396f
            dipDateY = 445f
            dipDateSz = 11f

            dipAgentX = 396f
            dipAgentY = 572f
            dipAgentSz = 10f

            dipStpsX = 396f
            dipStpsY = 584f
            dipStpsSz = 8f

            dipCedulaX = 396f
            dipCedulaY = 596f
            dipCedulaSz = 8f

            dipQrX = 680f
            dipQrY = 500f
            dipQrSz = 50f
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
        if (isLoadingDesigns) {
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

                // ======================================================
                // Lógica de presentación: lista (admin) vs editor (todos)
                // ======================================================
                if (user.role == "ADMIN" && selectedDesign == null && designs.isNotEmpty()) {
                    // ---------- ADMIN: LISTA DE DISEÑOS ----------
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isExpanded) 48.dp else 16.dp)
                    ) {
                        Text(
                            text = "Selecciona un diseño para editar",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray900
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(designs) { design ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .clickable { selectedDesign = design }
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = design.creatorEmail ?: "desconocido",
                                                fontWeight = FontWeight.Bold,
                                                color = Gray900
                                            )
                                            Text(
                                                text = "Última actualización: ${design.updatedAt ?: " — "}",
                                                fontSize = 12.sp,
                                                color = Gray500
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Seleccionar",
                                            tint = Gray400,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // ---------- EDITOR (admin con selección o usuario normal) ----------
                    // Botón para volver a la lista (solo admin con diseño seleccionado)
                    if (user.role == "ADMIN" && selectedDesign != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = if (isExpanded) 48.dp else 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { selectedDesign = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Volver a lista de diseños")
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Editando: ${selectedDesign?.creatorEmail ?: "?"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                    // Detectar landscape para layout de dos columnas
                    val isLandscape = LocalConfiguration.current.screenWidthDp > LocalConfiguration.current.screenHeightDp
                    val useTwoCol = isExpanded || (isLandscape && LocalConfiguration.current.screenWidthDp >= 600)
                    val editorPadding = if (isExpanded) 48.dp else 16.dp

                    // Seleccionamos la pestaña activa (DC-3 o Diploma)
                    if (selectedTab == 0) {
                        // DC-3 Design
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(editorPadding),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Plantilla Base DC-3
                            DesignCard("Plantilla Base DC-3 (PDF)") {
                                Text("Sube el formato oficial en blanco. Se usará como fondo para los datos del agente y trabajadores.", fontSize = 13.sp, color = Gray500)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Gray50,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Gray200)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (newDc3TemplateUri != null || dc3TemplateUrl != null) Icons.Default.PictureAsPdf else Icons.Default.FileOpen,
                                            contentDescription = null,
                                            tint = if (newDc3TemplateUri != null || dc3TemplateUrl != null) NavyPrimary else Gray400
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = when {
                                                    newDc3TemplateUri != null -> "Nuevo archivo seleccionado"
                                                    dc3TemplateUrl != null -> "Plantilla en la nube activa"
                                                    else -> "Plantilla por defecto (Assets)"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (dc3TemplateUrl != null && newDc3TemplateUri == null) {
                                                Text(dc3TemplateUrl!!, style = MaterialTheme.typography.bodySmall, color = Gray400, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Button(
                                            onClick = { dc3TemplateLauncher.launch("application/pdf") },
                                            colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp)
                                        ) {
                                            Text("Cambiar", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

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
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CoordinateField("X", sloganX) { sloganX = it }
                                    CoordinateField("Y", sloganY) { sloganY = it }
                                    CoordinateField("Tam.", sloganSize) { sloganSize = it }
                                }
                            }

                            // QR Code DC-3
                            DesignCard("Código QR (Verificación)") {
                                Text("Ajusta la posición y tamaño del QR en el DC-3", fontSize = 13.sp, color = Gray500)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CoordinateField("X", qrX) { qrX = it }
                                    CoordinateField("Y", qrY) { qrY = it }
                                    CoordinateField("Tam.", qrSz) { qrSz = it }
                                }
                            }

                            // Save button
                            if (saveMessage != null) {
                                Text(saveMessage!!, fontSize = 14.sp, color = if (saveMessage!!.startsWith("✓")) ComplianceGreen else ErrorRed, modifier = Modifier.padding(horizontal = 4.dp))
                            }

                            Button(
                                onClick = {
                                    isSaving = true
                                    scope.launch {
                                        val currentAgentEmail = selectedDesign?.creatorEmail ?: user.email
                                        
                                        // 1. Subir plantillas si hay nuevas seleccionadas
                                        var finalDc3Url = dc3TemplateUrl
                                        var finalDiplomaUrl = diplomaTemplateUrl

                                        if (newDc3TemplateUri != null) {
                                            val url = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                                                SupabaseRepository.uploadTemplate(context, newDc3TemplateUri!!, currentAgentEmail, "dc3_base.pdf") { url ->
                                                    cont.resume(url) { }
                                                }
                                            }
                                            if (url != null) finalDc3Url = url
                                        }

                                        if (newDiplomaTemplateUri != null) {
                                            val isPdf = context.contentResolver.getType(newDiplomaTemplateUri!!) == "application/pdf"
                                            val fileName = if (isPdf) "diploma_base.pdf" else "diploma_base.png"
                                            val url = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                                                SupabaseRepository.uploadTemplate(context, newDiplomaTemplateUri!!, currentAgentEmail, fileName) { url ->
                                                    cont.resume(url) { }
                                                }
                                            }
                                            if (url != null) finalDiplomaUrl = url
                                        }

                                        val updated = if (selectedDesign != null) {
                                            selectedDesign?.copy(
                                                headerSlogan = headerSlogan,
                                                dc3TemplateUrl = finalDc3Url,
                                                diplomaTemplateUrl = finalDiplomaUrl,
                                                headerSloganX = headerSloganX,
                                                headerSloganY = headerSloganY,
                                                headerSloganSize = headerSloganSize,
                                                headerSloganFont = headerSloganFont,
                                                agentName = agentName,
                                                slogan = slogan,
                                                sloganX = sloganX,
                                                sloganY = sloganY,
                                                sloganSize = sloganSize,
                                                sloganFont = sloganFont,
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
                                                dipStpsSz = dipStpsSz,
                                                dipCedulaX = dipCedulaX,
                                                dipCedulaY = dipCedulaY,
                                                dipCedulaSz = dipCedulaSz,
                                                qrX = qrX,
                                                qrY = qrY,
                                                qrSz = qrSz,
                                                dipQrX = dipQrX,
                                                dipQrY = dipQrY,
                                                dipQrSz = dipQrSz
                                            )
                                        } else {
                                            AgentDesign(
                                                creatorEmail = user.email,
                                                headerSlogan = headerSlogan,
                                                dc3TemplateUrl = finalDc3Url,
                                                diplomaTemplateUrl = finalDiplomaUrl,
                                                headerSloganX = headerSloganX,
                                                headerSloganY = headerSloganY,
                                                headerSloganSize = headerSloganSize,
                                                headerSloganFont = headerSloganFont,
                                                agentName = agentName,
                                                slogan = slogan,
                                                sloganX = sloganX,
                                                sloganY = sloganY,
                                                sloganSize = sloganSize,
                                                sloganFont = sloganFont,
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
                                                dipStpsSz = dipStpsSz,
                                                dipCedulaX = dipCedulaX,
                                                dipCedulaY = dipCedulaY,
                                                dipCedulaSz = dipCedulaSz,
                                                qrX = qrX,
                                                qrY = qrY,
                                                qrSz = qrSz,
                                                dipQrX = dipQrX,
                                                dipQrY = dipQrY,
                                                dipQrSz = dipQrSz
                                            )
                                        }

                                        try {
                                            // Lógica de guardado corregida:
                                            // - Si hay diseño seleccionado: actualizar por su ID (sin importar de quién sea)
                                            // - Si no hay seleccionado: buscar existente por email, si no insertar nuevo
                                            val success = if (selectedDesign != null) {
                                                SupabaseRepository.updateDataSuspend("agent_designs", selectedDesign!!.id!!, updated!!, AgentDesign.serializer())
                                            } else {
                                                val existing = SupabaseRepository.fetchDataFilteredSuspend("agent_designs", "creator_email=eq.${user.email}&order=created_at.desc", AgentDesign.serializer()).firstOrNull()
                                                if (existing != null) {
                                                    SupabaseRepository.updateDataSuspend("agent_designs", existing.id!!, updated!!, AgentDesign.serializer())
                                                } else {
                                                    SupabaseRepository.insertDataSuspend("agent_designs", updated!!, AgentDesign.serializer())
                                                }
                                            }

                                            if (success) {
                                                saveMessage = "✓ Diseño guardado correctamente"
                                                newDc3TemplateUri = null
                                                newDiplomaTemplateUri = null
                                                // Recargar lista para reflejar cambios
                                                isLoadingDesigns = true
                                                SupabaseRepository.fetchData("agent_designs", AgentDesign.serializer()) { fetched ->
                                                    val grouped = fetched.groupBy { it.creatorEmail ?: "unknown" }
                                                        .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt ?: "" } ?: list.first() }
                                                        .values.toList()
                                                    designs.clear()
                                                    designs.addAll(grouped)
                                                    val own = designs.find { it.creatorEmail == user.email }
                                                    if (own != null) selectedDesign = own
                                                    isLoadingDesigns = false
                                                }
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
                                .padding(editorPadding),
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
                                        .clickable { diplomaLauncher.launch("*/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (newDiplomaTemplateUri != null && context.contentResolver.getType(newDiplomaTemplateUri!!) == "application/pdf") {
                                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                             Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(48.dp))
                                             Text("PDF Seleccionado", fontSize = 12.sp, color = NavyPrimary)
                                         }
                                    } else if (diplomaTemplateUrl?.endsWith(".pdf", true) == true && newDiplomaTemplateUri == null) {
                                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                             Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(48.dp))
                                             Text("PDF en la nube activo", fontSize = 12.sp, color = NavyPrimary)
                                         }
                                    } else if (diplomaTemplateBase64 != null) {
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
                                    onClick = { diplomaLauncher.launch("*/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavySurface, contentColor = NavyPrimary)
                                ) {
                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Subir nueva plantilla (PDF o Img)")
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
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Cédula Profesional", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CoordinateField("X", dipCedulaX) { dipCedulaX = it }
                                    CoordinateField("Y", dipCedulaY) { dipCedulaY = it }
                                    CoordinateField("Tam.", dipCedulaSz) { dipCedulaSz = it }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Código QR (Verificación)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CoordinateField("X", dipQrX) { dipQrX = it }
                                    CoordinateField("Y", dipQrY) { dipQrY = it }
                                    CoordinateField("Tam.", dipQrSz) { dipQrSz = it }
                                }
                            }

                            Button(
                                onClick = {
                                    isSaving = true
                                    scope.launch {
                                        val currentAgentEmail = selectedDesign?.creatorEmail ?: user.email
                                        
                                        // 1. Subir plantillas si hay nuevas seleccionadas
                                        var finalDc3Url = dc3TemplateUrl
                                        var finalDiplomaUrl = diplomaTemplateUrl

                                        if (newDc3TemplateUri != null) {
                                            val url = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                                                SupabaseRepository.uploadTemplate(context, newDc3TemplateUri!!, currentAgentEmail, "dc3_base.pdf") { url ->
                                                    cont.resume(url) { }
                                                }
                                            }
                                            if (url != null) finalDc3Url = url
                                        }

                                        if (newDiplomaTemplateUri != null) {
                                            val url = kotlinx.coroutines.suspendCancellableCoroutine<String?> { cont ->
                                                SupabaseRepository.uploadTemplate(context, newDiplomaTemplateUri!!, currentAgentEmail, "diploma_base.png") { url ->
                                                    cont.resume(url) { }
                                                }
                                            }
                                            if (url != null) finalDiplomaUrl = url
                                        }

                                        val updated = if (selectedDesign != null) {
                                            selectedDesign?.copy(
                                                headerSlogan = headerSlogan,
                                                dc3TemplateUrl = finalDc3Url,
                                                diplomaTemplateUrl = finalDiplomaUrl,
                                                headerSloganX = headerSloganX,
                                                headerSloganY = headerSloganY,
                                                headerSloganSize = headerSloganSize,
                                                headerSloganFont = headerSloganFont,
                                                agentName = agentName,
                                                slogan = slogan,
                                                sloganX = sloganX,
                                                sloganY = sloganY,
                                                sloganSize = sloganSize,
                                                sloganFont = sloganFont,
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
                                                dipStpsSz = dipStpsSz,
                                                dipCedulaX = dipCedulaX,
                                                dipCedulaY = dipCedulaY,
                                                dipCedulaSz = dipCedulaSz,
                                                qrX = qrX,
                                                qrY = qrY,
                                                qrSz = qrSz,
                                                dipQrX = dipQrX,
                                                dipQrY = dipQrY,
                                                dipQrSz = dipQrSz
                                            )
                                        } else {
                                            AgentDesign(
                                                creatorEmail = user.email,
                                                headerSlogan = headerSlogan,
                                                dc3TemplateUrl = finalDc3Url,
                                                diplomaTemplateUrl = finalDiplomaUrl,
                                                headerSloganX = headerSloganX,
                                                headerSloganY = headerSloganY,
                                                headerSloganSize = headerSloganSize,
                                                headerSloganFont = headerSloganFont,
                                                agentName = agentName,
                                                slogan = slogan,
                                                sloganX = sloganX,
                                                sloganY = sloganY,
                                                sloganSize = sloganSize,
                                                sloganFont = sloganFont,
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
                                                dipStpsSz = dipStpsSz,
                                                dipCedulaX = dipCedulaX,
                                                dipCedulaY = dipCedulaY,
                                                dipCedulaSz = dipCedulaSz,
                                                qrX = qrX,
                                                qrY = qrY,
                                                qrSz = qrSz,
                                                dipQrX = dipQrX,
                                                dipQrY = dipQrY,
                                                dipQrSz = dipQrSz
                                            )
                                        }

                                        try {
                                            // Lógica de guardado corregida:
                                            // - Si hay diseño seleccionado: actualizar por su ID (sin importar de quién sea)
                                            // - Si no hay seleccionado: buscar existente por email, si no insertar nuevo
                                            val success = if (selectedDesign != null) {
                                                SupabaseRepository.updateDataSuspend("agent_designs", selectedDesign!!.id!!, updated!!, AgentDesign.serializer())
                                            } else {
                                                val existing = SupabaseRepository.fetchDataFilteredSuspend("agent_designs", "creator_email=eq.${user.email}&order=created_at.desc", AgentDesign.serializer()).firstOrNull()
                                                if (existing != null) {
                                                    SupabaseRepository.updateDataSuspend("agent_designs", existing.id!!, updated!!, AgentDesign.serializer())
                                                } else {
                                                    SupabaseRepository.insertDataSuspend("agent_designs", updated!!, AgentDesign.serializer())
                                                }
                                            }

                                            if (success) {
                                                saveMessage = "✓ Diseño guardado correctamente"
                                                newDc3TemplateUri = null
                                                newDiplomaTemplateUri = null
                                                // Recargar lista para reflejar cambios
                                                isLoadingDesigns = true
                                                SupabaseRepository.fetchData("agent_designs", AgentDesign.serializer()) { fetched ->
                                                    val grouped = fetched.groupBy { it.creatorEmail ?: "unknown" }
                                                        .mapValues { (_, list) -> list.maxByOrNull { it.updatedAt ?: "" } ?: list.first() }
                                                        .values.toList()
                                                    designs.clear()
                                                    designs.addAll(grouped)
                                                    val own = designs.find { it.creatorEmail == user.email }
                                                    if (own != null) selectedDesign = own
                                                    isLoadingDesigns = false
                                                }
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
                                    Text("Guardar diseño de diploma", fontWeight = FontWeight.SemiBold)
                                }
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
            content(this)
        }
    }
}