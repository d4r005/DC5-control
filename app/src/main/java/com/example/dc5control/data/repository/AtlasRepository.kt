package com.example.dc5control.data.repository

import com.example.dc5control.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object AtlasRepository {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    
    // CONFIGURACIÓN DE MONGODB ATLAS DATA API
    private const val API_KEY = "al-b14XHfG6V17yJMOy1lmUWm9vCbuLZG1-Ot7Mp0YAMhH"
    // REEMPLAZA 'tu-app-id' con el ID que aparece en la sección Data API de MongoDB Atlas
    private const val BASE_URL = "https://data.mongodb-api.com/app/data-jshze/endpoint/data/v1/action"
    private const val CLUSTER = "acecontrol"
    private const val DATABASE = "DC3_Database"

    private fun createRequestBody(collection: String, additionalParams: Map<String, JsonElement> = emptyMap()): RequestBody {
        val baseParams = mutableMapOf(
            "dataSource" to JsonPrimitive(CLUSTER),
            "database" to JsonPrimitive(DATABASE),
            "collection" to JsonPrimitive(collection)
        )
        baseParams.putAll(additionalParams)
        return Json.encodeToString(baseParams).toRequestBody("application/json".toMediaType())
    }

    fun <T> fetchData(collection: String, serializer: kotlinx.serialization.KSerializer<T>, onResult: (List<T>) -> Unit) {
        val requestBody = createRequestBody(collection, mapOf("filter" to JsonObject(emptyMap())))
        val request = Request.Builder()
            .url("$BASE_URL/find")
            .addHeader("api-key", API_KEY)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(emptyList()) }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val root = json.parseToJsonElement(bodyString).jsonObject
                    val documents = root["documents"]?.jsonArray ?: jsonArrayOf()
                    val items = documents.map { json.decodeFromJsonElement(serializer, it) }
                    onResult(items)
                } else {
                    onResult(emptyList())
                }
            }
        })
    }

    fun <T> insertData(collection: String, item: T, serializer: kotlinx.serialization.KSerializer<T>, onResult: (Boolean) -> Unit) {
        val requestBody = createRequestBody(collection, mapOf("document" to json.encodeToJsonElement(serializer, item)))
        val request = Request.Builder()
            .url("$BASE_URL/insertOne")
            .addHeader("api-key", API_KEY)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }

    fun insertWorkers(workers: List<Worker>, onResult: (Boolean) -> Unit) {
        val workersArray = JsonArray(workers.map { json.encodeToJsonElement(Worker.serializer(), it) })
        val requestBody = createRequestBody("workers", mapOf("documents" to workersArray))
        val request = Request.Builder()
            .url("$BASE_URL/insertMany")
            .addHeader("api-key", API_KEY)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { onResult(false) }
            override fun onResponse(call: Call, response: Response) { onResult(response.isSuccessful) }
        })
    }
}
