package mx.com.infotecno.zebracardprinter.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.IOException

import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils


object UriHelper {
    fun getFilename(context: Context, uri: Uri?): String? {
        var result: String? = null
        if (uri != null) {
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
        }
        return result
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val filename = getFilename(context, uri)
        return FilenameUtils.getExtension(filename)
    }

    fun isXmlFile(context: Context, uri: Uri): Boolean {
        val extension = getFileExtension(context, uri)
        return extension != null && extension.toLowerCase() == "xml"
    }

    @Throws(IOException::class)
    fun getByteArrayFromUri(context: Context, uri: Uri?): ByteArray? {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri!!)
        return if (inputStream != null) {
            try {
                IOUtils.toByteArray(inputStream)
            } finally {
                IOUtils.closeQuietly(inputStream)
            }
        } else null
    }
}