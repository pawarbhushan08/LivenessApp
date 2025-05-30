package com.bhushan.android.presentation.camera.vm


import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bhushan.android.presentation.camera.utils.PreprocessingUtils.preprocessGray
import com.bhushan.android.presentation.camera.utils.YuvToRgbConverter
import org.tensorflow.lite.Interpreter
import java.util.concurrent.atomic.AtomicBoolean

interface EmotionListener {
    fun onEmotionDetected(emotion: String)
}

class EmotionAnalyzer(
    private val interpreter: Interpreter,
    private val emotionListener: EmotionListener
) : ImageAnalysis.Analyzer {

    private val inputSize: Int = 48
    private val labels: List<String> =
        listOf("Angry", "Disgust", "Fear", "Happy", "Sad", "Surprise", "Neutral")

    private val working = AtomicBoolean(false)

    override fun analyze(image: ImageProxy) {
        if (working.getAndSet(true)) {
            image.close()
            return
        }
        try {
            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            YuvToRgbConverter.yuvToRgb(image, bitmap)
            val input = preprocessGray(bitmap, inputSize)
            val output = Array(1) { FloatArray(7) }
            interpreter.run(input, output)
            val emotionIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            val emotion = if (emotionIdx >= 0) labels[emotionIdx] else "Unknown"
            emotionListener.onEmotionDetected(emotion)
        } catch (e: Exception) {
            Log.e("EmotionAnalyzerException", e.message, e)
            emotionListener.onEmotionDetected("Error")
        } finally {
            image.close()
            working.set(false)
        }
    }
}