package com.example.connectme

import android.os.Bundle
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

    }
}