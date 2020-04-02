package com.example.fileonfire.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.crashlytics.android.Crashlytics
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun from(context: Context, uri: Uri): Map<File, Long> {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileData: Map<String, Long> = getFileData(context, uri)
    val fileName: String = fileData.keys.first()
    val splitFileName: Array<String> = splitFileName(fileName)
    val tempFile: File = File.createTempFile(splitFileName[0], splitFileName[1])
    val fileOutputStream = FileOutputStream(tempFile)
    val outputData: HashMap<File, Long> = HashMap()
    tempFile.deleteOnExit()

    inputStream?.let {
        copy(inputStream, fileOutputStream)
        inputStream.close()
    }
    fileOutputStream.close()

    // Set the data on the file
    outputData[tempFile] = fileData.values.first()
    return outputData
}

private fun getFileData(context: Context, uri: Uri): Map<String, Long> {
    var fileName: String? = null
    var fileSize = 0L
    val fileData: HashMap<String, Long> = HashMap()
    // Get the current filename
    if (uri.scheme.equals("content")) {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            cursor?.let {
                cursor.moveToFirst()
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                fileSize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE))
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
        } finally {
            cursor?.close()
        }
    }

    // If the previous procedure fails, get the name from the uri path
    if(fileName == null) {
        fileName = uri.path
        fileName?.let {
            val cut = it.lastIndexOf(File.separator)
            // If there is an occurrence, get the name without the first slash
            if (cut != -1) {
                fileName = it.substring(cut + 1)
            }
        }
    }

    // Set the data in a hashmap
    fileData[fileName ?: ""] = fileSize
    return fileData
}

private fun splitFileName(filename: String): Array<String> {
    var name = filename
    var extension = ""
    val lastIndexOf: Int = filename.lastIndexOf(".")
    // If there is an occurrence
    if (lastIndexOf != -1) {
        name = filename.substring(0, lastIndexOf)
        extension = filename.substring(lastIndexOf)
    }
    return arrayOf(name, extension)
}

private fun copy(inputStream: InputStream, fileOutputStream: FileOutputStream) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (EOF != inputStream.read(buffer)) {
        fileOutputStream.write(buffer)
    }
}