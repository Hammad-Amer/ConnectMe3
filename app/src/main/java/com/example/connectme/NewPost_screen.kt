package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPost_screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post_screen)


        val newpost_back = findViewById<ImageView>(R.id.back_icon_cross)
        newpost_back.setOnClickListener {
            val intent = Intent(this, NewPost::class.java)
            startActivity(intent)
        }

        val share = findViewById<Button>(R.id.share_button)
        share.setOnClickListener {
            val intent = Intent(this, MainFeedScreen::class.java)
            startActivity(intent)
        }
    }
}