package com.example.fileonfire.ui.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fileonfire.util.DEFAULT_HEIGHT
import com.example.fileonfire.util.DEFAULT_QUALITY
import com.example.fileonfire.util.DEFAULT_WIDTH
import com.example.fileonfire.util.WEBP_MIME_TYPE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainViewModel : ViewModel() {

    val file: MutableLiveData<File> by lazy {
        MutableLiveData<File>()
    }

    /**
     * Uses coroutines to get the compressed file
     */
    fun convertFile(currentFile: File) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                file.value = compressImage(currentFile)
            }
        }
    }

    /**
     * Retrieves the compressed file from the [currentFile]
     */
    suspend fun compressImage(currentFile: File): File = withContext(Dispatchers.IO) {
        return@withContext decodeSampledBitmapFromFile(currentFile).run {
            overwriteFile(currentFile, this)
        }
    }

    /**
     * Decodes the bitmap from the passed file.
     * When inJustDecodeBounds = true, it decodes the bitmap to check the original dimensions,
     * then calls [calculateInSampleSize] to calculate inSampleSize, and it changes
     * inJustDecodeBounds = false to decode the bitmap with the inSampleSize set
     */
    private fun decodeSampledBitmapFromFile(
        currentFile: File,
        reqWidth: Int = DEFAULT_WIDTH,
        reqHeight: Int = DEFAULT_HEIGHT
    ): Bitmap {
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(currentFile.absolutePath, this)

            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            inJustDecodeBounds = false
            BitmapFactory.decodeFile(currentFile.absolutePath, this)
        }
    }

    /**
     * With the raw dimensions retrieved from [decodeSampledBitmapFromFile] and stored on outHeight
     * and outWidth, it calculates the largest inSampleSize value that is a power of 2 and keeps
     * both height and width larger than the requested height and width
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2


            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Overwrites the current file [currentFile] by using a WEBP mime type [format] by default because it can support
     * both lossy and lossless modes, making it an ideal replacement for both PNG and JPG.
     * It calls [saveBitmap] with [resultBitmap] and the default [quality] too
     */
    private fun overwriteFile(
        currentFile: File,
        resultBitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.WEBP,
        quality: Int = DEFAULT_QUALITY
    ): File {
        val resultFile = if (currentFile.extension == WEBP_MIME_TYPE) {
            currentFile
        } else {
            File("${currentFile.absolutePath.substringBeforeLast(".")}.${WEBP_MIME_TYPE}")
        }
        currentFile.delete()
        saveBitmap(resultBitmap, resultFile, format, quality)
        return resultFile
    }

    /**
     * Saves the bitmap by using [resultBitmap], [resultFile], [format], and its [quality]
     */
    private fun saveBitmap(
        resultBitmap: Bitmap,
        resultFile: File,
        format: Bitmap.CompressFormat,
        quality: Int
    ) {
        resultFile.parentFile?.mkdirs()
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(resultFile.absolutePath)
            resultBitmap.compress(format, quality, fileOutputStream)
        } finally {
            fileOutputStream?.run {
                flush()
                close()
            }
        }
    }

}
