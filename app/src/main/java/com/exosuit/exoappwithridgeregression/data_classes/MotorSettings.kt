package com.exosuit.exoappwithridgeregression.data_classes

import org.json.JSONObject

data class MotorSettings(
    // Impedance control parameters
    val kp: Float = 5.0f,
    val kd: Float = 0.5f,

    // Safety limits
    val maxTorque: Float = 8.0f,
    val maxVelocity: Float = 2.0f,

    // Position limits
    val upperPositionLimit: Float = 1.57f,
    val lowerPositionLimit: Float = -1.57f,

    // Mode selection
    val candleMode: Int = 1,
    val motorId: Int = 0,

    // Force parameters (renamed to match Python expected keys)
    val extensionStrengthMax: Float = 6.0f,    // was extensionBigForce
    val flexionStrengthMax: Float = 7.0f,      // was flexionBigForce
    val minTorqueExtension: Float = 0.3f,      // was extensionSmallForce
    val minTorqueFlexion: Float = 0.5f,        // was flexionSmallForce

    // New parameters for Python handshake
    val baseTorque: Float = 0.2f,
    val alpha: Float = 0.1f
) {
    companion object {
        // Stiffness (kp)
        const val KP_MIN = 0.3f
        const val KP_MAX = 5.0f
        const val KP_DEFAULT = 5.0f

        // Damping (kd)
        const val KD_MIN = 0.015f
        const val KD_MAX = 0.5f
        const val KD_DEFAULT = 0.5f

        // Torque parameters
        const val MAX_TORQUE_MIN = 4.0f
        const val MAX_TORQUE_MAX = 12.0f
        const val MAX_TORQUE_DEFAULT = 8.0f

        // Velocity parameters
        const val MAX_VELOCITY_MIN = 1.0f
        const val MAX_VELOCITY_MAX = 3.0f
        const val MAX_VELOCITY_DEFAULT = 2.0f

        // Position parameters
        const val UPPER_POSITION_LIMIT_MIN = 0.79f
        const val UPPER_POSITION_LIMIT_MAX = 3.14f
        const val UPPER_POSITION_LIMIT_DEFAULT = 1.57f

        const val LOWER_POSITION_LIMIT_MIN = -3.14f
        const val LOWER_POSITION_LIMIT_MAX = -0.79f
        const val LOWER_POSITION_LIMIT_DEFAULT = -1.57f

        // Force parameters (updated names)
        const val EXTENSION_STRENGTH_MAX_MIN = 4.0f
        const val EXTENSION_STRENGTH_MAX_MAX = 10.0f
        const val EXTENSION_STRENGTH_MAX_DEFAULT = 6.0f

        const val FLEXION_STRENGTH_MAX_MIN = 4.0f
        const val FLEXION_STRENGTH_MAX_MAX = 10.0f
        const val FLEXION_STRENGTH_MAX_DEFAULT = 7.0f

        const val MIN_TORQUE_EXTENSION_MIN = 0.1f
        const val MIN_TORQUE_EXTENSION_MAX = 1.0f
        const val MIN_TORQUE_EXTENSION_DEFAULT = 0.3f

        const val MIN_TORQUE_FLEXION_MIN = 0.1f
        const val MIN_TORQUE_FLEXION_MAX = 1.0f
        const val MIN_TORQUE_FLEXION_DEFAULT = 0.5f

        // New parameters defaults
        const val BASE_TORQUE_MIN = 0.1f
        const val BASE_TORQUE_MAX = 1.0f
        const val BASE_TORQUE_DEFAULT = 0.2f

        const val ALPHA_MIN = 0.0f
        const val ALPHA_MAX = 1.0f
        const val ALPHA_DEFAULT = 0.1f
    }

    fun toJson(): String {
        return JSONObject().apply {
            put("kp", kp)
            put("kd", kd)
            put("maxTorque", maxTorque)
            put("extensionStrengthMax", extensionStrengthMax)
            put("flexionStrengthMax", flexionStrengthMax)
            put("minTorqueExtension", minTorqueExtension)
            put("minTorqueFlexion", minTorqueFlexion)
            put("baseTorque", baseTorque)
            put("alpha", alpha)
        }.toString()
    }
}