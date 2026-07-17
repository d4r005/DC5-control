package com.example.dc5control.data.repository

import com.example.dc5control.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Repositorio que utiliza el Cloudflare Worker como Proxy para verificar el flujo de datos.
 */
object SupabaseRepository {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // URL de Cloudflare Pages (ace-control.pages.dev) como base para la API
    private const val BASE_URL = "https://ace-control.pages.dev/api"

    // ─── FETCH (SELECT) ───────────────────────────────────────────
    fun <T> fetchData(table: String, serializer: kotlinx.serialization.KSerializer<T>, onResult: (List<T>) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/$table")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(emptyList()) }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    try {
                        val bodyString = response.body?.string() ?: "{}"
                        val root = json.parseToJsonElement(bodyString).jsonObject
                        val arr = root["documents"]?.jsonArray ?: jsonArrayOf()
                        val items = arr.map { json.decodeFromJsonElement(serializer, it) }
                        onResult(items)
                    } catch (e: Exception) {
                        onResult(emptyList())
                    }
                } else {
                    onResult(emptyList())
                }
            }
        })
    }

    // ─── INSERT (single) ───────────────────────────────────────────
    fun <T> insertData(table: String, item: T, serializer: kotlinx.serialization.KSerializer<T>, onResult: (Boolean) -> Unit) {
        val doc = json.encodeToJsonElement(serializer, item)
        val body = JsonObject(mapOf("document" to doc)).toString()
        
        val request = Request.Builder()
            .url("$BASE_URL/$table")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }

    // ─── INSERT (batch workers) ────────────────────────────────────
    fun insertWorkers(workers: List<Worker>, onResult: (Boolean) -> Unit) {
        val arr = workers.map { json.encodeToJsonElement(Worker.serializer(), it) }
        val body = JsonObject(mapOf("documents" to JsonArray(arr))).toString()
        
        val request = Request.Builder()
            .url("$BASE_URL/workers")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }

    // ─── DELETE ────────────────────────────────────────────────────
    fun deleteData(table: String, id: String, onResult: (Boolean) -> Unit) {
        val body = JsonObject(mapOf("id" to JsonPrimitive(id))).toString()
        val request = Request.Builder()
            .url("$BASE_URL/$table")
            .delete(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }
}
