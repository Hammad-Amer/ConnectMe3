package com.example.connectme

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest

class NewPost : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var imageAdapter: AdapterNewPost
    private val imageUris = mutableListOf<Uri>()
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)

        gridView = findViewById(R.id.new_post_grid)
        val mainImage: ImageView = findViewById(R.id.main_image)

        checkAndRequestPermissions()

        var close = findViewById<ImageView>(R.id.cross_go_backto_feed)
        close.setOnClickListener {
            finish()
        }


        gridView.setOnItemClickListener { _, _, position, _ ->
            selectedImageUri = imageUris[position]
            mainImage.setImageURI(selectedImageUri)
        }

        findViewById<TextView>(R.id.next_newpost).setOnClickListener {
            if (selectedImageUri != null) {
                val intent = Intent(this, NewPost_screen::class.java)
                intent.putExtra("imageUri", selectedImageUri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty() && permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
        } else {
            loadGalleryImages()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                loadGalleryImages()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadGalleryImages() {

        Toast.makeText(this, "Showing gallery", Toast.LENGTH_SHORT).show()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val imageId = it.getLong(columnIndex)
                val imageUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId.toString())
                imageUris.add(imageUri)
            }
        }

        imageAdapter = AdapterNewPost(this, imageUris)
        gridView.adapter = imageAdapter
        imageAdapter.notifyDataSetChanged()
    }
}