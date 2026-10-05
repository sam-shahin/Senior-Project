package com.example.lettalk_01

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable fun LegalQuestionScreen() {

    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var uriHandler = LocalUriHandler.current

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {


        Text("UI Mockup")

        TextField(
            value = question,
            onValueChange = { question = it },
            label = {Text("Ask a legal question")}
        )

        Button(
            onClick = { answer = "Mock legal answer" }
        ) {
            Text("Ask Question")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(answer)

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "this is a link........",
            modifier = Modifier.clickable {
                uriHandler.openUri("https://www.csub.edu/")
            }
        )

    }

}