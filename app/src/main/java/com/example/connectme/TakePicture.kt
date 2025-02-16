package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TakePicture : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_take_picture)


        val gotonewpost = findViewById<ImageView>(R.id.close_button_take_picture)
        gotonewpost.setOnClickListener {
            val intent = Intent(this, NewPost::class.java)
            startActivity(intent)
        }

        val gotonext = findViewById<TextView>(R.id.next_button_take_picture)
        gotonext.setOnClickListener {
            val intent = Intent(this, NewPost_screen::class.java)
            startActivity(intent)
        }
    }
}