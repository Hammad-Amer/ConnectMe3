package com.example.connectme

import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPost : AppCompatActivity() {

    private val Gimages = intArrayOf(
        R.drawable.userprofile_img1, R.drawable.userprofile_img2, R.drawable.userprofile_img3,
        R.drawable.userprofile_img4, R.drawable.userprofile_img5, R.drawable.userprofile_img6,
        R.drawable.userprofile_img7, R.drawable.userprofile_img8, R.drawable.userprofile_img9,
        R.drawable.userprofile_img10, R.drawable.userprofile_img11, R.drawable.userprofile_img12,
        R.drawable.userprofile_img13, R.drawable.userprofile_img14, R.drawable.userprofile_img15,
        R.drawable.userprofile_img16
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)

        val gridView2: GridView = findViewById(R.id.new_post_grid)
        val adapter = AdapterUserProfileImages(this, Gimages)
        gridView2.adapter = adapter

    }
}