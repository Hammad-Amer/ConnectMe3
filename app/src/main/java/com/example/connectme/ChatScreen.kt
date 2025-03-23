package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_screen)

        val messagesList = mutableListOf<ModelChat>()
        messagesList.add(ModelChat("What are you doing?", "10:03 AM", false, R.drawable.pf5))
        messagesList.add(ModelChat("I am good too. Thanks for asking.", "10:02 AM", true))
        messagesList.add(ModelChat("I am fine. How about you?", "10:01 AM", false, R.drawable.pf5))
        messagesList.add(ModelChat("Hello! How are you?", "10:00 AM", true))

        val rv4 = findViewById<RecyclerView>(R.id.recyclerViewMessages_chat)
        rv4.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
        }
        rv4.adapter = AdapterChatMessage(messagesList)

        // Back button
        val backtomaindms = findViewById<ImageView>(R.id.BackButton_chat)
        backtomaindms.setOnClickListener {
            startActivity(Intent(this, DMs::class.java))
        }

        // Voice call icon
        val gotocall= findViewById<ImageView>(R.id.Call_chat)
        gotocall.setOnClickListener {
            startActivity(Intent(this, Voicecall::class.java))
        }

        // Video call icon -> triggers VideoCall activity
        val gotovideo= findViewById<ImageView>(R.id.VideoCall_chat)
        gotovideo.setOnClickListener {
            // Simply navigate to VideoCall
            val intent = Intent(this, VideoCall::class.java)
            startActivity(intent)
        }

        // Vanish mode (long press)
        val sendButton = findViewById<ImageView>(R.id.btnSend_vanish)
        sendButton.setOnLongClickListener {
            startActivity(Intent(this, ChatScreen_Vanish::class.java))
            true
        }
    }
}
