package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
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

        val gotofeed = findViewById<ImageView>(R.id.cross_go_backto_feed)
        gotofeed.setOnClickListener {
            val intent = Intent(this, MainFeedScreen::class.java)
            startActivity(intent)
        }

        val next_newpost_2 = findViewById<TextView>(R.id.next_newpost)
        next_newpost_2.setOnClickListener {
            val intent = Intent(this, NewPost_screen::class.java)
            startActivity(intent)
        }

        val gotocam = findViewById<ImageView>(R.id.camera)
        gotocam.setOnClickListener {
            val intent = Intent(this, TakePicture::class.java)
            startActivity(intent)
        }


    }
}