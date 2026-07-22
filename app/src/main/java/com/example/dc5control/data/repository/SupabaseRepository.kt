package com.example.dc5control.data.repository

import android.os.Handler
import android.os.Looper
import com.example.dc5control.data.model.*
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Repositorio que se conecta directamente a Supabase REST API.
 * Sincronizado con las credenciales y estructura de index.html.
 */
object SupabaseRepository {
    private val client = OkHttpClient()
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    private val mainHandler = Handler(Looper.getMainLooper())

    // Credenciales de la plataforma web (osgfwgedjdltrmvwycjd.supabase.co)
    private const val SUPABASE_URL = "https://osgfwgedjdltrmvwycjd.supabase.co/rest/v1"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zZ2Z3Z2VkamRsdHJtdnd5Y2pkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyNDAxMzcsImV4cCI6MjA5OTgxNjEzN30.jeV98eAfhQzkXiGj88DUOLqLPLFr_IKPrcnTaefEgj0"

    private fun getBaseRequest(table: String, query: String = ""): Request.Builder {
        val url = if (query.isNotEmpty()) "$SUPABASE_URL/$table?$query" else "$SUPABASE_URL/$table"
        return Request.Builder()
            .url(url)
            .addHeader("apikey", SUPABASE_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_KEY")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
    }

    // ─── FETCH (SELECT) ───────────────────────────────────────────
    fun <T> fetchData(table: String, serializer: kotlinx.serialization.KSerializer<T>, onResult: (List<T>) -> Unit) {
        val request = getBaseRequest(table, "select=*")
            .get()
            .build()

        android.util.Log.d("Supabase", "Fetching from $table...")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { 
                android.util.Log.e("Supabase", "Connection failed for $table: ${e.message}")
                mainHandler.post { onResult(emptyList()) }
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bodyString = it.body?.string() ?: "[]"
                    if (it.isSuccessful) {
                        try {
                            android.util.Log.d("Supabase", "Success! Response ($table): $bodyString")
                            val jsonElement = json.parseToJsonElement(bodyString)
                            val items = if (jsonElement is JsonArray) {
                                jsonElement.map { item -> json.decodeFromJsonElement(serializer, item) }
                            } else {
                                listOf(json.decodeFromJsonElement(serializer, jsonElement))
                            }
                            mainHandler.post { onResult(items) }
                        } catch (e: Exception) {
                            android.util.Log.e("Supabase", "Parse error in $table: ${e.message}. Body: $bodyString")
                            mainHandler.post { onResult(emptyList()) }
                        }
                    } else {
                        android.util.Log.e("Supabase", "Server error ${it.code} in $table: $bodyString")
                        mainHandler.post { onResult(emptyList()) }
                    }
                }
            }
        })
    }

    // ─── INSERT (single) ───────────────────────────────────────────
    fun <T> insertData(table: String, item: T, serializer: kotlinx.serialization.KSerializer<T>, onResult: (Boolean) -> Unit) {
        val bodyString = json.encodeToString(serializer, item)
        val request = getBaseRequest(table)
            .addHeader("Prefer", "return=representation")
            .post(bodyString.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { 
                android.util.Log.e("Supabase", "Insert failed: ${e.message}")
                mainHandler.post { onResult(false) } 
            }
            override fun onResponse(call: Call, response: Response) {
                response.use { 
                    val success = it.isSuccessful
                    if (!success) android.util.Log.e("Supabase", "Insert error ${it.code}: ${it.body?.string()}")
                    mainHandler.post { onResult(success) }
                }
            }
        })
    }

    suspend fun <T> insertDataSuspend(table: String, item: T, serializer: kotlinx.serialization.KSerializer<T>): Boolean = 
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val bodyString = json.encodeToString(serializer, item)
            val request = getBaseRequest(table)
                .addHeader("Prefer", "return=representation")
                .post(bodyString.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                client.newCall(request).execute().use { it.isSuccessful }
            } catch (e: Exception) {
                false
            }
        }

    // ─── INSERT (batch workers) ────────────────────────────────────
    fun insertWorkers(workers: List<Employee>, onResult: (Boolean) -> Unit) {
        val bodyString = json.encodeToString(kotlinx.serialization.builtins.ListSerializer(Employee.serializer()), workers)
        val request = getBaseRequest("workers")
            .addHeader("Prefer", "return=representation")
            .post(bodyString.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { mainHandler.post { onResult(false) } }
            override fun onResponse(call: Call, response: Response) {
                response.use { mainHandler.post { onResult(it.isSuccessful) } }
            }
        })
    }

    // ─── DELETE ────────────────────────────────────────────────────
    fun deleteData(table: String, id: String, onResult: (Boolean) -> Unit) {
        val request = getBaseRequest(table)
            .url("$SUPABASE_URL/$table?id=eq.$id")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { mainHandler.post { onResult(false) } }
            override fun onResponse(call: Call, response: Response) {
                response.use { mainHandler.post { onResult(it.isSuccessful) } }
            }
        })
    }
}
