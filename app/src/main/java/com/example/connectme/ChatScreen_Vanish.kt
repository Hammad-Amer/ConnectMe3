package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChatScreen_Vanish : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_screen_vanish)

        val backtomaindms= findViewById<ImageView>(R.id.BackButton_chat)
        backtomaindms.setOnClickListener {
            val intent = Intent(this,DMs::class.java)
            startActivity(intent)
        }

        val gotocall= findViewById<ImageView>(R.id.Call_chat)
        gotocall.setOnClickListener {
            val intent = Intent(this,Voicecall::class.java)
            startActivity(intent)
        }

        val gotovideo= findViewById<ImageView>(R.id.VideoCall_chat)
        gotovideo.setOnClickListener {
            val intent = Intent(this,VideoCall::class.java)
            startActivity(intent)
        }


        val sendButton = findViewById<ImageView>(R.id.btnSend_vanish)
        sendButton.setOnLongClickListener {

            val intent = Intent(this, ChatScreen::class.java)
            startActivity(intent)
            true
        }

    }
}