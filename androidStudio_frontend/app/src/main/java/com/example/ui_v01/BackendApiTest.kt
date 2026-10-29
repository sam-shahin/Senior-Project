package com.example.ui_v01

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun getBackendTestResponse(question: String): MockResponse =
    withContext(Dispatchers.IO) {

        val connection = URL(
            "http://127.0.0.1:3000/api/test-answer"
        ).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("Content-Type", "application/json")

            val request = JSONObject()
                .put("question", question)
                .toString()

            connection.outputStream.use {
                it.write(request.toByteArray(Charsets.UTF_8))
            }

            if (connection.responseCode != 200) {
                throw Exception("HTTP ${connection.responseCode}")
            }

            val response = JSONObject(
                connection.inputStream.bufferedReader()
                    .use { it.readText() }
            )

            MockResponse(
                type = response.getString("type"),
                message = response.getString("message"),
                sourceUrl = response.optString("sourceUrl")
                    .takeIf { it.isNotEmpty() }
            )
        } finally {
            connection.disconnect()
        }
    }