package com.example.connectme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StoryView : AppCompatActivity() {

    private lateinit var storyImageView: ImageView
    private lateinit var storyVideoView: VideoView
    private val autoCloseHandler = Handler(Looper.getMainLooper())
    var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_view)

        userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
        }


        storyImageView = findViewById(R.id.storyFullScreen)
        storyVideoView = findViewById(R.id.storyFullScreenVideo)

        findViewById<ImageView>(R.id.close_button).setOnClickListener {
            finish()
        }

        loadStory()

        autoCloseHandler.postDelayed({
            finish()
        }, 10000)
    }

    private fun loadStory() {
        userId?.let { uid ->
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("stories")

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val mediaData = snapshot.child("mediaData").getValue(String::class.java)
                        val mediaType = snapshot.child("mediaType").getValue(String::class.java)

                        if (mediaType == "image") {
                            mediaData?.let {
                                storyImageView.setImageBitmap(decodeBase64ToBitmap(it))
                                storyImageView.visibility = ImageView.VISIBLE
                                storyVideoView.visibility = VideoView.GONE
                            }
                        } else if (mediaType == "video") {
                            mediaData?.let {
                                val videoUri = Uri.parse("data:video/mp4;base64,$it")
                                storyVideoView.setVideoURI(videoUri)
                                storyVideoView.setOnPreparedListener { it.isLooping = true }
                                storyVideoView.visibility = VideoView.VISIBLE
                                storyImageView.visibility = ImageView.GONE
                                storyVideoView.start()
                            }
                        }
                    } else {
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } ?: finish()
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
