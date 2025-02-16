package com.example.connectme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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




    }
}
