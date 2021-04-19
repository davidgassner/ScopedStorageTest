package com.example.scopedstoragetest

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.scopedstoragetest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var directory: DocumentFile? = null

    private val storageDirRequest =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->

            val uri = result.data?.data ?: return@registerForActivityResult
            directory = DocumentFile.fromTreeUri(applicationContext, uri)

//          debug code
//            Log.d(LOG_TAG, "Selected Uri: $uri")
//            Log.d(LOG_TAG, "Directory name: ${getFileName(directory!!)}")

            val fileList = directory?.listFiles() ?: emptyArray()

            for (docFile in fileList) {
                Log.d(LOG_TAG, getFileName(docFile))
            }

        }

    private val importFileRequest =
        registerForActivityResult(GetContent()) {

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            chooseDirButton.setOnClickListener { requestStoragePermission() }
            createFileButton.setOnClickListener { createFile() }
            createDirButton.setOnClickListener { makeDir() }
            importFileButton.setOnClickListener { importFile() }
        }
    }

    private fun importFile() {
        TODO("Not yet implemented")
    }

    private fun makeDir() {
        directory?.createDirectory("aSubDirectory")
    }

    private fun createFile() {
        directory?.createFile("text/plain", "myfile.txt").also {
            contentResolver.openOutputStream(it!!.uri).use { out ->
                val text = "Hello world!"
                out?.write(text.toByteArray())
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
            it.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            storageDirRequest.launch(it)
        }
    }

}