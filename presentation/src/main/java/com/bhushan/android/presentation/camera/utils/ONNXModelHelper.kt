package com.bhushan.android.presentation.camera.utils

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap

class ONNXModelHelper(
    private val modelPath: String,
    private val inputWidth: Int = 224,
    private val inputHeight: Int = 224,
    private val inputName: String = "pixel_values" // Check with Netron and update if needed
) {

    private val ortEnv: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val ortSession: OrtSession =
        ortEnv.createSession(modelPath, OrtSession.SessionOptions())

    fun runInference(bitmap: Bitmap): FloatArray? {
        if (ortSession == null) {
            throw IllegalStateException("Call loadModel() first!")
        }
        val inputTensor = preprocess(bitmap)
        val tensor = OnnxTensor.createTensor(ortEnv, inputTensor)
        val inputs = mapOf(inputName to tensor)
        val results = ortSession!!.run(inputs)

        @Suppress("UNCHECKED_CAST")
        val outputTensor = results[0].value as Array<FloatArray>
        return outputTensor[0]
    }

    /**
     * Preprocess Bitmap: resize, normalize, convert to [1, 3, 224, 224] float32
     * If you want to share logic with PreprocessingUtils, move RGB conversion there.
     */
    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val input = Array(1) { Array(3) { Array(inputHeight) { FloatArray(inputWidth) } } }
        for (y in 0 until inputHeight) {
            for (x in 0 until inputWidth) {
                val pixel = resized.getPixel(x, y)
                // Extract R, G, B and normalize [0,1]
                input[0][0][y][x] = ((pixel shr 16 and 0xFF) / 255.0f) // R
                input[0][1][y][x] = ((pixel shr 8 and 0xFF) / 255.0f)  // G
                input[0][2][y][x] = ((pixel and 0xFF) / 255.0f)        // B
            }
        }
        return input
    }
}