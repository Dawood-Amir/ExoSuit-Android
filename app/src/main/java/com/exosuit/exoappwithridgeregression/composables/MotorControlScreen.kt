package com.exosuit.exoappwithridgeregression.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.exosuit.exoappwithridgeregression.MotorViewModel
import com.exosuit.exoappwithridgeregression.data_classes.MotorSettings







@Composable
fun MotorControlScreen(
    navController: NavHostController? = null,
    viewModel: MotorViewModel
) {
    val motorSettings by viewModel.motorSettings.collectAsState()

    // Use a single state object for better performance
    var uiState by remember(motorSettings) {
        mutableStateOf(
            MotorUiState.fromMotorSettings(motorSettings)
        )
    }
    var isLocked by remember { mutableStateOf(false) }

    // Update UI state when motorSettings changes
    LaunchedEffect(motorSettings) {
        uiState = MotorUiState.fromMotorSettings(motorSettings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Lock switch at the top
        LockSwitchCard(isLocked, onLockChange = { isLocked = it })

        Text(
            text = "Motor Control Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Impedance Control Card
        ImpedanceControlCard(
            kpValue = uiState.kp,
            onKpChange = { uiState = uiState.copy(kp = it) },
            kdValue = uiState.kd,
            onKdChange = { uiState = uiState.copy(kd = it) },
            enabled = !isLocked
        )

        // Safety Limits Card
        SafetyLimitsCard(
            maxTorqueValue = uiState.maxTorque,
            onMaxTorqueChange = { uiState = uiState.copy(maxTorque = it) },
            enabled = !isLocked
        )

        // Force Parameters Card
        ForceParametersCard(
            extensionStrengthMax = uiState.extensionStrengthMax,
            onExtensionStrengthMaxChange = { uiState = uiState.copy(extensionStrengthMax = it) },
            flexionStrengthMax = uiState.flexionStrengthMax,
            onFlexionStrengthMaxChange = { uiState = uiState.copy(flexionStrengthMax = it) },
            minTorqueExtension = uiState.minTorqueExtension,
            onMinTorqueExtensionChange = { uiState = uiState.copy(minTorqueExtension = it) },
            minTorqueFlexion = uiState.minTorqueFlexion,
            onMinTorqueFlexionChange = { uiState = uiState.copy(minTorqueFlexion = it) },
            enabled = !isLocked
        )

        // Advanced Parameters Card
        AdvancedParametersCard(
            baseTorque = uiState.baseTorque,
            onBaseTorqueChange = { uiState = uiState.copy(baseTorque = it) },
            alpha = uiState.alpha,
            onAlphaChange = { uiState = uiState.copy(alpha = it) },
            enabled = !isLocked
        )

        // Save Button
        SaveButton(
            isLocked = isLocked,
            onClick = {
                viewModel.updateMotorSettings(uiState.toMotorSettings())
            }
        )
    }
}

// Data class to hold all UI state
data class MotorUiState(
    val kp: Float = MotorSettings.KP_DEFAULT,
    val kd: Float = MotorSettings.KD_DEFAULT,
    val maxTorque: Float = MotorSettings.MAX_TORQUE_DEFAULT,
    val extensionStrengthMax: Float = MotorSettings.EXTENSION_STRENGTH_MAX_DEFAULT,
    val flexionStrengthMax: Float = MotorSettings.FLEXION_STRENGTH_MAX_DEFAULT,
    val minTorqueExtension: Float = MotorSettings.MIN_TORQUE_EXTENSION_DEFAULT,
    val minTorqueFlexion: Float = MotorSettings.MIN_TORQUE_FLEXION_DEFAULT,
    val baseTorque: Float = MotorSettings.BASE_TORQUE_DEFAULT,
    val alpha: Float = MotorSettings.ALPHA_DEFAULT
) {
    companion object {
        fun fromMotorSettings(settings: MotorSettings): MotorUiState {
            return MotorUiState(
                kp = settings.kp,
                kd = settings.kd,
                maxTorque = settings.maxTorque,
                extensionStrengthMax = settings.extensionStrengthMax,
                flexionStrengthMax = settings.flexionStrengthMax,
                minTorqueExtension = settings.minTorqueExtension,
                minTorqueFlexion = settings.minTorqueFlexion,
                baseTorque = settings.baseTorque,
                alpha = settings.alpha
            )
        }
    }

    fun toMotorSettings(): MotorSettings {
        return MotorSettings(
            kp = kp,
            kd = kd,
            maxTorque = maxTorque,
            extensionStrengthMax = extensionStrengthMax,
            flexionStrengthMax = flexionStrengthMax,
            minTorqueExtension = minTorqueExtension,
            minTorqueFlexion = minTorqueFlexion,
            baseTorque = baseTorque,
            alpha = alpha
        )
    }
}

// Extract card components for better performance
@Composable
fun LockSwitchCard(isLocked: Boolean, onLockChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lock Controls",
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = isLocked,
                onCheckedChange = onLockChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.error,
                    checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                    uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun ImpedanceControlCard(
    kpValue: Float,
    onKpChange: (Float) -> Unit,
    kdValue: Float,
    onKdChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Impedance Control",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingSlider(
                label = "Stiffness (Kp)",
                value = kpValue,
                onValueChange = onKpChange,
                range = MotorSettings.KP_MIN..MotorSettings.KP_MAX,
                enabled = enabled,
                decimalPlaces = 2
            )
            SettingSlider(
                label = "Damping (Kd)",
                value = kdValue,
                onValueChange = onKdChange,
                range = MotorSettings.KD_MIN..MotorSettings.KD_MAX,
                enabled = enabled,
                decimalPlaces = 3
            )
        }
    }
}

