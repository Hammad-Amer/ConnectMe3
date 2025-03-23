package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainFeedScreen : AppCompatActivity() {

    private val userStory = 1
    private val otherStory = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed_screen)

        val storyList = mutableListOf<ModelStory>()
        storyList.add(ModelStory(userStory, R.drawable.pf1))
        storyList.add(ModelStory(otherStory, R.drawable.pf2))
        storyList.add(ModelStory(otherStory, R.drawable.pf3))
        storyList.add(ModelStory(otherStory, R.drawable.pf4))
        storyList.add(ModelStory(otherStory, R.drawable.pf5))
        storyList.add(ModelStory(otherStory, R.drawable.pf6))
        storyList.add(ModelStory(otherStory, R.drawable.pf7))

        val rv = findViewById<RecyclerView>(R.id.Story_recyclerview_mainfeed)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.adapter = AdapterStory(storyList)

        val feedPostsList = mutableListOf<ModelFeedPosts>()
        feedPostsList.add(ModelFeedPosts("Raja Muhammad Adil Nadeem", R.drawable.pf6, R.drawable.adil1,"G-13"))
        feedPostsList.add(ModelFeedPosts("Affan Ahmed Swati", R.drawable.pf7, R.drawable.feed_post2,"From Palestine btw"))

        val rv2 = findViewById<RecyclerView>(R.id.feed_posts_recyclerview)
        rv2.layoutManager = LinearLayoutManager(this)
        rv2.adapter = AdapterFeedPosts(feedPostsList)

        val gotodms = findViewById<ImageView>(R.id.connectme_dm)
        gotodms.setOnClickListener {
            val intent = Intent(this, DMs::class.java)
            startActivity(intent)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home ->
                    true

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
}
