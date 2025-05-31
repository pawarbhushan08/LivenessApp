package com.bhushan.android.presentation.camera.vm

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bhushan.android.presentation.camera.utils.ONNXModelHelper
import com.bhushan.android.presentation.camera.utils.YuvToRgbConverter
import org.tensorflow.lite.Interpreter
import java.util.concurrent.atomic.AtomicBoolean

interface DualEmotionListener {
    fun onEmotionDetected(tfliteEmotion: String, onnxEmotion: String)
}

class DualEmotionAnalyzer(
    private val tfliteInterpreter: Interpreter,
    private val onnxModelHelper: ONNXModelHelper,
    private val listener: DualEmotionListener
) : ImageAnalysis.Analyzer {

    private val tfliteLabels =
        listOf("Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")
    private val onnxLabels = tfliteLabels // Change if your ONNX model has different classes

    private val working = AtomicBoolean(false)

    override fun analyze(image: ImageProxy) {
        if (working.getAndSet(true)) {
            image.close()
            return
        }
        try {
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            YuvToRgbConverter.yuvToRgb(image, bitmap)

            // TFLite expects 48x48 grayscale
            val tfliteInput =
                com.bhushan.android.presentation.camera.utils.PreprocessingUtils.preprocessGray(
                    bitmap,
                    48
                )
            val tfliteOutput = Array(1) { FloatArray(tfliteLabels.size) }
            tfliteInterpreter.run(tfliteInput, tfliteOutput)
            val tfliteIdx = tfliteOutput[0].indices.maxByOrNull { tfliteOutput[0][it] } ?: -1
            val tfliteEmotion = if (tfliteIdx >= 0) tfliteLabels[tfliteIdx] else "Unknown"

            // ONNX expects 224x224 RGB
            val onnxOutput =
                onnxModelHelper.runInference(Bitmap.createScaledBitmap(bitmap, 224, 224, true))
            val onnxIdx = onnxOutput?.indices?.maxByOrNull { onnxOutput[it] } ?: -1
            val onnxEmotion = if (onnxIdx >= 0) onnxLabels[onnxIdx] else "Unknown"

            listener.onEmotionDetected(tfliteEmotion, onnxEmotion)
        } catch (e: Exception) {
            Log.e("DualEmotionAnalyzer", e.message, e)
            listener.onEmotionDetected("Error", "Error")
        } finally {
            image.close()
            working.set(false)
        }
    }
}