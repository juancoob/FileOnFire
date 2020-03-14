package com.example.fileonfire.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.crashlytics.android.Crashlytics
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun from(context: Context, uri: Uri): File? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileName: String = getFileName(context, uri) ?: return null //todo get correct filename
    val splitFileName: Array<String> = splitFileName(fileName)
    val tempFile: File = File.createTempFile(splitFileName[0], splitFileName[1])
    lateinit var outputStream: FileOutputStream
    if (fileName == tempFile.name) {
        tempFile.deleteOnExit()
        outputStream = FileOutputStream(tempFile)
    }
    inputStream?.let {
        copy(inputStream, outputStream)
        inputStream.close()
    }
    outputStream.close()
    return tempFile
}

private fun getFileName(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme.equals("content")) {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            cursor?.let {
                cursor.moveToFirst()
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
        } finally {
            cursor?.close()
        }
    }
    result.let {
        result = uri.path
        result?.let {
            val cut = it.lastIndexOf(File.separator)
            if (cut != -1) {
                return it.substring(cut + 1)
            }
        }
    }
    return result
}

private fun splitFileName(filename: String): Array<String> {
    var name = filename
    var extension = ""
    val lastIndexOf: Int = filename.lastIndexOf(".")
    if (lastIndexOf != -1) {
        name = filename.substring(0, lastIndexOf)
        extension = filename.substring(lastIndexOf)
    }
    return arrayOf(name, extension)
}

private fun copy(inputStream: InputStream, outputStream: FileOutputStream) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (EOF != inputStream.read(buffer)) {
        outputStream.write(buffer)
    }
}