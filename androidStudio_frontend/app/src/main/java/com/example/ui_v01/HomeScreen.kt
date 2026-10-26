package com.example.ui_v01

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController) {
    Column {
        Text("Let's Talk - Home")

        Button (onClick = {
            navController.navigate("legal")
        }) {

            Text("Legal Assistant")
        }
    }
}