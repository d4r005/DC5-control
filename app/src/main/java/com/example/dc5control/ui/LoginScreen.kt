package com.example.dc5control.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dc5control.data.model.User
import com.example.dc5control.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope

@Composable
fun LoginScreen(onLoginSuccess: (User) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(LoginGradientStart, LoginGradientMid, LoginGradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo & Brand
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(NavyPrimary, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("ACE-Control", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gray900)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Sistema de Gestión DC-3 · STPS v1.0.5", fontSize = 14.sp, color = Gray400)

            Spacer(modifier = Modifier.height(32.dp))

            // Login Card
            Surface(
                modifier = Modifier.width(IntrinsicSize.Min).width(420.dp),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceWhite,
                shadowElevation = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Iniciar sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Gray900)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Ingresa tus credenciales para continuar", fontSize = 14.sp, color = Gray400)

                    Spacer(modifier = Modifier.height(28.dp))

                    // Email
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Text("Correo electrónico", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; showError = false },
                            placeholder = { Text("usuario@correo.com", color = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Gray200,
                                focusedBorderColor = NavyPrimary,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedContainerColor = SurfaceWhite
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Text("Contraseña", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; showError = false },
                            placeholder = { Text("••••••••", color = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Mostrar/ocultar contraseña",
                                        tint = Gray400,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Gray200,
                                focusedBorderColor = NavyPrimary,
                                unfocusedContainerColor = SurfaceWhite,
                                focusedContainerColor = SurfaceWhite
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Remember me
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(checkedColor = NavyPrimary, uncheckedColor = Gray400)
                        )
                        Text("Recordar usuario", fontSize = 14.sp, color = Gray600, modifier = Modifier.clickable { rememberMe = !rememberMe })
                    }

                    if (showError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Credenciales incorrectas. Intenta de nuevo.", fontSize = 14.sp, color = ErrorRed, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit button
                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.isNotBlank()) {
                                isLoading = true
                                val role = if (email.contains("admin", ignoreCase = true)) "ADMIN" else "USER"
                                val name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                                val user = User(name = name, email = email, role = role, password = password)
                                // Simulate brief auth
                                kotlinx.coroutines.MainScope().launch {
                                    delay(300)
                                    isLoading = false
                                    onLoginSuccess(user)
                                }
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Entrar al sistema", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Compliance footer
                    HorizontalDivider(color = Gray100)
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cumple con la STPS · Art. 153-A LFT", fontSize = 12.sp, color = Gray400)
                    }
                }
            }
        }
    }
}
