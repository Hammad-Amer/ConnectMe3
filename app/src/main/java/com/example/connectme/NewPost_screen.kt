package com.example.connectme

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream


class NewPost_screen : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var postImageView: ImageView
    private lateinit var captionEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post_screen)

        postImageView = findViewById(R.id.post_image)
        captionEditText = findViewById(R.id.caption_input)

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            selectedImageUri = Uri.parse(imageUriString)
            postImageView.setImageURI(selectedImageUri)
        }

        val backButton = findViewById<ImageView>(R.id.back_icon_cross)
        backButton.setOnClickListener() {
            finish()
        }

        val shareButton = findViewById<Button>(R.id.share_button)
        shareButton.setOnClickListener {
            uploadPostToFirebase()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun uploadPostToFirebase() {
        val caption = captionEditText.text.toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null && selectedImageUri != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("posts")

            val bitmap = uriToBitmap(selectedImageUri!!)
            if (bitmap != null) {
                val base64Image = bitmapToBase64(bitmap)

                val postData = hashMapOf(
                    "imageBase64" to base64Image,
                    "caption" to caption,
                    "timestamp" to System.currentTimeMillis()
                )

                databaseRef.push().setValue(postData).addOnSuccessListener {
                    Toast.makeText(this, "Post Uploaded!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                Toast.makeText(this, "Failed to process image!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Failed to upload post!", Toast.LENGTH_SHORT).show()
        }
    }
}
