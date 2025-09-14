package com.exosuit.exoappwithridgeregression

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.exosuit.exoappwithridgeregression.composables.MotorStatusIndicator
import com.exosuit.exoappwithridgeregression.composables.NoModelScreen
import com.exosuit.exoappwithridgeregression.data_classes.ModelType
import com.exosuit.exoappwithridgeregression.utility.UdpMotorController
import com.ncorti.myonnaise.MyoStatus
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin



@SuppressLint("MissingPermission")
@Composable
fun EmgScreen(viewModel: EmgViewModel, context: Context,
              navController: NavHostController,
              motorViewModel: MotorViewModel
) {

    val motorConnectionState by motorViewModel.connectionState.collectAsState()

    val isRecording by viewModel.isRecording
    val predicted by viewModel.predictedValue

 /*   val wristFlexionTarget = (predicted * 45f).toFloat()
    val wristFlexion by animateFloatAsState(targetValue = wristFlexionTarget)*/
    val smoothedAngle by viewModel.smoothedAngle.collectAsState()
    val wristFlexion by animateFloatAsState(targetValue = smoothedAngle)

    val myoStatus by viewModel.myoStatus.collectAsState()
    val modelExists by viewModel.modelExists.collectAsState()

    var showMyoDialog by remember { mutableStateOf(false) }
    var showModelChoiceDialog by remember { mutableStateOf(false) }
    val availableMyos by viewModel.availableMyos.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val modelActive by viewModel.modelActive.collectAsState()

    //Select Model
    var showModelSelectionDialog by remember { mutableStateOf(false) }


    // For Analysis
    var showExportDialog by remember { mutableStateOf(false) }



   // Check if model exists when screen is first shown
    LaunchedEffect(Unit) { viewModel.checkModelExists() }
    /*
        // Show model choice dialog if model exists
        LaunchedEffect(modelExists) {
            if (modelExists == true) showModelChoiceDialog = true
        }*/

    if (modelExists == true) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("EMG Exosuit Control", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {


                    MotorStatusIndicator(motorConnectionState)

                    // Start button - only enabled when ready to start
                    Button(
                        onClick = {
                            motorViewModel.sendStartSignal { success, error ->
                                if (!success) {
                                    // Handle error
                                }
                            }
                        },
                        enabled = motorConnectionState == UdpMotorController.ConnectionState.READY_TO_START
                    ) {
                        Text("Start System")
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))


                ArmCanvas(
                    wristFlexion = wristFlexion,
                    radialDeviation = 0f,
                    cableTension = 20f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))


/*                Text(
                    "Predicted Value: %.3f (%s)".format(
                        predicted,
                        when (viewModel.activeModelType) {
                            ModelType.TFLITE -> "TFLite"
                            else -> "Ridge"
                        }
                    )
                )*/

                Text(
                    "Predicted Values: [%s] (%s)".format(
                        predicted.joinToString(", ") { "%.3f".format(it) },
                        when (viewModel.activeModelType) {
                            ModelType.TFLITE -> "TFLite"
                            else -> "Ridge"
                        }
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Model: ${selectedModel ?: "None"} (${
                        when (viewModel.activeModelType) {
                            ModelType.TFLITE -> "TFLite"
                            else -> "Ridge"
                        }
                    })",
                    color = if (selectedModel != null) Color.Green else Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showModelSelectionDialog = true },
                        enabled = availableModels.isNotEmpty()
                    ) { Text("Select Model") }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activate Model", modifier = Modifier.weight(1f))
                    Switch(
                        checked = modelActive,
                        onCheckedChange = { viewModel.toggleModelActive(it) },
                        enabled = selectedModel != null
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

/*
                Button(
                    onClick = {
                        */
/*viewModel.sendSavedTrainingData(
                        filename = "emg_raw_data.csv",
                        modelType = ModelType.RIDGE // or whatever model type you want
                    )*//*

                    },
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Train from Saved Data")
                }
*/


                Spacer(modifier = Modifier.height(20.dp))

                when (myoStatus) {
                    MyoStatus.CONNECTING -> {
                        showMyoDialog = false
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Connecting to Myo...", color = Color.Gray)
                    }

                    MyoStatus.CONNECTED, MyoStatus.READY -> {
                        Button(onClick = { viewModel.disconnectMyo() }) { Text("Disconnect Myo") }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Myo Status: $myoStatus", color = Color.Green)
                    }

                    MyoStatus.DISCONNECTED -> {
                        Button(
                            onClick = { viewModel.scanForMyos(); showMyoDialog = true },
                            enabled = viewModel.permissionsGranted.value
                        ) { Text("Scan for Myo Bands") }

                        if (!viewModel.permissionsGranted.value) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Please grant Bluetooth and location permissions to scan.",
                                color = Color.Red
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(NavGraph.Screen.GuidedRecording.route) },
                    enabled = !isRecording && myoStatus == MyoStatus.READY
                ) { Text("Start New Training") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { navController.navigate("motor_settings") }) {
                    Text("Motor Settings")
                }
/* just uncomment later if need to export data for testing
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Export Features")

                }
*/


             /*   Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.checkModelExists() }) { Text("Check for Model") }*/
            }





    } else {
        NoModelScreen(
            myoStatus = myoStatus,
            permissionsGranted = viewModel.permissionsGranted.value,
            onScanMyo = { viewModel.scanForMyos(); showMyoDialog = true },
            onStartTraining = { navController.navigate(NavGraph.Screen.GuidedRecording.route) },
            onCheckModels = { viewModel.checkModelExists() }
        )
    }

    // Show the dialog when needed
