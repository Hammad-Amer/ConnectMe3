package com.example.connectme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import java.io.ByteArrayOutputStream
import java.io.InputStream

class NewPost_screen : AppCompatActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var postImageView: ImageView
    private lateinit var captionEditText: EditText

    val postUrl = Globals.BASE_URL+"insertpost.php"


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
        backButton.setOnClickListener {
            finish()
        }

        val shareButton = findViewById<Button>(R.id.share_button)
        shareButton.setOnClickListener {
            uploadPostToServer()
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun uploadPostToServer() {
        val caption = captionEditText.text.toString().trim()
        val bitmap = selectedImageUri?.let { uriToBitmap(it) }

        if (caption.isEmpty() || bitmap == null) {
            Toast.makeText(this, "Caption or image missing!", Toast.LENGTH_SHORT).show()
            return
        }

        val base64Image = bitmapToBase64(bitmap)

        val sharedPrefs = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPrefs.getInt("userId", -1)


        if (userId == -1) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show()
            return
        }

        val stringRequest = object : StringRequest(
            Method.POST, postUrl,
            Response.Listener { response ->
                if (response.contains("success")) {
                    Toast.makeText(this, "Post Uploaded!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
                    Log.e("NewPost", "Server response: $response")
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Network error!", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["caption"] = caption
                params["image_base64"] = base64Image
                return params
            }
        }

        Volley.newRequestQueue(this).add(stringRequest)
    }
}