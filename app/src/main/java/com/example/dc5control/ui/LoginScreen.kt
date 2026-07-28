package com.example.dc5control.ui

import android.content.Context
import android.content.SharedPreferences
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.dc5control.R
import com.example.dc5control.data.AuthManager
import com.example.dc5control.data.model.User
import com.example.dc5control.util.SecurePreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val BRUTE_FORCE_PREFS = "brute_force_prefs"
private const val KEY_ATTEMPT_COUNT = "attempt_count"
private const val KEY_LAST_ATTEMPT_TIME = "last_attempt_time"
private const val MAX_ATTEMPTS = 3
private const val LOCKOUT_DURATION_MINUTES = 15

@Composable
fun LoginScreen(onLoginSuccess: (User, Boolean) -> Unit) {
    val context = LocalContext.current
    // Pre-fill email if "recordar usuario" was previously checked
    var email by rememberSaveable { mutableStateOf("") }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Credenciales incorrectas. Intenta de nuevo.") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Brute force protection state
    val prefs = context.getSharedPreferences(BRUTE_FORCE_PREFS, Context.MODE_PRIVATE)
    val attemptCount = prefs.getInt(KEY_ATTEMPT_COUNT, 0)
    val lastAttemptTime = prefs.getLong(KEY_LAST_ATTEMPT_TIME, 0)
    val isLocked = attemptCount >= MAX_ATTEMPTS &&
        (System.currentTimeMillis() - lastAttemptTime < TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES))
    val timeUntilUnlock = if (isLocked) {
        val timePassed = System.currentTimeMillis() - lastAttemptTime
        val timeLeft = TimeUnit.MINUTES.toMillis(LOCKOUT_DURATION_MINUTES) - timePassed
        Math.max(0, timeLeft / (60 * 1000)) // minutes left
    } else 0

    // Initialize email from secure storage
    init {
        try {
            email = SecurePreferences.getEmail(context) ?: ""
            rememberMe = email.isNotEmpty()
        } catch (e: Exception) {
            // If there's an error reading from secure storage, fall back to empty
            email = ""
            rememberMe = false
        }
    }

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
            // Logo & Brand — logo de la app
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NavyPrimary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo ACE-Control",
                    modifier = Modifier.size(64.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("ACE-Control", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Gray900)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Sistema de Gestión DC-3 · STPS v1.0.5", fontSize = 14.sp, color = Gray400)

            Spacer(modifier = Modifier.height(32.dp))

            // Login Card
            Surface(
                modifier = Modifier.width(420.dp),
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
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Gray400, modifier = Modifier.size(18.dp))
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

                    // Password
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                        Text("Contraseña", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it.trimEnd(); showError = false },
                            placeholder = { Text("••••••••", color = Gray400) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Gray400, modifier = Modifier.size(18.dp))
                            },
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

                    // Show lockout message if account is locked
                    if (isLocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(ErrorRed.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Cuenta bloqueada debido a demasiados intentos fallidos. Inténtalo de nuevo en $timeUntilUnlock minutos.",
                                fontSize = 14.sp,
                                color = ErrorRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (showError && !isLocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(ErrorRed.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage, fontSize = 14.sp, color = ErrorRed, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Submit button
                    Button(
                        onClick = {
                            if (isLocked) {
                                // Show error if account is locked
                                errorMessage = "Cuenta bloqueada debido a demasiados intentos fallidos. Inténtalo de nuevo en $timeUntilUnlock minutos."
                                showError = true
                                return@Button
                            }

                            if (email.isNotBlank() && password.isNotBlank()) {
                                isLoading = true
                                scope.launch {
                                    delay(500) // Brief delay for UX feedback
                                    val user = AuthManager.validateLogin(email.trim(), password.trim())
                                    isLoading = false
                                    if (user != null) {
                                        showError = false
                                        // Reset failed attempts on successful login
                                        prefs.edit().putInt(KEY_ATTEMPT_COUNT, 0).apply()
                                        // Save email if remember me is checked
                                        if (rememberMe) {
                                            try {
                                                SecurePreferences.saveEmail(context, email.trim())
                                            } catch (e: Exception) {
                                                // Handle encryption error - fallback to regular preferences for now
                                                val prefs = context.getSharedPreferences("ace_session", Context.MODE_PRIVATE)
                                                prefs.edit().putString("saved_email", email.trim()).apply()
                                            }
                                        } else {
                                            try {
                                                SecurePreferences.clearEmail(context)
                                            } catch (e: Exception) {
                                                // Handle decryption error - fallback to regular preferences for now
                                                val prefs = context.getSharedPreferences("ace_session", Context.MODE_PRIVATE)
                                                prefs.edit().remove("saved_email").apply()
                                            }
                                        }
                                        onLoginSuccess(user, rememberMe)
                                    } else {
                                        // Increment failed attempts
                                        val newAttemptCount = attemptCount + 1
                                        prefs.edit()
                                            .putInt(KEY_ATTEMPT_COUNT, newAttemptCount)
                                            .putLong(KEY_LAST_ATTEMPT_TIME, System.currentTimeMillis())
                                            .apply()

                                        val updatedAttemptCount = attemptCount + 1
                                        errorMessage = if (updatedAttemptCount >= MAX_ATTEMPTS) {
                                            "Demasiados intentos. Cuenta bloqueada por $LOCKOUT_DURATION_MINUTES minutos."
                                        } else {
                                            "Credenciales incorrectas. Intenta de nuevo."
                                        }
                                        showError = true
                                    }
                                }
                            } else {
                                errorMessage = "Completa todos los campos."
                                showError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        enabled = !isLoading && !isLocked
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
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(ComplianceGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = ComplianceGreen, modifier = Modifier.size(12.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cumple con la STPS · Art. 153-A LFT", fontSize = 12.sp, color = Gray400)
                    }
                }
            }
        }
    }
}