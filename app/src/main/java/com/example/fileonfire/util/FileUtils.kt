package com.example.fileonfire.util

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.crashlytics.android.Crashlytics
import com.example.fileonfire.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

enum class Size {
    Bytes, KB, MB, GB
}

/**
 * This method gets the file data with [context] and [uri] to copy it on a different file
 */
fun from(context: Context, uri: Uri): Map<File, String> {
    val fileData: Map<String, Long> = getFileData(context, uri)
    val fileName: String = fileData.keys.first()

    val splitFileName: Array<String> = splitFileName(fileName)
    val tempFile: File = File.createTempFile(splitFileName[0], splitFileName[1])
    tempFile.deleteOnExit()

    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

    val outputData: HashMap<File, String> = HashMap()

    inputStream?.let {
        val fileOutputStream = FileOutputStream(tempFile)
        copy(it, fileOutputStream)
        it.close()
        fileOutputStream.close()
    }
    outputData[tempFile] = getHumanReadableSize(fileData.values.first(), context.resources)
    return outputData
}

/**
 * This method gets the current file name and its size in bytes by using the [context] and the [uri] for the cursor
 */
private fun getFileData(context: Context, uri: Uri): Map<String, Long> {
    var fileName = ""
    var fileSize = 0L
    val fileData: HashMap<String, Long> = HashMap()

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
    fileData[fileName] = fileSize
    return fileData
}

/**
 * This method splits the file name to get its name and file extension if the [filename] has data
 */
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

/**
 * This method copy the file with [inputStream] to the temp file by using [fileOutputStream] to create it
 */
private fun copy(inputStream: InputStream, fileOutputStream: FileOutputStream) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (EOF != inputStream.read(buffer)) {
        fileOutputStream.write(buffer)
    }
}

/**
 * This method gets the [numberOfBytes] to parse it to be human readable by using [resources].
 * The counter variable helps to select the correct size, so it starts by one because the minimum
 * value bigger than Bytes is KB
 * Source: https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
 */
fun getHumanReadableSize(numberOfBytes: Long, resources: Resources): String {
    return if (-(ONE_MB) < numberOfBytes && numberOfBytes < ONE_MB) {
        String.format(resources.getString(R.string.actual_size), numberOfBytes, Size.Bytes.name)
    } else {
        var nob: Long = numberOfBytes
        var counter = 1
        while (nob <= -(MAX_NUMBER) || nob >= MAX_NUMBER) {
            nob /= ONE_MB
            counter++
        }
        String.format(resources.getString(R.string.actual_size), nob / ONE_MB.toDouble(), Size.values()[counter].name)
    }
}