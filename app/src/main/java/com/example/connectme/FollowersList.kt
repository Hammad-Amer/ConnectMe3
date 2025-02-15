package com.example.connectme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FollowersList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_followers_list)

        val FollowersList = mutableListOf<ModelFollowing>()
        FollowersList.add(ModelFollowing(R.drawable.pf8, "Bambi"))
        FollowersList.add(ModelFollowing(R.drawable.pf9, "Selvester Stallone"))
        FollowersList.add(ModelFollowing(R.drawable.pf10, "Maryam Nawaz"))
        FollowersList.add(ModelFollowing(R.drawable.pf11, "Altaf Hussain"))
        FollowersList.add(ModelFollowing(R.drawable.pf12, "Imran Khan"))
        FollowersList.add(ModelFollowing(R.drawable.pf13, "Leanardo Dicaprio"))
        FollowersList.add(ModelFollowing(R.drawable.pf14, "Weeknd"))
        FollowersList.add(ModelFollowing(R.drawable.pf16, "Kendrick Lamar"))
        FollowersList.add(ModelFollowing(R.drawable.pf17, "Drake"))
        FollowersList.add(ModelFollowing(R.drawable.pf18, "Playboi Carti"))
        FollowersList.add(ModelFollowing(R.drawable.pf19, "Lil Uzi Vert"))
        FollowersList.add(ModelFollowing(R.drawable.pf20, "Kanye West"))
        FollowersList.add(ModelFollowing(R.drawable.pf21, "Travis Scott"))


        val rv4 = findViewById<RecyclerView>(R.id.recyclerView_followers_list)
        rv4.layoutManager = LinearLayoutManager(this)
        rv4.adapter = AdapterFollowing(FollowersList)


    }
}