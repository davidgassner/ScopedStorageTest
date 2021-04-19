package com.example.scopedstoragetest

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.example.scopedstoragetest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val getAccessRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            val uri: Uri? = result.data?.data

            Log.d(LOG_TAG, "URI = $uri!!")

            val directoryDocFile = DocumentFile.fromTreeUri(applicationContext, uri!!)
            Log.d(LOG_TAG, getFileName(directoryDocFile!!))
            val fileList = directoryDocFile.listFiles()

            for (docFile in fileList) {
                Log.d(LOG_TAG, getFileName(docFile))
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.myButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                getAccess()
            }
        }
    }

    private fun getFileName(docFile: DocumentFile): String {
        val cursor = this.contentResolver.query(
            docFile.uri,
            arrayOf(MediaStore.Audio.AudioColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        val fileName = cursor?.getString(0) ?: "Name not found"
        cursor?.close()
        return fileName
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getAccess() {

//        val storageManager = getSystemService(StorageManager::class.java)
//        val volumes = storageManager.storageVolumes
//        for (volume in volumes) {
//            Log.i(LOG_TAG, "$volume, removable=${volume.isRemovable}")
//        }

        // You can specify initial folder using intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).also {
            it.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            getAccessRequest.launch(it)
        }
    }

}