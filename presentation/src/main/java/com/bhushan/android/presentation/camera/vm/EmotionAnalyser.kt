package com.bhushan.android.presentation.camera.vm

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import com.bhushan.android.domain.ml.model.EmotionResult
import com.bhushan.android.domain.ml.model.ImageData
import com.bhushan.android.domain.ml.usecase.DetectEmotionUseCase
import com.bhushan.android.presentation.camera.utils.YuvToRgbConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface DualEmotionListener {
    fun onEmotionDetected(result: EmotionResult)
}

class DualEmotionAnalyzer(
    private val detectEmotionUseCase: DetectEmotionUseCase,
    private val listener: DualEmotionListener,
    private val externalScope: CoroutineScope,
    private val selectedModel: ModelType
) : ImageAnalysis.Analyzer {


    override fun analyze(image: ImageProxy) {
        externalScope.launch {
            try {
                // Convert ImageProxy to Bitmap
                val bitmap = createBitmap(image.width, image.height)
                YuvToRgbConverter.yuvToRgb(image, bitmap)

                val result = when (selectedModel) {
                    ModelType.TFLITE -> {
                        val tfLiteImageData = preprocessBitmapToImageDataForTfLite(bitmap)
                        detectEmotionUseCase(tfLiteImageData, selectedModel.mapToMLModelType())
                    }

                    ModelType.ONNX -> {
                        val onnxImageData = preprocessBitmapToImageDataForOnnx(bitmap)
                        detectEmotionUseCase(onnxImageData, selectedModel.mapToMLModelType())
                    }
                }

                withContext(Dispatchers.Main) {
                    listener.onEmotionDetected(result)
                }
            } catch (e: Exception) {
                Log.e("DualEmotionAnalyzer", e.message, e)
                listener.onEmotionDetected(EmotionResult("Error"))
            } finally {
                image.close()
            }
        }
    }

    private fun preprocessBitmapToImageDataForTfLite(bitmap: Bitmap): ImageData {
        // Example: for TFLite, use grayscale 48x48; for ONNX, use RGB 224x224
        // You can generalize or make this configurable
        val size = 48
        val resized = bitmap.scale(size, size)
        val pixels = FloatArray(size * size)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val color = resized[x, y]
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
        val resized = bitmap.scale(width, height)
        val pixels = FloatArray(width * height * 3)
        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = resized[x, y]
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