package com.example.connectme

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager

import android.content.Intent
import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatScreen : AppCompatActivity() {

    private lateinit var screenshotObserver : ContentObserver
    private var recv_id = ""
    private val curr_userName = ""
    private var lastScreenshotPath: String?= null
    private var lastScreenshotTime: Long = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var adapter: AdapterChatMessage
    private val chatMessages = mutableListOf<ModelChat>()
    private lateinit var usernameTextView: TextView
    private lateinit var onlineStatusTextView: TextView  // <--- For showing receiver's status

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

        var callbutton = findViewById<ImageView>(R.id.Call_chat)
        var vidcallbutton = findViewById<ImageView>(R.id.VideoCall_chat)

        callbutton.setOnClickListener {
            val intent = Intent(this, Voicecall::class.java)
            intent.putExtra("USER_ID", receiverUserId) // pass receiver's user ID dynamically
            startActivity(intent)
        }

        vidcallbutton.setOnClickListener {
            val intent = Intent(this, VideoCall::class.java)
            intent.putExtra("USER_ID", receiverUserId) // pass receiver's user ID dynamically
            startActivity(intent)
        }

        // This TextView must exist in your layout (e.g., below usernameTextView).
        // For example: <TextView android:id="@+id/onlineStatusTextView" ... />
        onlineStatusTextView = findViewById(R.id.onlineStatusTextView)

        adapter = AdapterChatMessage(chatMessages) { message, position ->
            handleMessageLongPress(message, position)
        }

        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        // Generate the chat room ID
        chatRoomId = generateChatRoomId(currentUser?.uid ?: "", receiverUserId)

        // Load receiver's username & profile image
        fetchReceiverUsername()

        // Listen for messages in this chat
        listenForMessages()


        val handler = Handler(Looper.getMainLooper())

        screenshotObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                checkForScreenshot()
            }
        }

        // Button listeners
        sendButton.setOnClickListener { sendMessage() }
        sendButton.setOnLongClickListener {
            val intent = Intent(this, ChatScreen_Vanish::class.java)
            intent.putExtra("USER_ID", receiverUserId) // pass receiver's user ID dynamically
            startActivity(intent)
            true
        }

        val backButtonchat = findViewById<ImageView>(R.id.BackButton_chat)
        backButtonchat.setOnClickListener {
            startActivity(Intent(this, DMs::class.java))
        }

        // Listen for the receiver's online/offline status
        listenForReceiverStatus()
    }

    // MARK THE CURRENT USER AS ONLINE WHEN THE ACTIVITY STARTS
    override fun onStart() {
        super.onStart()
        setUserOnlineStatus(currentUser?.uid)
    }

    // FUNCTION TO SET THE CURRENT USER'S STATUS TO "ONLINE"
    private fun setUserOnlineStatus(userId: String?) {
        if (userId == null) return
        val userStatusDatabaseRef = FirebaseDatabase.getInstance().getReference("status/$userId")

        // Write the online status
        userStatusDatabaseRef.setValue("online")

        // When the client disconnects, update to "offline"
        userStatusDatabaseRef.onDisconnect().setValue("offline")
    }

    // FUNCTION TO LISTEN FOR THE RECEIVER'S STATUS
    private fun listenForReceiverStatus() {
        val receiverStatusRef = FirebaseDatabase.getInstance().getReference("status/$receiverUserId")
        receiverStatusRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                if (status == "online") {
                    onlineStatusTextView.text = "Online"
                } else {
                    onlineStatusTextView.text = "Offline"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }



    private fun checkForScreenshot() {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA
        )

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                val now = System.currentTimeMillis()
                if (path.lowercase().contains("screenshot") && path != lastScreenshotPath  && now - lastScreenshotTime > 5000) {
                    lastScreenshotPath = path
                    lastScreenshotTime = now
                    runOnUiThread {
                        sendNotificationToReceiver(" took a screenshot of the chat!")


                    }
                }
            }
           }
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
            Toast.makeText(this, "Editing or deleting is allowed only within 5 minutes.", Toast.LENGTH_SHORT).show()
            return
        }
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
                    val messageRef = database.child("Chats")
                        .child(chatRoomId)
                        .child("messages")
                        .child(message.key ?: "")
                    val updates = mapOf<String, Any>("message" to newText)
                    messageRef.updateChildren(updates)
                        .addOnSuccessListener {
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

    override fun onPause() {
        super.onPause()
        contentResolver.unregisterContentObserver(screenshotObserver)
    }
    override fun onResume() {
        super.onResume()
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            screenshotObserver
        )
    }

    private fun deleteMessage(message: ModelChat) {
        message.key?.let {
            val messageRef = database.child("Chats")
                .child(chatRoomId)
                .child("messages")
                .child(it)
            messageRef.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
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
                    sendNotificationToReceiver(messageText)
                }
                .addOnFailureListener { e ->
                    Log.e("RealtimeDB", "Failed to send message", e)
                }
        } else {
            Log.e("RealtimeDB", "Message is empty or user is null")
        }
    }

    private fun sendNotificationToReceiver(messageText: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("Users")
            .child(currentUserId)
            .child("username")
            .get().addOnSuccessListener { senderSnapshot ->
                val senderUsername = senderSnapshot.getValue(String::class.java) ?: "Someone"
                FirebaseDatabase.getInstance().getReference("Users")
                    .child(receiverUserId)
                    .child("token")
                    .get().addOnSuccessListener { tokenSnapshot ->
                        val receiverToken = tokenSnapshot.getValue(String::class.java)
                        if (!receiverToken.isNullOrEmpty()) {
                            val notification = Notification(
                                message = NotificationData(
                                    token = receiverToken,
                                    data = hashMapOf(
                                        "title" to senderUsername,
                                        "body" to messageText
                                    )
                                )
                            )
                            Log.d("FCM1", "Sending notification to receiver with token: $receiverToken")
                            NotificationApi.create().sendNotification(notification)
                                .enqueue(object : Callback<Notification> {
                                    override fun onResponse(call: Call<Notification>, response: Response<Notification>) {
                                        Log.d("FCM", "Notification sent to receiver")
                                    }
                                    override fun onFailure(call: Call<Notification>, t: Throwable) {
                                        Log.e("FCM", "Error sending notification: ${t.message}")
                                    }
                                })
                        } else {
                            Log.e("FCM", "Receiver token is null or empty")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("FCM", "Failed to fetch receiver token", e)
                    }
            }.addOnFailureListener { e ->
                Log.e("FCM", "Failed to fetch sender username", e)
            }
    }

    private fun fetchReceiverUsername() {
        val usersRef = database.child("Users").child(receiverUserId)
        val profileImageView = findViewById<ShapeableImageView>(R.id.Main_profile_pic_chat)
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.child("username").value?.toString() ?: "Unknown"
                usernameTextView.text = username

                val base64String = snapshot.child("profileImage").value?.toString()
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

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(screenshotObserver)
    }

    private fun generateChatRoomId(user1: String, user2: String): String {
        return if (user1 > user2) "$user1-$user2" else "$user2-$user1"
    }
}
