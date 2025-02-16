package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Voicecall : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voicecall)


        val gotochat = findViewById<ImageView>(R.id.endcall_callscreen)
        gotochat.setOnClickListener {
            val intent = Intent(this, ChatScreen::class.java)
            startActivity(intent)
        }

        val gotovideo = findViewById<ImageView>(R.id.videocall_callscreen)
        gotovideo.setOnClickListener {
            val intent = Intent(this, VideoCall::class.java)
            startActivity(intent)
        }
    }
}