@Composable
fun SafetyLimitsCard(
    maxTorqueValue: Float,
    onMaxTorqueChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Safety Limits",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingSlider(
                label = "Max Torque (Nm)",
                value = maxTorqueValue,
                onValueChange = onMaxTorqueChange,
                range = MotorSettings.MAX_TORQUE_MIN..MotorSettings.MAX_TORQUE_MAX,
                enabled = enabled,
                decimalPlaces = 1
            )
        }
    }
}

@Composable
fun ForceParametersCard(
    extensionStrengthMax: Float,
    onExtensionStrengthMaxChange: (Float) -> Unit,
    flexionStrengthMax: Float,
    onFlexionStrengthMaxChange: (Float) -> Unit,
    minTorqueExtension: Float,
    onMinTorqueExtensionChange: (Float) -> Unit,
    minTorqueFlexion: Float,
    onMinTorqueFlexionChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Force Parameters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingSlider(
                label = "Extension Strength Max (Nm)",
                value = extensionStrengthMax,
                onValueChange = onExtensionStrengthMaxChange,
                range = MotorSettings.EXTENSION_STRENGTH_MAX_MIN..MotorSettings.EXTENSION_STRENGTH_MAX_MAX,
                enabled = enabled,
                decimalPlaces = 1
            )
            SettingSlider(
                label = "Flexion Strength Max (Nm)",
                value = flexionStrengthMax,
                onValueChange = onFlexionStrengthMaxChange,
                range = MotorSettings.FLEXION_STRENGTH_MAX_MIN..MotorSettings.FLEXION_STRENGTH_MAX_MAX,
                enabled = enabled,
                decimalPlaces = 1
            )
            SettingSlider(
                label = "Min Torque Extension (Nm)",
                value = minTorqueExtension,
                onValueChange = onMinTorqueExtensionChange,
                range = MotorSettings.MIN_TORQUE_EXTENSION_MIN..MotorSettings.MIN_TORQUE_EXTENSION_MAX,
                enabled = enabled,
                decimalPlaces = 1
            )
            SettingSlider(
                label = "Min Torque Flexion (Nm)",
                value = minTorqueFlexion,
                onValueChange = onMinTorqueFlexionChange,
                range = MotorSettings.MIN_TORQUE_FLEXION_MIN..MotorSettings.MIN_TORQUE_FLEXION_MAX,
                enabled = enabled,
                decimalPlaces = 1
            )
        }
    }
}

@Composable
fun AdvancedParametersCard(
    baseTorque: Float,
    onBaseTorqueChange: (Float) -> Unit,
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Advanced Parameters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingSlider(
                label = "Base Torque (Nm)",
                value = baseTorque,
                onValueChange = onBaseTorqueChange,
                range = MotorSettings.BASE_TORQUE_MIN..MotorSettings.BASE_TORQUE_MAX,
                enabled = enabled,
                decimalPlaces = 1
            )
            SettingSlider(
                label = "Alpha (Filter Coefficient)",
                value = alpha,
                onValueChange = onAlphaChange,
                range = MotorSettings.ALPHA_MIN..MotorSettings.ALPHA_MAX,
                enabled = enabled,
                decimalPlaces = 2
            )
        }
    }
}

