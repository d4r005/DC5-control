package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.AuthManager
import com.example.dc5control.data.model.User
import com.example.dc5control.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    user: User,
    isExpanded: Boolean,
    onBack: () -> Unit
) {
    // Authorized users — synchronized with the web platform
    val users = remember {
        listOf(
            User("Dario Robles", "d4r005@gmail.com", "ADMIN"),
            User("Cynthia Garza Lugo", "lugga.advisors@gmail.com", "USER")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Usuarios", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Gestión de accesos al sistema", fontSize = 12.sp, color = Gray400)
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(if (isExpanded) 48.dp else 16.dp)
        ) {
            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = NavySurface,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Solo el administrador puede gestionar usuarios. La edición está protegida.",
                        fontSize = 13.sp,
                        color = NavyPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Users list
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 2.dp
            ) {
                LazyColumn {
                    items(users) { u ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (u.role == "ADMIN") NavyPrimary else NavySurface,
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    u.name.take(2).uppercase(),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (u.role == "ADMIN") Color.White else NavyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))

                            // Name & email
                            Column(modifier = Modifier.weight(1f)) {
                                Text(u.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                                Text(u.email, fontSize = 13.sp, color = Gray500)
                            }

                            // Role badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (u.role == "ADMIN") NavyPrimary else Gray100
                            ) {
                                Text(
                                    u.role,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (u.role == "ADMIN") Color.White else Gray600,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                        if (users.indexOf(u) < users.size - 1) {
                            HorizontalDivider(color = Gray100, modifier = Modifier.padding(horizontal = 20.dp))
                        }
                    }
                }
            }
        }
    }
}
