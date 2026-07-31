package com.example.dc5control.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Utility class for securely storing SharedPreferences using EncryptedSharedPreferences
 */
object SecurePreferences {
    private const val PREFS_NAME = "secure_ace_session"
    private const val KEY_EMAIL = "secure_email"

    /**
     * Gets an instance of EncryptedSharedPreferences
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    private fun getEncryptedPreferences(context: Context): SharedPreferences {
        val mainKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            PREFS_NAME,
            mainKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Saves the email securely
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun saveEmail(context: Context, email: String?) {
        val prefs = getEncryptedPreferences(context)
        prefs.edit()
            .putString(KEY_EMAIL, email ?: "")
            .apply()
    }

    /**
     * Retrieves the email securely
     * Returns null if no email is stored
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getEmail(context: Context): String? {
        val prefs = getEncryptedPreferences(context)
        val email = prefs.getString(KEY_EMAIL, "")
        return if (email.isNullOrEmpty()) null else email
    }

    /**
     * Clears the stored email
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun clearEmail(context: Context) {
        val prefs = getEncryptedPreferences(context)
        prefs.edit()
            .remove(KEY_EMAIL)
            .apply()
    }
}