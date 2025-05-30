package com.bhushan.android.presentation.camera.utils.analyser

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.bhushan.android.nativelib.NativeLib
import com.bhushan.android.presentation.camera.utils.assets.AssetRepository

interface BlinkListener {
    fun onBlinkDetected(isBlinking: Boolean)
}

class BlinkAnalyzer(
    val blinkListener: BlinkListener,
    assetRepository: AssetRepository
) : ImageAnalysis.Analyzer {
    private val faceCascade by lazy {
        assetRepository.copyAssetToFile("haarcascade_frontalface_default.xml")
    }
    private val eyeCascade by lazy {
        assetRepository.copyAssetToFile("haarcascade_eye_tree_eyeglasses.xml")
    }

    override fun analyze(image: ImageProxy) {
        val yPlane = image.planes[0].buffer
        val yBytes = ByteArray(yPlane.remaining())
        yPlane.get(yBytes)
        val isSleeping = try {
            NativeLib.nativeDetectBlink(yBytes, image.width, image.height, faceCascade, eyeCascade)
        } catch (e: Exception) {
            Log.e("BlinkAnalyzer", "JNI blink detection error", e)
            false
        }
        blinkListener.onBlinkDetected(isSleeping)
        image.close()
    }
}