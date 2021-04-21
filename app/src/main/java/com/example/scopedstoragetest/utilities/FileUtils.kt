package com.example.scopedstoragetest.utilities

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.example.scopedstoragetest.FILE_NAME_NOT_FOUND

class FileUtils {

    companion object {

        fun makeFile(contentResolver: ContentResolver, directory: DocumentFile?, fileName: String) {
            directory?.findFile(fileName)
                ?: directory?.createFile("text/plain", fileName)?.also {
                    contentResolver.openOutputStream(it.uri).use { out ->
                        val text = "Hello world!"
                        out?.write(text.toByteArray())
                    }
                }
        }

        fun createDirectory(
            directory: DocumentFile?,
            dirName: String
        ) {
            directory?.findFile(dirName)
                ?: directory?.createDirectory(dirName)
        }

        // Get the name of a file represented by a DocumentFile object
        fun getFileName(contentResolver: ContentResolver, docFile: DocumentFile): String {

            // Query the doc, get and return its display name
            contentResolver.query(
                docFile.uri,
                arrayOf(MediaStore.Audio.AudioColumns.DISPLAY_NAME),
                null,
                null,
                null
            ).use {
                it?.moveToFirst()
                return it?.getString(0) ?: FILE_NAME_NOT_FOUND
            }

        }


    }
}