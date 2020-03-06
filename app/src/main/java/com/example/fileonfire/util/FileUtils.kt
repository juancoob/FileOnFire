package com.example.fileonfire.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.crashlytics.android.Crashlytics
import java.io.File
import java.io.InputStream

fun from(context: Context, uri: Uri) {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val fileName: String? = getFileName(context, uri)
    fileName?.let {
        val splitFileName: Array<String> = splitFileName(it)

    }
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
            Crashlytics.log(e.toString())
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
    if(lastIndexOf != -1) {
        name = filename.substring(0, lastIndexOf)
        extension = filename.substring(lastIndexOf)
    }
    return arrayOf(name, extension)
}