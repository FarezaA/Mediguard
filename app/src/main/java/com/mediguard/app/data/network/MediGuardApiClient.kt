package com.mediguard.app.data.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object MediGuardApiClient {

    // Default to the current Wi-Fi host IP
    var baseUrl: String = "http://192.168.1.28:8080"

    suspend fun checkServerHealth(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/status")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            conn.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchData(): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/data")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.requestMethod = "GET"

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                JSONObject(response)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendAction(action: String, payload: JSONObject, userId: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/action")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val body = JSONObject().apply {
                put("action", action)
                put("payload", payload)
                put("userId", userId ?: "")
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            conn.responseCode == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun uploadPhoto(imageBytes: ByteArray, filename: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/upload")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val body = JSONObject().apply {
                put("filename", filename)
                put("imageBase64", base64Str)
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.use { it.readText() }
                val json = JSONObject(response)
                json.optString("url", null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun loadBitmapFromUrl(urlString: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // If the URL is relative like /uploads/..., prepend baseUrl
            val fullUrl = if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                urlString
            } else {
                "$baseUrl${if (urlString.startsWith("/")) "" else "/"}$urlString"
            }

            val url = URL(fullUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.doInput = true
            conn.connect()

            if (conn.responseCode == 200) {
                conn.inputStream.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
