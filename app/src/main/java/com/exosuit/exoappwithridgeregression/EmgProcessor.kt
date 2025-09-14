package com.exosuit.exoappwithridgeregression

import java.lang.Math.abs
import kotlin.math.sqrt
import kotlin.random.Random


class EmgProcessor(
    private val fs: Float = 200f,
    private val hpfCutoff: Float = 10f,
    private val lpfCutoff: Float = 3f,
    private val numChannels: Int = 8,
    private val windowSize: Int = 100 // Increased window size for better stability
) {
    // Circular buffers for each channel
    private val channelBuffers = Array(numChannels) { FloatArray(windowSize) }
    private var bufferIndex = 0
    private val bufferFilled = BooleanArray(numChannels) { false }

    // Filters
    private val prevInput = FloatArray(numChannels)
    private val prevOutput = FloatArray(numChannels)
    private val lpfPrev = FloatArray(numChannels) { 0f }

    // Filter coefficients
    private val hpfRC = 1f / (2f * Math.PI.toFloat() * hpfCutoff)
    private val hpfAlpha = hpfRC / (hpfRC + 1f / fs)
    private val lpfAlpha: Float = (1 - kotlin.math.exp(-2 * Math.PI * lpfCutoff / fs)).toFloat()

    // Process a sample and return a feature vector (multiple features per channel)
    fun processSample(sample: List<Float>): List<Float> {
        val features = mutableListOf<Float>()

        for (ch in 0 until numChannels) {
            // High-pass filter
            val hpfOut = hpfAlpha * (prevOutput[ch] + sample[ch] - prevInput[ch])
            prevInput[ch] = sample[ch]
            prevOutput[ch] = hpfOut

            // Rectify
            val rectified = abs(hpfOut)

            // Update circular buffer
            channelBuffers[ch][bufferIndex] = rectified
            if (!bufferFilled[ch] && bufferIndex == windowSize - 1) {
                bufferFilled[ch] = true
            }

            // Calculate features only if buffer is filled
            if (bufferFilled[ch]) {
                val buffer = channelBuffers[ch]
                val rms = calculateRMS(buffer)
                val mav = calculateMAV(buffer)
                val wl = calculateWL(buffer)
                val ssc = calculateSSC(buffer)
                val zc = calculateZC(buffer)

                // Apply LPF on RMS for smoothing
                lpfPrev[ch] = lpfAlpha * rms + (1f - lpfAlpha) * lpfPrev[ch]
                features.add(lpfPrev[ch])
                features.add(mav)
                features.add(wl)
                features.add(ssc)
                features.add(zc)
            } else {
                // Add zeros if buffer not filled
                features.add(0f)
                features.add(0f)
                features.add(0f)
                features.add(0f)
                features.add(0f)
            }
        }

        bufferIndex = (bufferIndex + 1) % windowSize

        // ADD DEBUG LOGGING HERE
        if (Random.nextInt(100) == 0) { // Log approximately 1% of samples
            android.util.Log.d("MyoScan", "Generated ${features.size} features: ${features.take(5).joinToString()}...")
        }

        return features
    }
    // Calculates the Root Mean Square (RMS) of the signal.
    private fun calculateRMS(buffer: FloatArray): Float {
        var sumSquares = 0f
        for (value in buffer) {
            sumSquares += value * value
        }
        return sqrt(sumSquares / buffer.size)
    }
    // Calculates the Mean Absolute Value (MAV) of the signal.
    private fun calculateMAV(buffer: FloatArray): Float {
        var sum = 0f
        for (value in buffer) {
            sum += abs(value)
        }
        return sum / buffer.size
    }
    // Calculates the Waveform Length (WL) of the signal.
// WL is the cumulative sum of absolute differences between consecutive samples and reflects
// the signal’s complexity or variability over time.
    private fun calculateWL(buffer: FloatArray): Float {
        var sum = 0f
        for (i in 1 until buffer.size) {
            sum += abs(buffer[i] - buffer[i - 1])
        }
        return sum
    }
    // Calculates the Slope Sign Changes (SSC) of the signal.
// SSC counts the number of times the slope changes direction, which captures frequency characteristics.
    private fun calculateSSC(buffer: FloatArray): Float {
        var count = 0
        for (i in 1 until buffer.size - 1) {
            val a = buffer[i - 1]
            val b = buffer[i]
            val c = buffer[i + 1]
            if ((b > a && b > c) || (b < a && b < c)) {
                count++
            }
        }
        return count.toFloat()
    }

    // Calculates the Zero Crossings (ZC) of the signal.
// ZC counts how many times the signal crosses zero, giving an indication of signal frequency content.
    private fun calculateZC(buffer: FloatArray): Float {
        var count = 0
        for (i in 1 until buffer.size) {
            if (buffer[i] * buffer[i - 1] < 0) {
                count++
            }
        }
        return count.toFloat()
    }
}



/*

class EmgProcessor(
    private val fs: Float = 200f,       // sampling rate
    private val hpfCutoff: Float = 10f, // HPF cutoff in Hz
    private val lpfCutoff: Float = 3f,  // LPF cutoff in Hz
    private val numChannels: Int = 8,
    private val windowSize: Int = 50    // sliding window size
) {
    // Previous samples for HPF
    private val prevInput = FloatArray(numChannels)
    private val prevOutput = FloatArray(numChannels)

    // Sliding window buffers per channel
    private val channelBuffers = Array(numChannels) { mutableListOf<Float>() }

    // Previous LPF values (optional)
    private val lpfPrev = FloatArray(numChannels) { 0f }

    // HPF coefficient (first-order)
    private val hpfRC = 1f / (2f * Math.PI.toFloat() * hpfCutoff)
    private val hpfAlpha = hpfRC / (hpfRC + 1f / fs)

    // LPF coefficient for exponential smoothing
    //private val lpfAlpha = 1 - kotlin.math.exp(-2 * Math.PI * lpfCutoff / fs)
    private val lpfAlpha: Float = (1 - kotlin.math.exp(-2 * Math.PI * lpfCutoff / fs)).toFloat()
    */
/**
     * Process a single EMG sample (numChannels floats)
     * Returns feature vector: RMS per channel
     *//*

    fun processSample(sample: List<Float>): List<Float> {
        val features = FloatArray(numChannels)

        for (ch in 0 until numChannels) {
            // --- High-pass filter (first-order) ---
            val hpfOut = hpfAlpha * (prevOutput[ch] + sample[ch] - prevInput[ch])
            prevInput[ch] = sample[ch]
            prevOutput[ch] = hpfOut

            // --- Rectify ---
            val rectified = abs(hpfOut)

            // --- Sliding window RMS ---
            val buffer = channelBuffers[ch]
            buffer.add(rectified)
            if (buffer.size > windowSize) buffer.removeAt(0)

            var sumSquares = 0f
            for (v in buffer) sumSquares += v * v
            var rms = sqrt(sumSquares / buffer.size)

            // --- Optional LPF on RMS ---
            lpfPrev[ch] = (lpfAlpha * rms + (1f - lpfAlpha) * lpfPrev[ch])
            rms = lpfPrev[ch]

            features[ch] = rms
        }

        return features.toList()
    }
}
*/
