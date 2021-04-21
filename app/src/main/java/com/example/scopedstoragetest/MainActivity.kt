package com.example.scopedstoragetest

import android.content.Context.*
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.scopedstoragetest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var appStorageDir: DocumentFile? = null
    private lateinit var binding: ActivityMainBinding

    private val storageDirRequest =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->

            val uri = result.data?.data ?: return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            appStorageDir = DocumentFile.fromTreeUri(applicationContext, uri)

//          debug code
//            Log.d(LOG_TAG, "Selected Uri: $uri")
//            Log.d(LOG_TAG, "Directory name: ${getFileName(directory!!)}")

            val fileList = appStorageDir?.listFiles() ?: emptyArray()

            for (docFile in fileList) {
                Log.d(LOG_TAG, getFileName(docFile))
            }

        }

    private val importFileRequest =
        registerForActivityResult(StartActivityForResult()) {

            val sourceUri = it.data?.data ?: return@registerForActivityResult
            contentResolver.openInputStream(sourceUri).use { input ->

                Log.d(LOG_TAG, "URI: $sourceUri")

                // Get a DocumentFile object from the uri
                val sourceDoc = DocumentFile.fromSingleUri(applicationContext, sourceUri)
                    ?: return@registerForActivityResult

                // If file already exists, delete it
                val fileName = getFileName(sourceDoc)
                appStorageDir?.findFile(fileName)?.delete()

                val mimeType = sourceDoc.type ?: "audio/*"
                Log.d(LOG_TAG, "Mime type: $mimeType")
                val newFile = appStorageDir?.createFile(mimeType, fileName)
                    ?: return@registerForActivityResult

                Log.d(LOG_TAG, "Start copy")
                contentResolver.openOutputStream(newFile.uri)?.use { output ->
                    input?.copyTo(output)
                }
            }
            Log.d(LOG_TAG, "End copy")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up binding, layout and event handlers
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            chooseDirButton.setOnClickListener { requestStoragePermission() }
            createFileButton.setOnClickListener { createFile() }
            createDirButton.setOnClickListener { makeDir() }
            importFileButton.setOnClickListener { importFile() }
        }

        val permissions = contentResolver.persistedUriPermissions
        if (permissions.isNotEmpty()) {
            val storageUri = permissions[0].uri
            Log.d(LOG_TAG, "Is read? ${permissions[0].isReadPermission}")
            Log.d(LOG_TAG, "Is write? ${permissions[0].isWritePermission}")
            appStorageDir = DocumentFile.fromTreeUri(this, storageUri)
        }

    }

    private fun importFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("audio/*")
        importFileRequest.launch(intent)
    }

    private fun makeDir() {
        appStorageDir?.createDirectory("aSubDirectory")
    }

    private fun createFile() {
        val fileName = "myFile.txt"
        val existingFile = appStorageDir?.findFile(fileName)
        if (existingFile == null) {
            appStorageDir?.createFile("text/plain", fileName)?.also {
                contentResolver.openOutputStream(it.uri).use { out ->
                    val text = "Hello world!"
                    out?.write(text.toByteArray())
                }
            }
        }

    }

    private fun getFileName(docFile: DocumentFile): String {

        // Query the doc, get and return its display name
        this.contentResolver.query(
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

    private fun requestStoragePermission() {

//        val storageManager = getSystemService(StorageManager::class.java)
//        val volumes = storageManager.storageVolumes
//        for (volume in volumes) {
//            Log.i(LOG_TAG, "$volume, removable=${volume.isRemovable}")
//        }

        // Specify initial folder with intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).also {
            it.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
            storageDirRequest.launch(it)
        }
    }

}