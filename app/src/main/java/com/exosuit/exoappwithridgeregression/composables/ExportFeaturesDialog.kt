package com.exosuit.exoappwithridgeregression.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exosuit.exoappwithridgeregression.EmgViewModel






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportFeaturesDialog(
    viewModel: EmgViewModel,
    onDismiss: () -> Unit
) {
    var filename by remember { mutableStateOf("features_${System.currentTimeMillis()}.csv") }
    var gestureDescription by remember { mutableStateOf("") }

    // Capture the current state when dialog is shown
    LaunchedEffect(Unit) {
        viewModel.prepareForExport()
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resumeModelAfterExport()
            onDismiss()
        },
        title = { Text("Export Features") },
        text = {
            Column {
                TextField(
                    value = filename,
                    onValueChange = { filename = it },
                    label = { Text("Filename") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = gestureDescription,
                    onValueChange = { gestureDescription = it },
                    label = { Text("Gesture Description") },
                    placeholder = { Text("e.g., Full flexion for 5 seconds") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.exportFeaturesWithMetadata(filename, gestureDescription)
                    viewModel.resumeModelAfterExport()
                    onDismiss()
                }
            ) {
                Text("Export")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    viewModel.resumeModelAfterExport()
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}




