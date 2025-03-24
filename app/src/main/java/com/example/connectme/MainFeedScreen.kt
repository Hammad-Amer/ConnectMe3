package com.example.connectme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainFeedScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed_screen)

        checkAndDeleteExpiredStory()

        setupStoriesRecyclerView()
        setupFeedPostsRecyclerView()
        setupDMButton()
        setupBottomNavigation()
    }

    private fun setupStoriesRecyclerView() {
        val storyList = mutableListOf<ModelStory>()
        storyList.add(ModelStory(AdapterStory.USER_STORY, R.drawable.pf1))
        storyList.add(ModelStory(AdapterStory.OTHER_STORY, R.drawable.pf2))
        storyList.add(ModelStory(AdapterStory.OTHER_STORY, R.drawable.pf3))
        storyList.add(ModelStory(AdapterStory.OTHER_STORY, R.drawable.pf4))
        storyList.add(ModelStory(AdapterStory.OTHER_STORY, R.drawable.pf5))
        storyList.add(ModelStory(AdapterStory.OTHER_STORY, R.drawable.pf6))
        storyList.add(ModelStory(AdapterStory.OTHER_STORY, R.drawable.pf7))

        val rvStories = findViewById<RecyclerView>(R.id.Story_recyclerview_mainfeed)
        rvStories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rvStories.adapter = AdapterStory(
            storyList,
            onStoryClick = { clickedStory ->
                if (clickedStory.type == AdapterStory.USER_STORY) {
                    checkAndOpenStory()
                }
            },
            onStoryLongClick = { clickedStory ->
                if (clickedStory.type == AdapterStory.USER_STORY) {
                    openUploadStoryPage()
                }
            }
        )
    }

    private fun checkAndDeleteExpiredStory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId)
            .child("stories")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val storyTimestamp = snapshot.child("timestamp").getValue(Long::class.java)

                    if (storyTimestamp != null) {
                        val currentTime = System.currentTimeMillis()

                        if (currentTime - storyTimestamp >= 86400000) {
                            databaseRef.removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@MainFeedScreen,
                                        "Story expired and deleted",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainFeedScreen, "Error checking story", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkAndOpenStory() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child(userId)
            .child("stories") // Make sure this matches your actual path

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    openStoryViewer(userId) // Pass the user ID
                } else {
                    Toast.makeText(this@MainFeedScreen, "No stories available", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainFeedScreen, "Error loading story", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupFeedPostsRecyclerView() {
        val feedPostsList = mutableListOf<ModelFeedPosts>()
        feedPostsList.add(ModelFeedPosts("Raja Muhammad Adil Nadeem", R.drawable.pf6, R.drawable.adil1, "G-13"))
        feedPostsList.add(ModelFeedPosts("Affan Ahmed Swati", R.drawable.pf7, R.drawable.feed_post2, "From Palestine btw"))

        val rvPosts = findViewById<RecyclerView>(R.id.feed_posts_recyclerview)
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = AdapterFeedPosts(feedPostsList)
    }

    private fun setupDMButton() {
        findViewById<ImageView>(R.id.connectme_dm).setOnClickListener {
            startActivity(Intent(this, DMs::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, Search::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, NewPost::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, UserProfile::class.java))
                    true
                }
                R.id.nav_contacts -> {
                    startActivity(Intent(this, Contacts::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun openStoryViewer(userId: String) {
        val intent = Intent(this, StoryView::class.java).apply {
            putExtra("USER_ID", userId)
        }
        startActivity(intent)
    }

    private fun openUploadStoryPage() {
        val intent = Intent(this, UploadStory::class.java)
        startActivity(intent)
    }
}
