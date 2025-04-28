package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.tasks.Tasks
import org.json.JSONObject

class Contacts : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterContact
    private val contactList = mutableListOf<ModelContact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        recyclerView = findViewById(R.id.recycler_follow)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterContact(this, contactList) { receiverId ->
            sendRequest(receiverId)
        }
        recyclerView.adapter = adapter

        fetchUsers()

        val requestsTextView = findViewById<ImageView>(R.id.requests)
        requestsTextView.setOnClickListener {
            startActivity(Intent(this, ContactsRequests::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainFeedScreen::class.java)); true }
                R.id.nav_search -> { startActivity(Intent(this, Search::class.java)); true }
                R.id.nav_add -> { startActivity(Intent(this, NewPost::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfile::class.java)); true }
                R.id.nav_contacts -> true
                else -> false
            }
        }
    }

    private fun fetchUsers() {

        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = Globals.BASE_URL+"fetch_users.php?user_id=$currentUserId"


        val requestQueue = Volley.newRequestQueue(this)

        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                if (response.getString("status") == "success") {
                    val users = response.getJSONArray("users")
                    contactList.clear()

                    for (i in 0 until users.length()) {
                        val user = users.getJSONObject(i)
                        val userId = user.getInt("id").toString()
                        val username = user.getString("username")
                        val profileImage = user.optString("pfp", "")
                        val isPending = user.getBoolean("isPending")

                        contactList.add(
                            ModelContact(userId, username, profileImage, isFollowed = false, isPending = isPending)
                        )
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Error: ${response.getString("message")}", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonRequest)
    }


    private fun sendRequest(receiverId: String) {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = "http://10.0.2.2/connectme/send_request.php?user_id=$currentUserId"


        val requestQueue = Volley.newRequestQueue(this)
        val params = HashMap<String, String>()
        params["sender_id"] = currentUserId.toString()
        params["receiver_id"] = receiverId

        val request = object : StringRequest(Method.POST, url,
            { response ->
                val json = JSONObject(response)
                val message = json.getString("message")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return params
            }
        }

        requestQueue.add(request)
    }
}