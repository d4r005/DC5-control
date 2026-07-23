package com.example.dc5control.data

import com.example.dc5control.data.model.User
import java.security.MessageDigest

/**
 * Gestor de autenticación seguro.
 * Valida credenciales contra los mismos usuarios de la plataforma web,
 * usando hashes SHA-256 en lugar de contraseñas en texto plano.
 *
 * Ambas plataformas (web y Android) usan la misma lista de usuarios
 * autorizados. Las contraseñas se almacenan como hashes SHA-256.
 */
object AuthManager {

    // Usuarios autorizados — sincronizados con la plataforma web (index.html)
    // Las contraseñas se almacenan como hashes SHA-256, nunca en texto plano.
    private data class AuthUser(
        val name: String,
        val email: String,
        val role: String,
        val passHash: String
    )

    private val authorizedUsers = listOf(
        AuthUser(
            name = "Dario Robles",
            email = "d4r005@gmail.com",
            role = "ADMIN",
            passHash = "dd080657906b80be4ea5f3b67af9a02ccf2bc6d9a004d10c1e9bfd42e0cc7754" // Branco2025
        ),
        AuthUser(
            name = "Cynthia Garza Lugo",
            email = "lugga.advisors@gmail.com",
            role = "USER",
            passHash = "c625194773dbb816d11f159af7702363e7ccc64d213ea54d17c7f31aeab9921f" // Cynthia123
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
     * Valida credenciales contra la lista de usuarios autorizados.
     * Retorna el User si las credenciales son válidas, null en caso contrario.
     */
    fun validateLogin(email: String, password: String): User? {
        val passHash = sha256(password)
        val authUser = authorizedUsers.find {
            it.email.equals(email, ignoreCase = true) && it.passHash == passHash
        } ?: return null

        return User(
            name = authUser.name,
            email = authUser.email,
            role = authUser.role,
            password = "" // Nunca almacenamos la contraseña en el objeto User
        )
    }

    /**
     * Verifica si un email corresponde a un usuario autorizado.
     */
    fun isAuthorizedEmail(email: String): Boolean {
        return authorizedUsers.any { it.email.equals(email, ignoreCase = true) }
    }

    /**
     * Obtiene el rol de un usuario por email.
     */
    fun getRoleByEmail(email: String): String? {
        return authorizedUsers.find { it.email.equals(email, ignoreCase = true) }?.role
    }
}
