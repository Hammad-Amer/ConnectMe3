package com.example.connectme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class UploadStory : AppCompatActivity() {
    private var selectedBitmap: Bitmap? = null
    private var selectedVideoBase64: String? = null
    private var mediaType: String? = null
    private val PICK_MEDIA_REQUEST = 101
    var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_story)


        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        userId = sharedPref.getInt("userId", 0)

        if (userId == 0) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (userId == 0) {
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
                val base64Image = encodeBitmapToBase64(selectedBitmap!!)
                uploadStoryToServer(base64Image, "image")
            } else if (mediaType == "video" && selectedVideoBase64 != null) {
                uploadStoryToServer(selectedVideoBase64!!, "video")
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


    private fun uploadStoryToServer(mediaBase64: String, mediaType: String) {
        val url = Globals.BASE_URL+"uploadstories.php"  // Replace with your actual endpoint
        val requestQueue = Volley.newRequestQueue(this)

        val postRequest = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getBoolean("success")) {
                        Toast.makeText(this, "Story uploaded!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Upload failed: ${jsonResponse.getString("message")}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Response parse error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UploadStory", "Response parse error: ${e.message}", e)
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["userId"] = userId.toString()
                params["mediaType"] = mediaType
                params["mediaData"] = mediaBase64
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        requestQueue.add(postRequest)
    }
}
