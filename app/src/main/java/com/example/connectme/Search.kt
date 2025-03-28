package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.tasks.Tasks

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
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val followingRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/following")
        val pendingRef = FirebaseDatabase.getInstance().getReference("Requests")

        Tasks.whenAllSuccess<DataSnapshot>(followingRef.get(), usersRef.get())
            .addOnSuccessListener { results ->
                val followingSnapshot = results[0] as DataSnapshot
                val usersSnapshot = results[1] as DataSnapshot

                val followedUsers = followingSnapshot.children.map { it.key }.toSet()
                searchList.clear()
                allUsersList.clear()

                for (userSnapshot in usersSnapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    if (userId == currentUserId) continue

                    val username = userSnapshot.child("username").getValue(String::class.java) ?: ""
                    val profileImageUrl = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""
                    val isFollowed = followedUsers.contains(userId)

                    pendingRef.child(userId).child("pending").child(currentUserId).get()
                        .addOnSuccessListener { snapshot ->
                            val isPending = snapshot.exists()
                            if (!isFollowed) {
                                val model = ModelSearch(userId, username, profileImageUrl, isFollowed, isPending)
                                allUsersList.add(model)
                                searchList.add(model)
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
            }
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
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId)

        usersRef.get().addOnSuccessListener { snapshot ->
            val username = snapshot.child("username").value?.toString() ?: "Unknown"
            val profileImageUrl = snapshot.child("profileImage").value?.toString() ?: ""

            val requestRef = FirebaseDatabase.getInstance()
                .getReference("Requests")
                .child(receiverId)
                .child("pending")
                .child(currentUserId)

            val request = ModelRequest(currentUserId, username, profileImageUrl)

            requestRef.setValue(request).addOnSuccessListener {
                Toast.makeText(this, "Follow request sent!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to send follow request", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
        }
    }
}