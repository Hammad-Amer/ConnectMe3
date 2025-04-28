package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException
import org.json.JSONObject

class Search : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterSearch
    private lateinit var searchEditText: EditText

    private val searchList = mutableListOf<ModelSearch>()
    private val allUsersList = mutableListOf<ModelSearch>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.recyclerView_search_history)
        searchEditText = findViewById(R.id.search_bar) //
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdapterSearch(this, searchList) { receiverId ->
            sendFollowRequest(receiverId)
        }
        recyclerView.adapter = adapter

        fetchUsers()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    filterUsers(query)
                } else {
                    searchList.clear()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainFeedScreen::class.java)); true }
                R.id.nav_search -> true
                R.id.nav_add -> { startActivity(Intent(this, NewPost::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, UserProfile::class.java)); true }
                R.id.nav_contacts -> { startActivity(Intent(this, Contacts::class.java)); true }
                else -> false
            }
        }
    }

    private fun fetchUsers() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = Globals.BASE_URL+"fetch_users.php?user_id=$currentUserId"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    if (status == "success") {
                        val usersArray = jsonResponse.getJSONArray("users")
                        val usersList = mutableListOf<ModelSearch>()

                        for (i in 0 until usersArray.length()) {
                            val userObject = usersArray.getJSONObject(i)
                            val userId = userObject.getInt("id")
                            val username = userObject.getString("username")
                            val profileImageUrl = userObject.getString("pfp")
                            val isPending = userObject.getBoolean("isPending")
                            val isFollowed = false

                            val user = ModelSearch(userId.toString(), username, profileImageUrl, isFollowed, isPending)
                            usersList.add(user)
                        }

                        allUsersList.clear()
                        allUsersList.addAll(usersList)
                        searchList.clear()
                        // searchList.addAll(usersList)
                        adapter.notifyDataSetChanged()

                    } else {
                        Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                    Log.e("fetchUsers", "JSON Parsing Error: ${e.message}")
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
                Log.e("fetchUsers", "Error: ${error.message}")
            }
        )

        Volley.newRequestQueue(this).add(stringRequest)
    }

    private fun filterUsers(query: String) {
        val lowerQuery = query.lowercase()
        searchList.clear()
        searchList.addAll(
            allUsersList
                .filter { it.username.lowercase().contains(lowerQuery) }
                .take(4)
        )
        adapter.notifyDataSetChanged()
    }

    private fun sendFollowRequest(receiverId: String) {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = Globals.BASE_URL+"send_request.php"

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    if (status == "success") {
                        Toast.makeText(this, "Follow request sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                    Log.e("sendFollowRequest", "JSON Parsing Error: ${e.message}")
                }
            },
            { error ->
                Toast.makeText(this, "Failed to send follow request", Toast.LENGTH_SHORT).show()
                Log.e("sendFollowRequest", "Error: ${error.message}")
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf(
                    "sender_id" to currentUserId.toString(),
                    "receiver_id" to receiverId
                )
            }
        }

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(stringRequest)
    }
}