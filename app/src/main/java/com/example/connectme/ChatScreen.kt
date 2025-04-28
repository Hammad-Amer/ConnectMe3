package com.example.connectme

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatScreen : AppCompatActivity() {
    private lateinit var onlineStatusTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var adapter: AdapterChatMessage
    private val chatMessages = mutableListOf<ModelChat>()

    private lateinit var screenshotObserver : ContentObserver
    private var recv_id = ""
    private val curr_userName = ""
    private var lastScreenshotPath: String?= null
    private var lastScreenshotTime: Long = 0




    private var receiverUsername: String? = null
    private var receiverToken: String? = null

    private val handler = Handler(Looper.getMainLooper())

    private val handler2 = Handler(Looper.getMainLooper())

    private val refreshInterval = 2000L // 2 seconds

    private lateinit var statusHandler: Handler
    private val statusInterval = 3000L  // milliseconds
    private lateinit var statusRunnable: Runnable

    private val fetchMessagesRunnable = object : Runnable {
        override fun run() {
            fetchMessagesVolley()
            handler.postDelayed(this, refreshInterval)
        }
    }

    private lateinit var usernameTextView: TextView
    private lateinit var pfp: ImageView

    private val sharedPref by lazy {
        getSharedPreferences("ConnectMePref", MODE_PRIVATE)
    }
    private val currentUserId by lazy {
        sharedPref.getInt("userId", 0).takeIf { it != 0 }?.toString() ?: ""
    }
    private val receiverUserId by lazy {
        intent.getStringExtra("USER_ID") ?: ""
    }
    private val baseUrl = Globals.BASE_URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)


        recyclerView = findViewById(R.id.recyclerViewMessages_chat)
        etMessage = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend_vanish)
        usernameTextView = findViewById(R.id.Username_chat)
        onlineStatusTextView = findViewById(R.id.onlineStatusTextView)


        pfp = findViewById(R.id.Main_profile_pic_chat)

        adapter = AdapterChatMessage(chatMessages, currentUserId) { message, position ->
            handleMessageLongPress(message, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recyclerView.adapter = adapter

        fetchReceiverUsername(receiverUserId)

        fetchUserProfile(receiverUserId)

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

        fetchMessagesVolley()

        statusHandler = Handler(Looper.getMainLooper())
        statusRunnable = object : Runnable {
            override fun run() {
                fetchReceiverStatus()
                statusHandler.postDelayed(this, statusInterval)
            }
        }

        screenshotObserver = object : ContentObserver(handler2)
        {
            override fun onChange(selfChange: Boolean)
            {
                super.onChange(selfChange)
                checkForScreenshot()
            }
        }

        sendButton.setOnClickListener { sendMessageVolley() }

        sendButton.setOnLongClickListener {

            Toast.makeText(this, "Entering vanish mode", Toast.LENGTH_SHORT).show()


            val intent = Intent(this, ChatScreen_Vanish::class.java).apply {
                putExtra("USER_ID", receiverUserId)
            }
            startActivity(intent)
            true
        }
    }




    private fun fetchMessagesVolley() {
        val db = dbHelper(this)

        if (Globals.isInternetAvailable(this)) {
            val url = "${baseUrl}get_messages.php?user_a=$currentUserId&user_b=$receiverUserId"
            Volley.newRequestQueue(this).add(StringRequest(
                Request.Method.GET, url,
                { resp ->
                    try {
                        val arr = JSONArray(resp)
                        db.clearCachedMessages(currentUserId, receiverUserId)
                        chatMessages.clear()

                        // wipe old cache for this conversation


                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            val sender = o.getInt("sender_id").toString()
                            val receiver = if (sender == currentUserId) receiverUserId else currentUserId
                            val msg = ModelChat(
                                id         = o.getString("id"),
                                senderId   = sender,
                                receiverId = receiver,                    // <- new
                                message    = o.getString("message"),
                                timestamp  = o.getLong("timestamp")
                            )
                            chatMessages.add(msg)
                            db.cacheMessage(msg)  // store locally
                        }

                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(chatMessages.size - 1)
                    } catch (e: JSONException) {
                        Log.e("ChatScreen", "JSON parse error", e)
                        loadFromCache(db)
                    }
                },
                { err ->
                    Log.e("ChatScreen", "Volley GET failed", err)
                    Toast.makeText(this, "Fetch failed, loading cache", Toast.LENGTH_SHORT).show()
                    loadFromCache(db)
                }
            ))
        } else {
            Toast.makeText(this, "No internet. Loading cached messages.", Toast.LENGTH_SHORT).show()
            loadFromCache(db)
        }
    }

    private fun loadFromCache(db: dbHelper) {
        chatMessages.clear()
        chatMessages.addAll(db.getCachedMessages(currentUserId, receiverUserId))
        adapter.notifyDataSetChanged()
        if (chatMessages.isNotEmpty()) {
            recyclerView.scrollToPosition(chatMessages.size - 1)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(screenshotObserver)
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

    private fun sendMessageVolley() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val url = "${baseUrl}send_message.php"
        val req = object : StringRequest(Method.POST, url,
            { _ ->
                etMessage.text.clear()
                sendNotificationToReceiver(text)
                fetchMessagesVolley()
            },
            { err ->
                Log.e("ChatScreen", "Volley POST failed", err)
                Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_a"] = currentUserId
                params["user_b"] = receiverUserId
                params["message"] = text
                return params
            }
        }
        Volley.newRequestQueue(this).add(req)
    }

    override fun onStart() {
        super.onStart()
        setUserOnlineStatus(currentUserId)
        handler.post(fetchMessagesRunnable) // Start fetching
        setMyStatus("online")
        statusHandler.post(statusRunnable)


    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(fetchMessagesRunnable) // Stop fetching
        setMyStatus("offline")
        statusHandler.removeCallbacks(statusRunnable)

    }

    private fun fetchReceiverStatus() {
        val url = "$baseUrl/get_status.php?user_id=$receiverUserId"
        Volley.newRequestQueue(this).add(
            StringRequest(Request.Method.GET, url,
                { resp ->
                    val status = JSONObject(resp).optString("status","offline")
                    onlineStatusTextView.text = status
                },
                { err -> Log.e("ChatScreen", "fetch status failed", err) }
            )
        )
    }

    private fun setMyStatus(status: String) {
        val url = "$baseUrl/set_status.php"
        val req = object : StringRequest(Method.POST, url,
            { /* ignore success */ },
            { err -> Log.e("ChatScreen", "set status failed", err) }
        ) {
            override fun getParams() = hashMapOf(
                "user_id" to currentUserId,
                "status"  to status
            )
        }
        Volley.newRequestQueue(this).add(req)
    }


    private fun setUserOnlineStatus(userId: String) {
        val ref = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("status/$userId")
        ref.setValue("online")
        ref.onDisconnect().setValue("offline")
    }



    private fun handleMessageLongPress(message: ModelChat, position: Int) {
        // 1) Never allow ops on an already-deleted placeholder:
        if (message.message == "This message was deleted") {
            Toast.makeText(this, "Cannot modify a deleted message", Toast.LENGTH_SHORT).show()
            return
        }

        // 2) Enforce the 5-minute window:
        val fiveMinutesMs = 5 * 60 * 1000L
        if (System.currentTimeMillis() - message.timestamp > fiveMinutesMs) {
            Toast.makeText(this, "You can only edit/delete within 5 minutes", Toast.LENGTH_SHORT).show()
            return
        }

        // 3) Otherwise show your Edit/Delete dialog as before:
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditMessageDialog(message)
                    1 -> confirmDeleteMessage(message)
                }
            }
            .show()
    }


    private fun showEditMessageDialog(message: ModelChat) {
        val editText = EditText(this)
        editText.setText(message.message)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Message")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    editMessageVolley(message, newText)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    private fun editMessageVolley(message: ModelChat, newText: String) {
        val url = "${baseUrl}edit_message.php"
        val req = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("ChatScreen", "Edit Response: $response")
                if (response.trim() == "success") {
                    fetchMessagesVolley()
                    Toast.makeText(this, "Message updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update: $response", Toast.LENGTH_LONG).show()
                }
            },
            { err ->
                Log.e("ChatScreen", "Edit message failed", err)
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
        ){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["message_id"] = message.id
                // only for edit:
                if (newText.isNotEmpty()) {
                    params["new_message"] = newText
                }
                return params
            }
        }
        Volley.newRequestQueue(this).add(req)
    }
    private fun confirmDeleteMessage(message: ModelChat) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Message")
            .setMessage("Are you sure you want to delete this message?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMessageVolley(message)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }
    private fun deleteMessageVolley(message: ModelChat) {
        val url = "${baseUrl}delete_message.php"
        val req = object : StringRequest(Method.POST, url,
            { response ->
                if (response.trim() == "success") {
                    fetchMessagesVolley()
                    Toast.makeText(this, "Message deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete: $response", Toast.LENGTH_LONG).show()
                }
            },
            { err ->
                Toast.makeText(this, "Delete failed: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "message_id" to message.id      // ← critical: must match your PHP
                )
            }
        }
        Volley.newRequestQueue(this).add(req)
    }


    private fun sendNotificationToReceiver(messageText: String) {
        val currentUserId = sharedPref.getInt("userId", 0).takeIf { it != 0 }?.toString() ?: return
        val senderUsername = sharedPref.getString("username", "Someone") ?: "Someone"

        if (receiverToken.isNullOrEmpty()) {
            Log.e("FCM", "Receiver token is null or empty")
            return
        }

        val notification = Notification(
            message = NotificationData(
                token = receiverToken!!,
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
    }



    private fun fetchReceiverUsername(userId: String) {
        val url = "${baseUrl}get_user_info.php"
        Volley.newRequestQueue(this).add(object : StringRequest(Method.POST, url,
            { response ->
                Log.d("FetchReceiverRaw", "Raw response:\n[$response]")
                try {
                    val json = JSONObject(response.trim())
                    receiverUsername = json.optString("username", null)
                    receiverToken    = json.optString("fcm_token", null)
                    Log.d("FetchReceiver", "=> username: $receiverUsername, token: $receiverToken")

                    runOnUiThread {
                        usernameTextView.text = receiverUsername ?: ""
                    }
                } catch (e: JSONException) {
                    Log.e("FetchReceiver", "JSON parsing error", e)
                }
            },
            { error ->
                Log.e("FetchReceiver", "Volley error", error)
            }
        ) {
            override fun getParams() = hashMapOf("user_id" to userId)
        })
    }


    private fun generateChatRoomId(user1: String, user2: String): String {
        return if (user1 > user2) "$user1-$user2" else "$user2-$user1"
    }

    private fun fetchUserProfile(userId: String) {
        val url = "${baseUrl}profile.php"
        val requestQueue = Volley.newRequestQueue(this)

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                Log.d("FetchUserProfileRaw", "Raw response:\n[$response]")
                try {
                    val json = JSONObject(response.trim())
                    val status = json.optString("status")

                    if (status == "success") {
                        val data = json.getJSONObject("data")
                        val username = data.optString("username", "")
                        val profilePicUrl = data.optString("pfp", "")

                        runOnUiThread {
                            usernameTextView.text = username

                            if (profilePicUrl.isNotEmpty()) {
                                Glide.with(this)
                                    .load(profilePicUrl)
                                    .placeholder(R.drawable.pf6) // your default image
                                    .into(pfp)
                            }
                        }
                    } else {
                        Log.e("FetchUserProfile", "Server returned error: ${json.optString("message")}")
                    }

                } catch (e: JSONException) {
                    Log.e("FetchUserProfile", "JSON parsing error", e)
                }
            },
            { error ->
                Log.e("FetchUserProfile", "Volley error", error)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("user_id" to userId)
            }
        }

        requestQueue.add(stringRequest)
    }

}

