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
fun from(context: Context, uri: Uri): Pair<File, String> {
    val fileData: Pair<String, Long> = getFileData(context, uri)
    val splitFileName: Pair<String, String> = splitFileName(fileData.first)
    val tempFile: File = File.createTempFile(splitFileName.first, splitFileName.second)
    tempFile.deleteOnExit()

    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

    inputStream?.let {
        val fileOutputStream = FileOutputStream(tempFile)
        copy(it, fileOutputStream)
        it.close()
        fileOutputStream.close()
    }
    return Pair(tempFile, getHumanReadableSize(fileData.second, context.resources, R.string.actual_size))
}

/**
 * This method gets the current file name and its size in bytes by using the [context] and the [uri] for the cursor
 */
private fun getFileData(context: Context, uri: Uri): Pair<String, Long> {
    var fileName = ""
    var fileSize = 0L

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
    return Pair(fileName, fileSize)
}

/**
 * This method splits the file name to get its name and file extension if the [filename] has data
 */
private fun splitFileName(filename: String): Pair<String, String> {
    var name = filename
    var extension = ""
    val lastIndexOf: Int = filename.lastIndexOf(".")
    // If there is an occurrence
    if (lastIndexOf != -1) {
        name = filename.substring(0, lastIndexOf)
        extension = filename.substring(lastIndexOf)
    }
    return Pair(name, extension)
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
 * This method gets the [numberOfBytes] to parse it to be human readable by using the [stringResource]
 * from [resources].
 * The counter variable helps to select the correct size, so it starts by one because the minimum
 * value bigger than Bytes is KB
 * Source: https://programming.guide/java/formatting-byte-size-to-human-readable-format.html
 */
fun getHumanReadableSize(numberOfBytes: Long, resources: Resources, stringResource: Int): String {
    return if (-(ONE_MB) < numberOfBytes && numberOfBytes < ONE_MB) {
        String.format(resources.getString(stringResource), numberOfBytes, Size.Bytes.name)
    } else {
        var nob: Long = numberOfBytes
        var counter = 1
        while (nob <= -(MAX_NUMBER) || nob >= MAX_NUMBER) {
            nob /= ONE_MB
            counter++
        }
        String.format(
            resources.getString(stringResource),
            nob / ONE_MB.toDouble(),
            Size.values()[counter].name
        )
    }
}