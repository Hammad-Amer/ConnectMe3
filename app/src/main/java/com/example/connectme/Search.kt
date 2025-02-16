package com.example.connectme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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
    }
}