package com.example.connectme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UploadStory : AppCompatActivity() {
    private var selectedBitmap: Bitmap? = null
    private var selectedVideoBase64: String? = null
    private var mediaType: String? = null
    private val PICK_MEDIA_REQUEST = 101
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)

        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<ImageView>(R.id.gallery).setOnClickListener {
            openGallery()
        }

        findViewById<ImageView>(R.id.close_button_upload_story).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.next_button_take_picture).setOnClickListener {
            if (mediaType == "image" && selectedBitmap != null) {
                uploadStoryToFirebase(encodeBitmapToBase64(selectedBitmap!!), "image")
            } else if (mediaType == "video" && selectedVideoBase64 != null) {
                uploadStoryToFirebase(selectedVideoBase64!!, "video")
            } else {
                Toast.makeText(this, "Select an image or video first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        startActivityForResult(intent, PICK_MEDIA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST && resultCode == RESULT_OK && data != null) {
            val mediaUri = data.data
            val mimeType = contentResolver.getType(mediaUri!!)

            if (mimeType?.startsWith("image") == true) {
                selectedBitmap = decodeUriToBitmap(mediaUri)
                mediaType = "image"
                findViewById<ImageView>(R.id.imagePreview).apply {
                    setImageBitmap(selectedBitmap)
                    visibility = ImageView.VISIBLE
                }
                findViewById<VideoView>(R.id.videoPreview).visibility = VideoView.GONE
            } else if (mimeType?.startsWith("video") == true) {
                selectedVideoBase64 = encodeVideoToBase64(mediaUri)
                mediaType = "video"
                findViewById<VideoView>(R.id.videoPreview).apply {
                    setVideoURI(mediaUri)
                    visibility = VideoView.VISIBLE
                    start()
                }
                findViewById<ImageView>(R.id.imagePreview).visibility = ImageView.GONE
            }
        }
    }

    private fun decodeUriToBitmap(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun encodeVideoToBase64(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val buffer = ByteArray(1024)
        val outputStream = ByteArrayOutputStream()
        var bytesRead: Int

        while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun uploadStoryToFirebase(encodedMedia: String, type: String) {
        userId?.let { uid ->
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("stories")

            val storyData = hashMapOf(
                "mediaData" to encodedMedia,
                "mediaType" to type,
                "timestamp" to System.currentTimeMillis()
            )

            databaseRef.setValue(storyData).addOnSuccessListener {
                Toast.makeText(this, "Story Uploaded!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } ?: Toast.makeText(this, "Error: User ID is null", Toast.LENGTH_SHORT).show()
    }
}
