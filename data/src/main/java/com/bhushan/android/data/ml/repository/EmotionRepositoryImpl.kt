package com.bhushan.android.data.ml.repository

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import com.bhushan.android.data.utils.ONNXModelLoader
import com.bhushan.android.data.utils.TFLiteModelLoader
import com.bhushan.android.domain.ml.model.ImageData
import com.bhushan.android.domain.ml.repository.EmotionRepository
import org.tensorflow.lite.Interpreter

class EmotionRepositoryImpl(
    private val context: Context
) : EmotionRepository {

    private val tfliteLabels =
        listOf("Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")
    private val onnxLabels =
        listOf("Angry", "Disgust", "Fear", "Happy", "Neutral", "Sad", "Surprise")

    // Use the provided model loader objects for proper file and interpreter/session management
    private val ortEnv: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private val tfliteInterpreter: Interpreter by lazy {
        TFLiteModelLoader.loadModel(context, "emotion_model.tflite")
    }

    private val onnxSession: OrtSession by lazy {
        ortEnv.createSession(
            ONNXModelLoader.loadModelFile(context, "model.onnx")
        )
    }

    override suspend fun detectEmotionTFLite(image: ImageData): String =
        try {
            val input = preprocessTFLiteInput(image)
            val output = Array(1) { FloatArray(tfliteLabels.size) }
            tfliteInterpreter.run(input, output)
            val idx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            if (idx >= 0) tfliteLabels[idx] else "Unknown"
        } catch (e: Exception) {
            Log.e("EmotionRepo", "TFLite error: ${e.message}", e)
            "Error"
        }


    override suspend fun detectEmotionOnnx(image: ImageData): String =
        try {
            val input = preprocessOnnxInput(image.pixels, image.width, image.height)
            val tensor = OnnxTensor.createTensor(ortEnv, input)
            val results = onnxSession.run(mapOf("pixel_values" to tensor))
            val outputTensor = results[0].value as Array<FloatArray>
            val idx = outputTensor[0].indices.maxByOrNull { outputTensor[0][it] } ?: -1
            if (idx >= 0) onnxLabels[idx] else "Unknown"
        } catch (e: Exception) {
            Log.e("EmotionRepo", "ONNX error: ${e.message}", e)
            "Error"
        }

    // --- Preprocessing helpers ---

    private fun preprocessTFLiteInput(image: ImageData): Array<Array<Array<FloatArray>>> {
        val size = 48
        val input = Array(1) { Array(size) { Array(size) { FloatArray(1) } } }
        for (y in 0 until size) {
            for (x in 0 until size) {
                input[0][y][x][0] = image.pixels[y * size + x]
            }
        }
        return input
    }

    private fun preprocessOnnxInput(
        pixels: FloatArray,
        width: Int = 224,
        height: Int = 224
    ): Array<Array<Array<FloatArray>>> {
        require(pixels.size == width * height * 3) {
            "Pixel array must be of size width*height*3 (${width * height * 3}), was ${pixels.size}"
        }
        val input = Array(1) { Array(3) { Array(height) { FloatArray(width) } } }
        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                input[0][0][y][x] = pixels[idx++] // R
                input[0][1][y][x] = pixels[idx++] // G
                input[0][2][y][x] = pixels[idx++] // B
            }
        }
        return input
    }
}