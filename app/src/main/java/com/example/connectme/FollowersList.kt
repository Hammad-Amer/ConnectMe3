package com.example.connectme

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FollowersList : AppCompatActivity() {

    private lateinit var adapter: AdapterFollowing
    private val followersList = mutableListOf<ModelFollowing>()
    private val tempFollowersMap = mutableMapOf<String, ModelFollowing>()
    private var totalFollowers = 0 // Track total followers count

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_followers_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_followers_list)
        adapter = AdapterFollowing(followersList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.go_backto_profile).setOnClickListener { finish() }

        fetchFollowers()
    }

    private fun fetchFollowers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.e("FollowersList", "No authenticated user")
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("FollowersList", "Starting fetch for user: $currentUserId")

        val followersRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(currentUserId)
            .child("followers")

        followersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FollowersList", "Received followers snapshot: ${snapshot.exists()}")

                if (!snapshot.exists()) {
                    Log.w("FollowersList", "No followers node exists")
                    Toast.makeText(this@FollowersList, "No followers found", Toast.LENGTH_SHORT).show()
                    return
                }

                val followerIds = snapshot.children.mapNotNull { it.key }
                totalFollowers = followerIds.size // Set the total count
                Log.d("FollowersList", "Found $totalFollowers follower IDs: $followerIds")

                if (followerIds.isEmpty()) {
                    Log.w("FollowersList", "Followers list is empty")
                    Toast.makeText(this@FollowersList, "No followers", Toast.LENGTH_SHORT).show()
                    return
                }

                val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
                var processedCount = 0

                followerIds.forEach { followerId ->
                    Log.d("FollowersList", "Processing follower ID: $followerId")

                    databaseRef.child(followerId).addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                Log.d("FollowersList", "Received user data for: $followerId")

                                if (!userSnapshot.exists()) {
                                    Log.e("FollowersList", "User $followerId does not exist in database")
                                    return
                                }

                                val username = userSnapshot.child("fullName")
                                    .getValue(String::class.java) ?: "Unknown".also {
                                    Log.w("FollowersList", "Missing username for $followerId")
                                }

                                val rawBase64 = userSnapshot.child("profileImage")
                                    .getValue(String::class.java) ?: "".also {
                                    Log.w("FollowersList", "Missing profile image for $followerId")
                                }

                                Log.d("FollowersList", "Raw Base64 for $followerId - " +
                                        "Length: ${rawBase64.length}, " +
                                        "Start: ${rawBase64.take(10)}, " +
                                        "End: ${rawBase64.takeLast(10)}")

                                try {
                                    Base64.decode(rawBase64, Base64.DEFAULT)
                                } catch (e: Exception) {
                                    Log.e("FollowersList", "Invalid Base64 for $followerId: ${e.message}")
                                }

                                synchronized(tempFollowersMap) {
                                    tempFollowersMap[followerId] = ModelFollowing(rawBase64, username)
                                    processedCount++

                                    Log.d("FollowersList", "Processed $processedCount/$totalFollowers followers")

                                    if (processedCount == totalFollowers) {
                                        Log.d("FollowersList", "All followers processed. Updating UI")
                                        runOnUiThread {
                                            followersList.clear()
                                            followersList.addAll(tempFollowersMap.values)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("FollowersList", "Database error for $followerId: ${error.message}")
                            }
                        }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FollowersList", "Followers fetch cancelled: ${error.code} - ${error.message}")
                Toast.makeText(this@FollowersList, "Database error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}