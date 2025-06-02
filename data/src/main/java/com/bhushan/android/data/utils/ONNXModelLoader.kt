package com.bhushan.android.data.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

object ONNXModelLoader {
    /**
     * Copies the ONNX model from assets to internal storage and returns the absolute file path.
     * This is necessary because ONNX Runtime requires a file path.
     */
    fun loadModelFile(context: Context, filename: String): String {
        val file = File(context.filesDir, filename)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(filename).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return file.absolutePath
    }
}