package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.android.gms.tasks.Tasks

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
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("Users")
        val followingRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/following")
        val pendingRef = FirebaseDatabase.getInstance().getReference("Requests") // General reference

        Tasks.whenAllSuccess<DataSnapshot>(
            followingRef.get(), usersRef.get()
        ).addOnSuccessListener { results ->
            val followingSnapshot = results[0] as DataSnapshot
            val usersSnapshot = results[1] as DataSnapshot

            val followedUsers = followingSnapshot.children.map { it.key }.toSet()
            contactList.clear()

            for (userSnapshot in usersSnapshot.children) {
                val userId = userSnapshot.key ?: continue
                if (userId == currentUserId) continue

                val username = userSnapshot.child("username").getValue(String::class.java) ?: ""
                val profileImageUrl = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""

                val isFollowed = followedUsers.contains(userId)

                pendingRef.child(userId).child("pending").child(currentUserId).get().addOnSuccessListener { snapshot ->
                    val isPending = snapshot.exists()

                    if (!isFollowed) {
                        contactList.add(ModelContact(userId, username, profileImageUrl, isFollowed, isPending))
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch users", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendRequest(receiverId: String) {
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
                Toast.makeText(this, "Friend Request Sent!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to Send Request", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to Fetch User Data", Toast.LENGTH_SHORT).show()
        }
    }
}
