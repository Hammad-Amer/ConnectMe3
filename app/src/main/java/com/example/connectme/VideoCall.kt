package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class VideoCall : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_call)

        val gotochat = findViewById<ImageView>(R.id.endcall_videocallscreen)
        gotochat.setOnClickListener {
            val intent = Intent(this, ChatScreen::class.java)
            startActivity(intent)
        }

        val gotovideo = findViewById<ImageView>(R.id.videocall_videocallscreen)
        gotovideo.setOnClickListener {
            val intent = Intent(this, Voicecall::class.java)
            startActivity(intent)
        }
    }
}