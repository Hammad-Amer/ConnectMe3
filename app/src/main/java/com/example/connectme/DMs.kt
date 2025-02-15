package com.example.connectme

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DMs : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dms)

        val DMsList = mutableListOf<ModelDMs>()
        DMsList.add(ModelDMs(R.drawable.pf6, "Raja Muhammad Adil Nadeem"))
        DMsList.add(ModelDMs(R.drawable.pf7, "Affan Ahmed Swati"))
        DMsList.add(ModelDMs(R.drawable.pf3, "Shayaan Khalid"))
        DMsList.add(ModelDMs(R.drawable.pf4, "Fatima"))
        DMsList.add(ModelDMs(R.drawable.pf5, "Arjit Singh"))

        val rv3 = findViewById<RecyclerView>(R.id.recyclerView_Dms)
        rv3.layoutManager = LinearLayoutManager(this)
        rv3.adapter =AdapterDMs(DMsList)

    }
}