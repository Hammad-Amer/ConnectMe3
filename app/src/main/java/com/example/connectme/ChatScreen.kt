package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.material.imageview.ShapeableImageView

import com.google.firebase.database.*

class ChatScreen : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var adapter: AdapterChatMessage
    private val chatMessages = mutableListOf<ModelChat>()
    private lateinit var usernameTextView: TextView
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val database = FirebaseDatabase.getInstance().reference
    private val receiverUserId: String by lazy {
        intent.getStringExtra("USER_ID") ?: "" // Get the receiver ID from intent
    }

    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        recyclerView = findViewById(R.id.recyclerViewMessages_chat)
        etMessage = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend_vanish)
        usernameTextView = findViewById(R.id.Username_chat)
        adapter = AdapterChatMessage(chatMessages) { message, position ->
            handleMessageLongPress(message, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        chatRoomId = generateChatRoomId(currentUser?.uid ?: "", receiverUserId)

        fetchReceiverUsername()
        sendButton.setOnClickListener {
            sendMessage()
        }

        sendButton.setOnLongClickListener {
            val intent = Intent(this, ChatScreen_Vanish::class.java)
            intent.putExtra("USER_ID", receiverUserId) // pass receiver's user ID dynamically
            startActivity(intent)
            true
        }

        val backButtonchat = findViewById<ImageView>(R.id.BackButton_chat)
        backButtonchat.setOnClickListener {
            val intent = Intent(this, DMs::class.java)
            startActivity(intent)
        }

        listenForMessages()
    }

    private fun handleMessageLongPress(message: ModelChat, position: Int) {
        // Check if the current user is the sender
        if (message.senderId != currentUser?.uid) {
            Toast.makeText(this, "You can only edit or delete your own messages.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime = System.currentTimeMillis()
        val allowedDuration = 5 * 60 * 1000  // 5 minutes in milliseconds
        if (currentTime - message.timestamp > allowedDuration) {
            // Outside allowed window
            Toast.makeText(this, "Editing or deleting is allowed only within 5 minutes.", Toast.LENGTH_SHORT).show()
            return
        }
        // Show a dialog with options "Edit" and "Delete"
        val options = arrayOf("Edit", "Delete")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit or Delete Message")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showEditDialog(message, position)
                    1 -> deleteMessage(message)
                }
            }
            .show()
    }

    private fun showEditDialog(message: ModelChat, position: Int) {
        val editText = EditText(this).apply {
            setText(message.message)
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { dialog, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    // Update the message in the database
                    val messageRef = database.child("Chats").child(chatRoomId).child("messages").child(message.key ?: "")
                    // Create a new map with updated message text
                    val updates = mapOf<String, Any>("message" to newText)
                    messageRef.updateChildren(updates)
                        .addOnSuccessListener {
                            // Also update locally
                            message.message = newText
                            adapter.notifyItemChanged(position)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update message.", Toast.LENGTH_SHORT).show()
                        }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun deleteMessage(message: ModelChat) {
        if (message.key != null) {
            val messageRef = database.child("Chats").child(chatRoomId).child("messages").child(message.key!!)
            messageRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
                    // Optionally, remove it from your local list and update adapter
                    val index = chatMessages.indexOfFirst { it.key == message.key }
                    if (index != -1) {
                        chatMessages.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
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
                receiverId = receiverUserId
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

    private fun listenForMessages() {
        val chatRef = database.child("Chats").child(chatRoomId).child("messages")

        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ModelChat::class.java)
                message?.key = snapshot.key  // Save the unique key
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
