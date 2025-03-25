package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.material.imageview.ShapeableImageView

class ChatScreen_Vanish : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var adapter: AdapterChatMessageVanish
    private lateinit var usernameTextView: TextView
    private val chatMessages = mutableListOf<ModelChat>()

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val database = FirebaseDatabase.getInstance().reference
    private val receiverUserId: String by lazy {
        intent.getStringExtra("USER_ID") ?: "" // Get the receiver ID from intent
    }

    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen_vanish)

        recyclerView = findViewById(R.id.recyclerViewMessages_chat)
        etMessage = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend_vanish)
        usernameTextView = findViewById(R.id.Username_chat)

        adapter = AdapterChatMessageVanish(chatMessages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        chatRoomId = generateChatRoomId(currentUser?.uid ?: "", receiverUserId)

        fetchReceiverUsername()
        sendButton.setOnClickListener {
            sendMessage()
        }



        listenForMessages()

        val backButtonchat = findViewById<ImageView>(R.id.BackButton_chat)
        backButtonchat.setOnClickListener {
            val intent = Intent(this, DMs::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchReceiverUsername() {
        val usersRef = database.child("Users").child(receiverUserId)
        val profileImageView = findViewById<ShapeableImageView>(R.id.Main_profile_pic_chat)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 1. Get username
                val username = snapshot.child("username").value?.toString() ?: "Unknown"
                usernameTextView.text = username

                // 2. Get profileImage (Base64-encoded string)
                val base64String = snapshot.child("profileImage").value?.toString()

                // 3. Decode Base64 and set image
                if (!base64String.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        profileImageView.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        Log.e("ChatScreen", "Error decoding profile image", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to load user data", error.toException())
            }
        })
    }


    private fun sendMessage() {
        val messageText = etMessage.text.toString().trim()
        if (messageText.isNotEmpty() && currentUser != null) {
            val chatMessage = ModelChat(
                message = messageText,
                timestamp = System.currentTimeMillis(),
                senderId = currentUser.uid,
                receiverId = receiverUserId,
                vanish = true
            )

            database.child("Chats").child(chatRoomId).child("messages").push()
                .setValue(chatMessage)
                .addOnSuccessListener {
                    Log.d("RealtimeDB", "Message sent: $messageText")
                    etMessage.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.e("RealtimeDB", "Failed to send message", e)
                }
        } else {
            Log.e("RealtimeDB", "Message is empty or user is null")
        }
    }

    override fun onStop() {
        super.onStop()
        // Remove vanish mode messages when chat screen is closed
        deleteVanishModeMessages()
    }

    private fun deleteVanishModeMessages() {
        val messagesRef = database.child("Chats").child(chatRoomId).child("messages")
        messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("VanishMode", "deleteVanishModeMessages -> onDataChange called")
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ModelChat::class.java)
                    Log.d("VanishMode", "Found message: ${message?.message}, vanish=${message?.vanish}")
                    if (message != null && message.vanish) {
                        Log.d("VanishMode", "Deleting vanish message: ${message.message}")
                        messageSnapshot.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeDB", "Error deleting vanish mode messages", error.toException())
            }
        })
    }

    private fun listenForMessages() {
        val chatRef = database.child("Chats").child(chatRoomId).child("messages")

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ModelChat::class.java)
                message?.let {
                    chatMessages.add(it)
                    adapter.notifyItemInserted(chatMessages.size - 1)
                    recyclerView.scrollToPosition(chatMessages.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("RealtimeDB", "Error fetching messages", error.toException())
            }
        })
    }

    private fun generateChatRoomId(user1: String, user2: String): String {
        return if (user1 > user2) "$user1-$user2" else "$user2-$user1"
    }
}
