package com.example.connectme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FollowingList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_following_list)

        val FollowingList = mutableListOf<ModelFollowing>()
        FollowingList.add(ModelFollowing(R.drawable.pf2, "Raja Muhammad Adil Nadeem"))
        FollowingList.add(ModelFollowing(R.drawable.pf3, "Affan Ahmed Swati"))
        FollowingList.add(ModelFollowing(R.drawable.pf4, "Shayaan Khalid"))
        FollowingList.add(ModelFollowing(R.drawable.pf5, "Fatima"))
        FollowingList.add(ModelFollowing(R.drawable.pf6, "Arjit Singh"))
        FollowingList.add(ModelFollowing(R.drawable.pf7, "Atif Aslam"))
        FollowingList.add(ModelFollowing(R.drawable.pf8, "Ali Zafar"))
        FollowingList.add(ModelFollowing(R.drawable.pf9, "Bilal Saeed"))
        FollowingList.add(ModelFollowing(R.drawable.pf10, "Maryam Nawaz"))
        FollowingList.add(ModelFollowing(R.drawable.pf11, "Altaf Hussain"))
        FollowingList.add(ModelFollowing(R.drawable.pf12, "Imran Khan"))
        FollowingList.add(ModelFollowing(R.drawable.pf13, "Leanardo Dicaprio"))
        FollowingList.add(ModelFollowing(R.drawable.pf14, "Weeknd"))
        FollowingList.add(ModelFollowing(R.drawable.pf16, "Kendrick Lamar"))

        val rv4 = findViewById<RecyclerView>(R.id.recyclerView_following_list)
        rv4.layoutManager = LinearLayoutManager(this)
        rv4.adapter = AdapterFollowing(FollowingList)


    }
}