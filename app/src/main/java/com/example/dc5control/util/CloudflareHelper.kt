package com.example.dc5control.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

object CloudflareHelper {
    private val client = OkHttpClient()
    private const val WORKER_URL = "https://tu-worker.workers.dev" // REEMPLAZAR CON TU URL

    fun uploadPdf(file: File, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val requestBody = file.asRequestBody("application/pdf".toMediaType())
        val request = Request.Builder()
            .url("$WORKER_URL?name=${file.name}")
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                onError(e.message ?: "Error desconocido")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError("Error del servidor: ${response.code}")
                }
            }
        })
    }
}
