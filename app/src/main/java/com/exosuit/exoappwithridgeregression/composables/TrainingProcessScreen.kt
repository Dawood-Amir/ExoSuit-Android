package com.exosuit.exoappwithridgeregression.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.exosuit.exoappwithridgeregression.EmgViewModel
import com.exosuit.exoappwithridgeregression.NavGraph
import kotlinx.coroutines.delay


@Composable
fun TrainingProgressScreen(viewModel: EmgViewModel, navController: NavController) {
    val trainingProgress by viewModel.trainingProgress.collectAsState()
    val trainingStatus by viewModel.trainingStatus.collectAsState()

    val modelReady by viewModel.modelReady.collectAsState()

    LaunchedEffect(modelReady) {
        if (modelReady) {
            navController.navigate(NavGraph.Screen.EmgHome.route) {
                popUpTo(NavGraph.Screen.GuidedRecording.route) { inclusive = true }
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Training Model",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Fixed CircularProgressIndicator - Material3 version
        CircularProgressIndicator(
            progress = trainingProgress / 100f,
            modifier = Modifier.size(100.dp),
            strokeWidth = 8.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("$trainingProgress%")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            trainingStatus,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (trainingProgress == 100) {
            Button(
                onClick = { navController.popBackStack() }
            ) {
                Text("Done")
            }
        } else if (trainingStatus.contains("Error", ignoreCase = true) ||
            trainingStatus.contains(
                "timeout",
                ignoreCase = true
            ) || trainingStatus.contains("Failed", ignoreCase = true) ||
            trainingStatus.startsWith("SERVER_ERROR", ignoreCase = true)
        ) {
            Button(
                onClick = {  navController.navigate(NavGraph.Screen.EmgHome.route) {
                    popUpTo(NavGraph.Screen.GuidedRecording.route) { inclusive = true }
                } }
            ) {
                Text("Back to Home")
            }
        }
    }

    // Automatically go back when training is complete
    if (trainingProgress == 100) {
        LaunchedEffect(Unit) {
            delay(2000) // Show success message for 2 seconds
            navController.popBackStack()
        }
    }
}
