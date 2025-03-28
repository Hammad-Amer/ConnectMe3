package com.example.connectme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserProfile : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var gridView: GridView
    private lateinit var adapter: AdapterUserProfileImages
    private lateinit var profilePic: com.google.android.material.imageview.ShapeableImageView
    private val imageBitmaps = mutableListOf<Bitmap>()  // Store Bitmaps instead of URIs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        gridView = findViewById(R.id.profile_grid_userprofile)
        profilePic = findViewById(R.id.Main_profile_pic_userprofile)  // Initialize profilePic

        adapter = AdapterUserProfileImages(this, imageBitmaps)
        gridView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            database = FirebaseDatabase.getInstance().getReference("Users/$userId")
            fetchUserProfile() // Fetch profile image
            fetchUserPosts() // Fetch user posts
        }

        findViewById<ImageView>(R.id.edit_profile_userprofile).setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        findViewById<LinearLayout>(R.id.follower_box).setOnClickListener {
            startActivity(Intent(this, FollowersList::class.java))
        }

        findViewById<LinearLayout>(R.id.following_box).setOnClickListener {
            startActivity(Intent(this, FollowingList::class.java))
        }

        findViewById<Button>(R.id.logout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainFeedScreen::class.java))
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, Search::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, NewPost::class.java))
                    true
                }
                R.id.nav_profile -> true
                R.id.nav_contacts -> {
                    startActivity(Intent(this, Contacts::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error decoding image", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    private fun fetchUserProfile() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileBase64 = snapshot.child("profileImage").getValue(String::class.java)
                val username = snapshot.child("username").getValue(String::class.java)

                // Load profile image if available
                if (!profileBase64.isNullOrEmpty()) {
                    val bitmap = base64ToBitmap(profileBase64)
                    if (bitmap != null) {
                        profilePic.setImageBitmap(bitmap)
                    }
                }

                val profileNameTextView = findViewById<TextView>(R.id.profile_name_userprofile)
                profileNameTextView.text = username ?: "User"

                val postCountTextView = findViewById<TextView>(R.id.post_count)
                val postCount = snapshot.child("posts").childrenCount.toInt()
                postCountTextView.text = postCount.toString()

                val followerCountTextView = findViewById<TextView>(R.id.follower_count)
                val followerCount = snapshot.child("followers").childrenCount.toInt()
                followerCountTextView.text = followerCount.toString()

                val followingCountTextView = findViewById<TextView>(R.id.following_count)
                val followingCount = snapshot.child("following").childrenCount.toInt()
                followingCountTextView.text = followingCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                println("DEBUG: Database error -> ${error.message}")
            }
        })
    }

    private fun fetchUserPosts() {
        database.child("posts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                imageBitmaps.clear()
                for (postSnapshot in snapshot.children) {
                    val imageBase64 = postSnapshot.child("imageBase64").getValue(String::class.java)
                    if (!imageBase64.isNullOrEmpty()) {
                        val bitmap = base64ToBitmap(imageBase64)
                        if (bitmap != null) {
                            imageBitmaps.add(bitmap)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}
