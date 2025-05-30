package com.bhushan.android.presentation.camera.utils.assets

import android.content.Context
import java.io.File
import java.io.FileOutputStream

interface AssetRepository {
    fun copyAssetToFile(assetName: String): String
}

class AssetRepositoryImpl(private val context: Context) : AssetRepository {
    /**
     * Copies the given asset to app's files directory and returns its absolute path.
     * If the file already exists, does nothing.
     */
    override fun copyAssetToFile(assetName: String): String {
        val outFile = File(context.filesDir, assetName)
        if (!outFile.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return outFile.absolutePath
    }
}