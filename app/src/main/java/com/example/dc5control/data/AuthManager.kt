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

    /**
     * Genera el hash SHA-256 de una cadena.
     */
    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Valida credenciales contra Supabase.
     * Retorna el User si las credenciales son válidas, null en caso contrario.
     */
    suspend fun validateLogin(email: String, password: String): User? {
        val passHash = sha256(password)
        val user = getUserByEmail(email)
        return if (user != null && user.passHash == passHash) {
            // Nunca almacenamos la contraseña en el objeto User
            User(
                name = user.name,
                email = user.email,
                role = user.role,
                password = ""
            )
        } else null
    }

    /**
     * Obtiene un usuario por email desde Supabase.
     */
    private suspend fun getUserByEmail(email: String): AuthUser? {
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

    // Data class to represent a user from Supabase
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