/*    if (showExportDialog) {
        ExportFeaturesDialog(
            viewModel = viewModel,
            onDismiss = { showExportDialog = false }
        )
    }*/
    // Model Selection Dialog
    if (showModelSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showModelSelectionDialog = false },
            title = { Text("Select Model") },
            text = {
                Column {
                    if (availableModels.isEmpty()) Text("No models available")
                    else {
                        availableModels.forEach { modelName ->
                            //val typeLabel = if (modelName.endsWith(".tflite")) "TFLite" else "Ridge"
                            val typeLabel = when {
                                modelName.endsWith(".tflite") -> "TFLite"
                                modelName.contains("_hybrid_") -> "Hybrid"
                                else -> "Ridge"
                            }
                            Button(
                                onClick = {

                                    when (typeLabel) {
                                        "TFLite" -> {
                                            viewModel.loadTfliteInterpreter(modelName)
                                            viewModel.activeModelType = ModelType.TFLITE
                                        }

                                        else -> {
                                            viewModel.loadModel(modelName)
                                            viewModel.activeModelType = ModelType.RIDGE_FOR_EXO
                                        }
                                    }
                                    showModelSelectionDialog = false
                                    Toast.makeText(
                                        context,
                                        "$typeLabel model $modelName loaded",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("$modelName ($typeLabel)") }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showModelSelectionDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Model Choice Dialog
    if (showModelChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showModelChoiceDialog = false },
            title = { Text("Model Found") },
            text = {
                Column {
                    Text("${availableModels.size} model(s) found. Would you like to select one?")
                    Spacer(modifier = Modifier.height(8.dp))
                    availableModels.forEach { modelName ->
                        //val typeLabel = if (modelName.endsWith(".tflite")) "TFLite" else "Ridge"
                        val typeLabel = when {
                            modelName.endsWith(".tflite") -> "TFLite"
                            modelName.contains("_hybrid_") -> "Hybrid"
                            else -> "Ridge"
                        }
                        Button(
                            onClick = {

                                when (typeLabel) {
                                    "TFLite" -> {
                                        viewModel.loadTfliteInterpreter(modelName)
                                        viewModel.activeModelType = ModelType.TFLITE
                                    }

                                    else -> {
                                        viewModel.loadModel(modelName)
                                        viewModel.activeModelType = ModelType.RIDGE_FOR_EXO
                                    }
                                }
                                showModelChoiceDialog = false
                                Toast.makeText(context, "$typeLabel model $modelName loaded", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("$modelName ($typeLabel)") }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (availableModels.isNotEmpty()) {
                            val firstModel = availableModels.first()
                            if (firstModel.endsWith(".tflite")) {
                                viewModel.loadTfliteInterpreter(firstModel)
                                viewModel.activeModelType = ModelType.TFLITE
                            } else {
                                viewModel.loadModel(firstModel)
                                viewModel.activeModelType = ModelType.RIDGE_FOR_EXO
                            }
                            Toast.makeText(context, "Model $firstModel loaded", Toast.LENGTH_SHORT).show()
                        }
                        showModelChoiceDialog = false
                    }
                ) { Text("Use First Model") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showModelChoiceDialog = false
                        Toast.makeText(context, "You can select a model later", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Select Later") }
            }
        )
    }

    // Myo Selection Dialog
    if (showMyoDialog) {
        Dialog(onDismissRequest = { showMyoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxHeight()
                ) {
                    Text("Select a Myo Band", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (availableMyos.isEmpty()) Text("Scanning...")
                        else LazyColumn {
                            items(availableMyos) { device ->
                                val displayName = device.name?.takeIf { it.isNotBlank() } ?: device.address
                                Button(
                                    onClick = {
                                        viewModel.connectToMyo(
                                            device,
                                            onConnected = {
                                                showMyoDialog = false
                                                Toast.makeText(
                                                    context,
                                                    "Connected to $displayName",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onConnecting = { showMyoDialog = false },
                                            onError = { err ->
                                                showMyoDialog = false
                                                Toast.makeText(
                                                    context,
                                                    "Failed to connect: ${err.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) { Text(displayName) }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.scanForMyos() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Retry Scan") }
                }
            }
        }
    }
}



@Composable
fun ArmCanvas(
    wristFlexion: Float,     // wrist flexion in degrees (-45 to 45)
    radialDeviation: Float,
    cableTension: Float,
    modifier: Modifier = Modifier
) {
    // Validate inputs to prevent NaN values
    val safeWristFlexion = if (wristFlexion.isNaN() || wristFlexion.isInfinite()) 0f else wristFlexion
    val safeRadialDeviation = if (radialDeviation.isNaN() || radialDeviation.isInfinite()) 0f else radialDeviation
    val safeCableTension = if (cableTension.isNaN() || cableTension.isInfinite()) 0f else cableTension

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val shoulder = Offset(canvasWidth * 0.5f, canvasHeight * 0.2f)

        val upperArmLength = canvasHeight * 0.3f
        val forearmLength = canvasHeight * 0.25f
        val handLength = canvasHeight * 0.2f

        val initialUpperArmAngle = 18f
        val initialElbowAngle = 36f
        val initialWristFlexion = 36f

        val upperArmAngleRad = Math.toRadians(initialUpperArmAngle.toDouble())
        val combinedAngleRad = Math.toRadians((initialUpperArmAngle + initialElbowAngle).toDouble())

        // Draw shoulder
        drawCircle(
            color = Color(0xFF3F51B5),
            center = shoulder,
            radius = 20f
        )

        val elbow = Offset(
            shoulder.x + upperArmLength * sin(upperArmAngleRad).toFloat(),
            shoulder.y + upperArmLength * cos(upperArmAngleRad).toFloat()
        )

        drawLine(
            color = Color(0xFF3F51B5),
            start = shoulder,
            end = elbow,
            strokeWidth = 30f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Color(0xFF3F51B5),
            center = elbow,
            radius = 15f
        )

        val wristBase = Offset(
            elbow.x + forearmLength * sin(combinedAngleRad).toFloat(),
            elbow.y + forearmLength * cos(combinedAngleRad).toFloat()
        )

        val wrist = Offset(
            wristBase.x + safeRadialDeviation * (forearmLength / 30f),
            wristBase.y
        )

        drawLine(
            color = Color(0xFF2196F3),
            start = elbow,
            end = wrist,
            strokeWidth = 25f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Color(0xFF2196F3),
            center = wrist,
            radius = 25f
        )

        val forearmVector = wrist - elbow
        val forearmAngle = atan2(forearmVector.x, forearmVector.y)

        val totalWristAngle = initialWristFlexion + safeWristFlexion
        val handAngle = forearmAngle + totalWristAngle * (Math.PI / 180f).toFloat()

        // Validate handAngle to prevent NaN values
        val safeHandAngle = if (handAngle.isNaN() || handAngle.isInfinite()) 0f else handAngle

        val hand = Offset(
            wrist.x + handLength * sin(safeHandAngle),
            wrist.y + handLength * cos(safeHandAngle)
        )

        // Only draw if hand position is valid
        if (!hand.x.isNaN() && !hand.y.isNaN()) {
            drawLine(
                color = Color(0xFF4CAF50),
                start = wrist,
                end = hand,
                strokeWidth = 30f,
                cap = StrokeCap.Round
            )

            // Draw cables
            val cableCount = 3
            val cableColor = when {
                safeCableTension > 45f -> Color.Red
                safeCableTension > 35f -> Color.Yellow
                else -> Color(0xFFFF9800)
            }
            val cableWidth = 3f + (safeCableTension / 50f) * 5f

            for (i in 0 until cableCount) {
                val cableStart = Offset(
                    shoulder.x + (i - 1) * 15f,
                    shoulder.y
                )

                drawLine(
                    color = cableColor,
                    start = cableStart,
                    end = hand,
                    strokeWidth = cableWidth,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 5f),
                        phase = 0f
                    )
                )
            }

            val isCritical = abs(totalWristAngle) > 75f || abs(safeRadialDeviation) > 25f
            val circleColor = if (isCritical) Color.Red else Color(0xFF4CAF50)
            val circleRadius = 25f

            drawCircle(
                color = circleColor,
                center = hand,
                radius = circleRadius
            )

            if (isCritical) {
                drawCircle(
                    color = Color.Red.copy(alpha = 0.3f),
                    center = hand,
                    radius = circleRadius * 1.5f
                )
            }
        }
    }
}

