package com.example.connectme

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import org.json.JSONArray
import org.json.JSONException

class ChatScreen_Vanish : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var adapter: AdapterChatMessageVanish
    private lateinit var handler: Handler
    private val chatMessages = mutableListOf<ModelChat>()
    private val refreshInterval = 2000L // 2 seconds
    private lateinit var fetchRunnable: Runnable

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
        setContentView(R.layout.activity_chat_screen_vanish)

        recyclerView = findViewById(R.id.recyclerViewMessages_chat)
        etMessage = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.btnSend_vanish)
        val backButton = findViewById<ImageView>(R.id.BackButton_chat)
        val usernameTextView: TextView = findViewById(R.id.Username_chat)

        adapter = AdapterChatMessageVanish(chatMessages, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recyclerView.adapter = adapter

        handler = Handler(Looper.getMainLooper())
        fetchRunnable = object : Runnable {
            override fun run() {
                fetchMessagesVolley()
                handler.postDelayed(this, refreshInterval)
            }
        }

        // Load receiver username if needed (optional)
        usernameTextView.text = "Vanishing Chat"

        sendButton.setOnClickListener {
            sendMessageVolley()
        }

        backButton.setOnClickListener {
            finish() // finish vanish screen
        }
    }

    override fun onStart() {
        super.onStart()
        handler.post(fetchRunnable)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(fetchRunnable)
        deleteVanishModeMessages()
    }

    private fun fetchMessagesVolley() {
        val url = "${baseUrl}get_messages.php?user_a=$currentUserId&user_b=$receiverUserId"
        Volley.newRequestQueue(this).add(
            StringRequest(Request.Method.GET, url,
                { resp ->
                    try {
                        val arr = JSONArray(resp)
                        chatMessages.clear()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            // Add both vanish and non-vanish messages
                            val id = o.getInt("id").toString()
                            val senderId = o.getInt("sender_id").toString()
                            val msg = o.getString("message")
                            val ts = o.getLong("timestamp")
                            val vanishFlag = o.optBoolean("vanish", false)
                            chatMessages.add(
                                ModelChat(
                                    id = id,
                                    senderId = senderId,
                                    message = msg,
                                    timestamp = ts,
                                    vanish = vanishFlag
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                        if (chatMessages.isNotEmpty()) {
                            recyclerView.scrollToPosition(chatMessages.size - 1)
                        }
                    } catch (e: JSONException) {
                        Log.e("ChatVanish", "JSON parse error", e)
                    }
                },
                { err ->
                    Log.e("ChatVanish", "Fetch failed", err)
                    Toast.makeText(this, "Fetch failed", Toast.LENGTH_SHORT).show()
                }
            )
        )
    }

    private fun sendMessageVolley() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val url = "${baseUrl}send_message.php"
        val req = object : StringRequest(Method.POST, url,
            { _ ->
                etMessage.text.clear()
                fetchMessagesVolley()
            },
            { err ->
                Log.e("ChatVanish", "Send failed", err)
                Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_a" to currentUserId,
                    "user_b" to receiverUserId,
                    "message" to text,
                    "vanish" to "1"
                )
            }
        }
        Volley.newRequestQueue(this).add(req)
    }

    private fun deleteVanishModeMessages() {
        val url = "${baseUrl}delete_message_vanish.php"
        val queue = Volley.newRequestQueue(this)
        for (msg in chatMessages) {
            if (!msg.vanish) continue // only delete vanish messages
            val req = object : StringRequest(Method.POST, url,
                { resp -> Log.d("VanishDelete", "Deleted msg id=${msg.id}") },
                { err -> Log.e("VanishDelete", "Deletion failed for id=${msg.id}", err) }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    return hashMapOf(
                        "message_id" to msg.id
                    )
                }
            }
            queue.add(req)
        }
    }
}