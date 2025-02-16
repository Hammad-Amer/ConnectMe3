package com.example.connectme

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class Search : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)


        val searchList = mutableListOf<ModelSearch>()
        searchList.add(ModelSearch("Kanye West"))
        searchList.add(ModelSearch( "Affan Ahmed Swati"))
        searchList.add(ModelSearch("Lil Durk"))
        searchList.add(ModelSearch("Raja Muhammad Adil Nadeem"))
        searchList.add(ModelSearch("Block Boy JB"))

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView_search_history)
        rv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rv.adapter = AdapterSearch(searchList)



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainFeedScreen::class.java))
                    true
                }
                R.id.nav_search ->
                    true

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