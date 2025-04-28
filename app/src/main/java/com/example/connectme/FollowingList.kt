package com.example.connectme

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FollowingList : AppCompatActivity() {

    private lateinit var adapter: AdapterFollowing
    private val followingList = mutableListOf<ModelFollowing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_following_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_following_list)
        adapter = AdapterFollowing(followingList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.go_backto_profile).setOnClickListener {
            finish()
        }

        fetchFollowing()
    }


    private fun fetchFollowing() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val followingRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId).child("following")

        followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followingList.clear()
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users")

                for (following in snapshot.children) {
                    val followingId = following.key ?: continue  // Get the user ID

                    databaseRef.child(followingId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val username = userSnapshot.child("fullName").getValue(String::class.java) ?: "Unknown"
                            val profileBase64 = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""

                            followingList.add(ModelFollowing(profileBase64, username))
                            adapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("FollowingList", "Error fetching user data: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FollowingList", "Error fetching following: ${error.message}")
            }
        })
    }
}
