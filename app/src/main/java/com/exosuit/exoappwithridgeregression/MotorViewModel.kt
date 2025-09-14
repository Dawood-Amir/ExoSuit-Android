package com.exosuit.exoappwithridgeregression

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.exosuit.exoappwithridgeregression.data_classes.MotorSettings
import com.exosuit.exoappwithridgeregression.utility.UdpMotorController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MotorViewModel(application: Application) : AndroidViewModel(application) {
    private val udpController = UdpMotorController.getInstance(viewModelScope)

    private val _motorSettings = MutableStateFlow(MotorSettings())
    val motorSettings: StateFlow<MotorSettings> = _motorSettings

    private val sharedPreferences = application.getSharedPreferences("MotorControlPrefs", Context.MODE_PRIVATE)

    val connectionState: StateFlow<UdpMotorController.ConnectionState>
        get() = udpController.connectionState


    init {
        loadMotorSettings()
    }

    fun updateMotorSettings(newSettings: MotorSettings) {
        _motorSettings.value = newSettings
        saveMotorSettings(newSettings)
        udpController.sendMotorSettings(newSettings) { success, error ->
            if (!success) {
                Log.e("MyoScan", "Failed to send settings: $error")
            }
        }
    }
    fun sendStartSignal(onComplete: (Boolean, String?) -> Unit) {
        udpController.sendStartSignal(onComplete)
    }



    fun saveMotorSettings(settings: MotorSettings) {
        sharedPreferences.edit().apply {
            putFloat("kp", settings.kp)
            putFloat("kd", settings.kd)
            putFloat("maxTorque", settings.maxTorque)
            putFloat("maxVelocity", settings.maxVelocity)
            putFloat("upperPositionLimit", settings.upperPositionLimit)
            putFloat("lowerPositionLimit", settings.lowerPositionLimit)
            putInt("candleMode", settings.candleMode)
            putInt("motorId", settings.motorId)
            putFloat("extensionStrengthMax", settings.extensionStrengthMax)
            putFloat("flexionStrengthMax", settings.flexionStrengthMax)
            putFloat("minTorqueExtension", settings.minTorqueExtension)
            putFloat("minTorqueFlexion", settings.minTorqueFlexion)
            putFloat("baseTorque", settings.baseTorque)
            putFloat("alpha", settings.alpha)
            apply()
        }
        _motorSettings.value = settings.copy()
    }

    fun loadMotorSettings() {
        _motorSettings.value = MotorSettings(
            kp = sharedPreferences.getFloat("kp", MotorSettings.KP_DEFAULT),
            kd = sharedPreferences.getFloat("kd", MotorSettings.KD_DEFAULT),
            maxTorque = sharedPreferences.getFloat("maxTorque", MotorSettings.MAX_TORQUE_DEFAULT),
            maxVelocity = sharedPreferences.getFloat("maxVelocity", MotorSettings.MAX_VELOCITY_DEFAULT),
            upperPositionLimit = sharedPreferences.getFloat("upperPositionLimit", MotorSettings.UPPER_POSITION_LIMIT_DEFAULT),
            lowerPositionLimit = sharedPreferences.getFloat("lowerPositionLimit", MotorSettings.LOWER_POSITION_LIMIT_DEFAULT),
            candleMode = sharedPreferences.getInt("candleMode", 1),
            motorId = sharedPreferences.getInt("motorId", 0),
            extensionStrengthMax = sharedPreferences.getFloat("extensionStrengthMax", MotorSettings.EXTENSION_STRENGTH_MAX_DEFAULT),
            flexionStrengthMax = sharedPreferences.getFloat("flexionStrengthMax", MotorSettings.FLEXION_STRENGTH_MAX_DEFAULT),
            minTorqueExtension = sharedPreferences.getFloat("minTorqueExtension", MotorSettings.MIN_TORQUE_EXTENSION_DEFAULT),
            minTorqueFlexion = sharedPreferences.getFloat("minTorqueFlexion", MotorSettings.MIN_TORQUE_FLEXION_DEFAULT),
            baseTorque = sharedPreferences.getFloat("baseTorque", MotorSettings.BASE_TORQUE_DEFAULT),
            alpha = sharedPreferences.getFloat("alpha", MotorSettings.ALPHA_DEFAULT)
        )
    }
}