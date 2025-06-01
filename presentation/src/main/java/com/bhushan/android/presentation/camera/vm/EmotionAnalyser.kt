package com.bhushan.android.presentation.camera.vm

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bhushan.android.domain.ml.model.EmotionResult
import com.bhushan.android.domain.ml.model.ImageData
import com.bhushan.android.domain.ml.usecase.DetectEmotionUseCase
import com.bhushan.android.presentation.camera.utils.YuvToRgbConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

interface DualEmotionListener {
    fun onEmotionDetected(result: EmotionResult)
}

class DualEmotionAnalyzer(
    private val detectEmotionUseCase: DetectEmotionUseCase,
    private val listener: DualEmotionListener,
    private val externalScope: CoroutineScope
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)

    override fun analyze(image: ImageProxy) {
        if (!isProcessing.compareAndSet(false, true)) {
            image.close()
            return
        }
        externalScope.launch {
            try {
                // Convert ImageProxy to Bitmap
                val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                YuvToRgbConverter.yuvToRgb(image, bitmap)

                // Preprocess for domain (grayscale or RGB as needed)
                val tfLiteImageData = preprocessBitmapToImageDataForTfLite(bitmap)
                val onnxImageData = preprocessBitmapToImageDataForOnnx(bitmap)
                val result = detectEmotionUseCase(tfLiteImageData, onnxImageData)

                // Run domain use case in background
                withContext(Dispatchers.Main) {
                    listener.onEmotionDetected(result)
                }
            } catch (e: Exception) {
                Log.e("DualEmotionAnalyzer", e.message, e)
                listener.onEmotionDetected(EmotionResult("Error", "Error"))
            } finally {
                image.close()
                isProcessing.set(false)
            }
        }
    }

    private fun preprocessBitmapToImageDataForTfLite(bitmap: Bitmap): ImageData {
        // Example: for TFLite, use grayscale 48x48; for ONNX, use RGB 224x224
        // You can generalize or make this configurable
        val size = 48
        val resized = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val pixels = FloatArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val color = resized.getPixel(x, y)
                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF
                pixels[y * size + x] = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
            }
        }
        return ImageData(pixels, size, size, ImageData.Format.GRAYSCALE)
    }

    private fun preprocessBitmapToImageDataForOnnx(bitmap: Bitmap): ImageData {
        val width = 224
        val height = 224
        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val pixels = FloatArray(width * height * 3)
        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = resized.getPixel(x, y)
                val r = ((color shr 16) and 0xFF) / 255.0f
                val g = ((color shr 8) and 0xFF) / 255.0f
                val b = (color and 0xFF) / 255.0f
                pixels[idx++] = r
                pixels[idx++] = g
                pixels[idx++] = b
            }
        }
        return ImageData(pixels, width, height, ImageData.Format.RGB)
    }

}