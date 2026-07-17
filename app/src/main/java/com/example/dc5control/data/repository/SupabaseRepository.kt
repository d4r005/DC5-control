package com.example.dc5control.data.repository

import com.example.dc5control.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Reemplaza AtlasRepository — ahora usa Supabase REST API.
 * Tablas: workers, companies, courses, agents, dc3_records
 */
object SupabaseRepository {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private const val SUPABASE_URL = "https://osgfwgedjdltrmvwycjd.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9zZ2Z3Z2VkamRsdHJtdnd5Y2pkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODQyNDAxMzcsImV4cCI6MjA5OTgxNjEzN30.jeV98eAfhQzkXiGj88DUOLqLPLFr_IKPrcnTaefEgj0"

    private fun headers(): Headers {
        return Headers.Builder()
            .add("apikey", SUPABASE_KEY)
            .add("Authorization", "Bearer $SUPABASE_KEY")
            .add("Content-Type", "application/json")
            .build()
    }

    // ─── FETCH (SELECT) ───────────────────────────────────────────
    fun <T> fetchData(table: String, serializer: kotlinx.serialization.KSerializer<T>, onResult: (List<T>) -> Unit) {
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/${table}?select=*")
            .headers(headers())
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(emptyList()) }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: "[]"
                    val arr = json.parseToJsonElement(bodyString).jsonArray
                    val items = arr.map { json.decodeFromJsonElement(serializer, it) }
                    onResult(items)
                } else {
                    onResult(emptyList())
                }
            }
        })
    }

    // ─── INSERT (single) ───────────────────────────────────────────
    fun <T> insertData(table: String, item: T, serializer: kotlinx.serialization.KSerializer<T>, onResult: (Boolean) -> Unit) {
        val jsonStr = json.encodeToString(serializer, item)
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/${table}")
            .headers(headers())
            .header("Prefer", "return=representation")
            .post(jsonStr.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }

    // ─── INSERT (batch workers) ────────────────────────────────────
    fun insertWorkers(workers: List<Worker>, onResult: (Boolean) -> Unit) {
        val arr = workers.map { json.encodeToJsonElement(Worker.serializer(), it) }
        val jsonStr = json.encodeToString(JsonArray(arr))
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/workers")
            .headers(headers())
            .header("Prefer", "return=representation")
            .post(jsonStr.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }

    // ─── INSERT DC-3 record ────────────────────────────────────────
    fun insertDC3Record(record: DC3Record, onResult: (Boolean) -> Unit) {
        val jsonStr = json.encodeToString(DC3Record.serializer(), record)
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/dc3_records")
            .headers(headers())
            .header("Prefer", "return=representation")
            .post(jsonStr.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }

    // ─── DELETE ────────────────────────────────────────────────────
    fun deleteData(table: String, id: String, onResult: (Boolean) -> Unit) {
        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/${table}?id=eq.${id}")
            .headers(headers())
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }
}
