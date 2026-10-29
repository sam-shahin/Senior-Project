package com.example.ui_v01

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


@Composable
fun BackendTest() {
    var result by remember { mutableStateOf("Not connected") }
    val scope = rememberCoroutineScope()

    Column {
        Button(onClick = {
            result = "Sending request..."

            scope.launch {
                result = withContext(Dispatchers.IO) {
                    try {
                        val connection = URL("http://127.0.0.1:3000/api/test")
                            .openConnection() as HttpURLConnection

                        try {
                            connection.connectTimeout = 3000
                            connection.readTimeout = 3000
                            connection.inputStream.bufferedReader().use {
                                it.readText()
                            }
                        } finally {
                            connection.disconnect()
                        }
                    } catch (e: Exception) {
                        "Connection failed: ${e.message}"
                    }
                }
            }
        }) {
            Text("Test Backend")
        }

        Text(result)
    }
}