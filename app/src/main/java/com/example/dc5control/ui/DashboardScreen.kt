package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.Screen
import com.example.dc5control.data.model.*
import com.example.dc5control.data.repository.SupabaseRepository
import com.example.dc5control.ui.theme.*

@Composable
fun DashboardScreen(
    user: User,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var workersCount by remember { mutableStateOf("–") }
    var dc3Count by remember { mutableStateOf("–") }
    var companiesCount by remember { mutableStateOf("–") }

    LaunchedEffect(Unit) {
        SupabaseRepository.fetchData("workers", Employee.serializer()) { fetched ->
            val count = if (user.role == "ADMIN") fetched.size else fetched.count { it.creatorEmail == user.email }
            workersCount = count.toString()
        }
        SupabaseRepository.fetchData("dc3_records", DC3Record.serializer()) { dc3Count = it.size.toString() }
        SupabaseRepository.fetchData("companies", Company.serializer()) { companiesCount = it.size.toString() }
    }

    val isExpanded = LocalConfiguration.current.screenWidthDp >= 600
    val mainPadding = if (isExpanded) 24.dp else 16.dp
    val spacing = if (isExpanded) 24.dp else 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(mainPadding),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Custom Topbar layout inside scrollable content
        DashboardHeader(
            user = user,
            isExpanded = isExpanded,
            onLogout = onLogout
        )

        // Stat Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isExpanded) 24.dp else 12.dp)
        ) {
            StatCard(
                label = "PERSONAL REGISTRADO",
                value = workersCount,
                subtitle = "Trabajadores",
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Workers) }
            )
            StatCard(
                label = "CONSTANCIAS DC-3",
                value = dc3Count,
                subtitle = "Generadas",
                valueColor = NavyPrimary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.DC3History) }
            )
            StatCard(
                label = "EMPRESAS",
                value = companiesCount,
                subtitle = "Registradas",
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Screen.Companies) }
            )
        }

        // 'Cómo funciona' Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Gray200, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Cómo funciona",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Genera constancias DC-3 válidas ante la STPS en 3 pasos",
                    fontSize = 14.sp,
                    color = Gray500
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (isExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ComoFuncionaStep(
                            number = "1",
                            title = "Registra el personal",
                            description = "Carga manual o importa desde Excel con nombre, CURP y puesto.",
                            modifier = Modifier.weight(1f)
                        )
                        ComoFuncionaStep(
                            number = "2",
                            title = "Selecciona cursos",
                            description = "Define los cursos de capacitación con duración y área temática.",
                            modifier = Modifier.weight(1f)
                        )
                        ComoFuncionaStep(
                            number = "3",
                            title = "Genera DC-3",
                            description = "Descarga constancias PDF oficiales firmadas y listas para STPS.",
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ComoFuncionaStep(
                            number = "1",
                            title = "Registra el personal",
                            description = "Carga manual o importa desde Excel con nombre, CURP y puesto."
                        )
                        ComoFuncionaStep(
                            number = "2",
                            title = "Selecciona cursos",
                            description = "Define los cursos de capacitación con duración y área temática."
                        )
                        ComoFuncionaStep(
                            number = "3",
                            title = "Genera DC-3",
                            description = "Descarga constancias PDF oficiales firmadas y listas para STPS."
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardHeader(
    user: User,
    isExpanded: Boolean,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = if (isExpanded) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Gray900
            )
            Text(
                text = "Bienvenido al sistema, ${user.name}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500
            )
        }
        IconButton(onClick = onLogout) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Cerrar Sesión",
                tint = Gray500
            )
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    subtitle: String,
    valueColor: Color = Gray900,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(1.dp, Gray200, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Gray400,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Gray500
            )
        }
    }
}

@Composable
fun ComoFuncionaStep(
    number: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Blue50, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(NavyPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = SurfaceWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            fontSize = 12.sp,
            color = Gray500,
            lineHeight = 16.sp
        )
    }
}
