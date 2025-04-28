package com.example.connectme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.io.File

class StoryView : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var storyVideoView: VideoView
    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_view)

        userId = intent.getIntExtra("userId", 0)

        if (userId == 0) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        storyImageView = findViewById(R.id.storyFullScreen)
        storyVideoView = findViewById(R.id.storyFullScreenVideo)
        findViewById<ImageView>(R.id.close_button).setOnClickListener {
            finish()
        }

        if (isNetworkAvailable()) {
            loadStoryOnline()
        } else {
            loadStoryOffline()
        }

        autoCloseHandler.postDelayed({ finish() }, 10000)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun loadStoryOnline() {
        val apiUrl = Globals.BASE_URL+"get_story.php?userId=$userId"

        val request = JsonObjectRequest(
            Request.Method.GET, apiUrl, null,
            { response ->
                displayMedia(response.getString("mediaType"), response.getString("mediaData"))
            },
            { error ->
                Toast.makeText(this, "Error fetching story: ${error.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun loadStoryOffline() {
        val dbHelper = dbHelper(this)
        val story = dbHelper.getCachedStoryByUser(userId)

        if (story != null) {
            displayMedia(story.mediaType ?: "", story.mediaData ?: "")
        } else {
            Toast.makeText(this, "No cached story available offline.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayMedia(mediaType: String, mediaData: String) {
        if (mediaType == "image") {
            val bitmap = decodeBase64ToBitmap(mediaData)
            storyImageView.setImageBitmap(bitmap)
            storyImageView.visibility = ImageView.VISIBLE
            storyVideoView.visibility = VideoView.GONE
        } else if (mediaType == "video") {
            val decodedBytes = Base64.decode(mediaData, Base64.DEFAULT)
            val tempFile = File.createTempFile("story_video", ".mp4", cacheDir)
            tempFile.writeBytes(decodedBytes)
            storyVideoView.setVideoURI(Uri.fromFile(tempFile))
            storyVideoView.setOnPreparedListener { it.isLooping = true }
            storyVideoView.visibility = VideoView.VISIBLE
            storyImageView.visibility = ImageView.GONE
            storyVideoView.start()
        }
    }

    private fun decodeBase64ToBitmap(encodedString: String): Bitmap {
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        autoCloseHandler.removeCallbacksAndMessages(null)
    }
}
