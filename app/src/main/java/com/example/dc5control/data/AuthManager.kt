package com.example.dc5control.data

import com.example.dc5control.data.model.User
import com.example.dc5control.data.repository.SupabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.util.Log
import kotlin.text.Charsets
import java.security.MessageDigest

/**
 * Gestor de autenticación seguro.
 * Valida credenciales contra Supabase, sincronizado con la plataforma web.
 *
 * Ambas plataformas (web y Android) usan la misma lista de usuarios
 * autorizados. Las contraseñas se almacenan como hashes SHA-256.
 */
object AuthManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    // Lista de usuarios predefinidos (idéntica a la versión web index.html)
    private val hardcodedUsers = listOf(
        AuthUser(
            name = "Dario Robles",
            email = "d4r005@gmail.com",
            role = "ADMIN",
            passHash = "dd080657906b80be4ea5f3b67af9a02ccf2bc6d9a004d10c1e9bfd42e0cc7754"
        ),
        AuthUser(
            name = "Cynthia Garza Lugo",
            email = "lugga.advisors@gmail.com",
            role = "USER",
            passHash = "c625194773dbb816d11f159af7702363e7ccc64d213ea54d17c7f31aeab9921f"
        )
    )

    /**
     * Genera el hash SHA-256 de una cadena.
     */
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Valida credenciales.
     * Retorna el User si las credenciales son válidas, null en caso contrario.
     */
    suspend fun validateLogin(email: String, password: String): User? {
        val passHash = sha256(password)
        val user = getUserByEmail(email)
        return if (user != null && user.passHash == passHash) {
            User(
                name = user.name,
                email = user.email,
                role = user.role,
                password = ""
            )
        } else null
    }

    /**
     * Obtiene un usuario por email (Primero local, luego Supabase).
     */
    @OptIn(kotlinx.serialization.InternalSerializationApi::class)
    private suspend fun getUserByEmail(email: String): AuthUser? {
        // 1. Buscar en usuarios hardcoded
        val localUser = hardcodedUsers.find { it.email.lowercase() == email.lowercase() }
        if (localUser != null) return localUser

        // 2. Si no está, buscar en Supabase
        return try {
            val users = withContext(Dispatchers.IO) {
                SupabaseRepository.fetchDataFilteredSuspend(
                    "users",
                    "email=eq.$email",
                    AuthUser.serializer()
                )
            }
            users.firstOrNull()
        } catch (e: Exception) {
            Log.e("AuthManager", "Error fetching user by email: ${e.message}")
            null
        }
    }

    /**
     * Verifica si un email corresponde a un usuario autorizado.
     */
    suspend fun isAuthorizedEmail(email: String): Boolean {
        return getUserByEmail(email) != null
    }

    /**
     * Obtiene el rol de un usuario por email.
     */
    suspend fun getRoleByEmail(email: String): String? {
        val user = getUserByEmail(email)
        return user?.role
    }

    // Data class to represent a user
    @Serializable
    private data class AuthUser(
        val id: String? = null,
        val name: String,
        val email: String,
        val role: String,
        val passHash: String,
        val active: Boolean = true
    )
}
