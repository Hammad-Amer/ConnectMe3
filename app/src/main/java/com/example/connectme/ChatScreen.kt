package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_screen)


        val MessagesList = mutableListOf<ModelChat>()
        MessagesList.add(ModelChat("What are you doing?", "10:03 AM", false, R.drawable.pf5))
        MessagesList.add(ModelChat("I am good too. Thanks for asking.", "10:02 AM", true))

        MessagesList.add(ModelChat("I am fine. How about you?", "10:01 AM", false, R.drawable.pf5))
        MessagesList.add(ModelChat("Hello! How are you?", "10:00 AM", true))


        val rv4 = findViewById<RecyclerView>(R.id.recyclerViewMessages_chat)
        rv4.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
        }
        rv4.adapter = AdapterChatMessage(MessagesList)

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

            val intent = Intent(this, ChatScreen_Vanish::class.java)
            startActivity(intent)
            true
        }

    }
}