@Composable
fun SaveButton(
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLocked,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(
            "Save and Apply",
            style = MaterialTheme.typography.labelLarge,
            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    decimalPlaces: Int = 2,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "%.${decimalPlaces}f".format(value),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                activeTrackColor = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "%.${decimalPlaces}f".format(range.start),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "%.${decimalPlaces}f".format(range.endInclusive),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}



/*






fun Modifier.disabled(disabled: Boolean) = composed {
    if (disabled) {
        graphicsLayer(alpha = 0.6f)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            }
    } else {
        this
    }
}

// MotorControlScreen.kt
@Composable
fun MotorControlScreen(
    navController: NavHostController? = null,
    viewModel: MotorViewModel
) {
    val motorSettings by viewModel.motorSettings.collectAsState()


    var kpValue by remember { mutableStateOf(MotorSettings.KP_DEFAULT) }
    var kdValue by remember { mutableStateOf(MotorSettings.KD_DEFAULT) }
    var kiValue by remember { mutableStateOf(MotorSettings.KI_DEFAULT) }
    var torqueValue by remember { mutableStateOf(MotorSettings.TORQUE_DEFAULT) }
    var maxTorqueValue by remember { mutableStateOf(MotorSettings.MAX_TORQUE_DEFAULT) }
    var targetPosition by remember { mutableStateOf(MotorSettings.TARGET_POSITION_DEFAULT) }
    var targetVelocity by remember { mutableStateOf(MotorSettings.TARGET_VELOCITY_DEFAULT) }
    var iWindupValue by remember { mutableStateOf(MotorSettings.I_WINDUP_DEFAULT) }
    var maxVelocityValue by remember { mutableStateOf(MotorSettings.MAX_VELOCITY_DEFAULT) }
    var upperPositionLimit by remember { mutableStateOf(MotorSettings.UPPER_POSITION_LIMIT_DEFAULT) }
    var lowerPositionLimit by remember { mutableStateOf(MotorSettings.LOWER_POSITION_LIMIT_DEFAULT) }
    var candleMode by remember { mutableStateOf(0) }
    var motorId by remember { mutableStateOf(0) }
    var zeroEncoderBool by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }

// Synchronize UI state with latest motorSettings whenever it updates
    LaunchedEffect(motorSettings) {
        kpValue = motorSettings.kp
        kdValue = motorSettings.kd
        kiValue = motorSettings.ki
        torqueValue = motorSettings.torque
        maxTorqueValue = motorSettings.maxTorque
        targetPosition = motorSettings.targetPosition
        targetVelocity = motorSettings.targetVelocity
        iWindupValue = motorSettings.iWindup
        maxVelocityValue = motorSettings.maxVelocity
        upperPositionLimit = motorSettings.upperPositionLimit
        lowerPositionLimit = motorSettings.lowerPositionLimit
        candleMode = motorSettings.candleMode
        motorId = motorSettings.motorId
        zeroEncoderBool = motorSettings.zeroOutEncoder
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lock Controls",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = isLocked,
                    onCheckedChange = { isLocked = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }


        Text(
            text = "Motor Control Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Motor ID Selection
        MotorIdSelection(motorId = motorId, onMotorIdChanged = { motorId = it } , enabled = !isLocked)

        // Mode Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Mode Settings",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ModeDropdown(
                    label = "Select motor mode",
                    currentMode = candleMode,
                    enabled = !isLocked,
                    onModeSelected = { newMode -> candleMode = newMode }
                )
                SettingSwitch(
                    label = "Zero Encoder",
                    checked = zeroEncoderBool,
                    onCheckedChange = { zeroEncoderBool = it },
                    enabled = !isLocked
                )
            }
        }

        // PID Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PID Control",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SettingSlider(
                    label = "KP Value",
                    value = kpValue,
                    onValueChange = { kpValue = it },
                    range = MotorSettings.KP_MIN..MotorSettings.KP_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 2
                )
                SettingSlider(
                    label = "KD Value",
                    value = kdValue,
                    onValueChange = { kdValue = it },
                    range = MotorSettings.KD_MIN..MotorSettings.KD_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 2
                )
                SettingSlider(
                    label = "KI Value",
                    value = kiValue,
                    onValueChange = { kiValue = it },
                    range = MotorSettings.KI_MIN..MotorSettings.KI_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 2
                )
                SettingSlider(
                    label = "I-Windup",
                    value = iWindupValue,
                    onValueChange = { iWindupValue = it },
                    range = MotorSettings.I_WINDUP_MIN..MotorSettings.I_WINDUP_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 1
                )
            }
        }

        // Torque Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ){
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Torque Control",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SettingSlider(
                    label = "Torque",
                    value = torqueValue,
                    onValueChange = { torqueValue = it },
                    range = MotorSettings.TORQUE_MIN..MotorSettings.TORQUE_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 1
                )
                SettingSlider(
                    label = "Max Torque",
                    value = maxTorqueValue,
                    onValueChange = { maxTorqueValue = it },
                    range = MotorSettings.MAX_TORQUE_MIN..MotorSettings.MAX_TORQUE_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 1
                )
            }
        }

        // Velocity Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Velocity Control",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SettingSlider(
                    label = "Max Velocity",
                    value = maxVelocityValue,
                    onValueChange = { maxVelocityValue = it },
                    range = MotorSettings.MAX_VELOCITY_MIN..MotorSettings.MAX_VELOCITY_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 1
                )
                SettingSlider(
                    label = "Target Velocity",
                    value = targetVelocity,
                    onValueChange = { targetVelocity = it },
                    range = MotorSettings.TARGET_VELOCITY_MIN..MotorSettings.TARGET_VELOCITY_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 1
                )
            }
        }

        // Position Control Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Position Control",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SettingSlider(
                    label = "Target Position",
                    value = targetPosition,
                    onValueChange = { targetPosition = it },
                    range = MotorSettings.TARGET_POSITION_MIN..MotorSettings.TARGET_POSITION_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 2
                )
                SettingSlider(
                    label = "Upper Position Limit",
                    value = upperPositionLimit,
                    onValueChange = { upperPositionLimit = it },
                    range = MotorSettings.UPPER_POSITION_LIMIT_MIN..MotorSettings.UPPER_POSITION_LIMIT_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 4
                )
                SettingSlider(
                    label = "Lower Position Limit",
                    value = lowerPositionLimit,
                    onValueChange = { lowerPositionLimit = it },
                    range = MotorSettings.LOWER_POSITION_LIMIT_MIN..MotorSettings.LOWER_POSITION_LIMIT_MAX,
                    enabled = !isLocked,
                    decimalPlaces = 4
                )
            }
        }

        // Save Button
        Button(
            onClick = {
                if (!isLocked) {
                    viewModel.saveMotorSettings(
                        MotorSettings(
                            kp = kpValue,
                            kd = kdValue,
                            ki = kiValue,
                            torque = torqueValue,
                            maxTorque = maxTorqueValue,
                            targetPosition = targetPosition,
                            targetVelocity = targetVelocity,
                            iWindup = iWindupValue,
                            maxVelocity = maxVelocityValue,
                            upperPositionLimit = upperPositionLimit,
                            lowerPositionLimit = lowerPositionLimit,
                            candleMode = candleMode,
                            motorId = motorId,
                            zeroOutEncoder = zeroEncoderBool
                        )
                    )
                }
            },
            enabled = !isLocked,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .disabled(isLocked)
        ) {
            Text(
                "Save and Apply",
                style = MaterialTheme.typography.labelLarge,
                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeDropdown(
    label: String,
    currentMode: Int,
    onModeSelected: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Map of mode values to their display names
    val modes = mapOf(
        0 to "IDLE",
        1 to "IMPEDANCE",
        2 to "VELOCITY_PID",
        3 to "POSITION_PID"
    )

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = modes[currentMode] ?: "UNKNOWN",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            label = { Text(label) },
            enabled = enabled,
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),

            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            modes.forEach { (modeValue, modeName) ->
                DropdownMenuItem(
                    text = { Text(modeName) },
                    onClick = {
                        onModeSelected(modeValue)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
private fun MotorIdSelection(motorId: Int, onMotorIdChanged: (Int) -> Unit ,enabled: Boolean = true ) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Motor ID",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = if (enabled) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Row(
            modifier = Modifier.fillMaxWidth()
                .disabled(!enabled),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MotorIdButton(
                id = 0,
                selected = motorId == 0,
                onClick = { if (enabled) onMotorIdChanged(0) },  // Only call if enabled
                enabled = enabled
            )
            MotorIdButton(
                id = 1,
                selected = motorId == 1,
                onClick = { if (enabled) onMotorIdChanged(1) },  // Only call if enabled
                enabled = enabled
            )
        }
    }
}

@Composable
private fun MotorIdButton(id: Int, selected: Boolean, onClick: () -> Unit,  enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                selected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                selected -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = CircleShape,
        modifier = Modifier.size(60.dp)
    ) {
        Text(text = id.toString(), style = MaterialTheme.typography.titleLarge,
            color = if (!enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            else if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled : Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp).disabled(!enabled),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (enabled) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled=enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }
}

@Composable
private fun SettingSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    decimalPlaces: Int = 2,
    enabled: Boolean = true,
    modifier: Modifier = Modifier


) {
    Column(modifier = modifier.padding(vertical = 8.dp).disabled(!enabled)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "%.${decimalPlaces}f".format(value),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                activeTrackColor = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "%.${decimalPlaces}f".format(range.start),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "%.${decimalPlaces}f".format(range.endInclusive),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

}
*/
