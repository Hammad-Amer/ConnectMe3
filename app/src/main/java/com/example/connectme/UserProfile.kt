package com.example.connectme

import android.os.Bundle
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UserProfile : AppCompatActivity() {

    private val images = intArrayOf(
        R.drawable.userprofile_img1, R.drawable.userprofile_img2, R.drawable.userprofile_img3,
        R.drawable.userprofile_img4, R.drawable.userprofile_img5, R.drawable.userprofile_img6,
        R.drawable.userprofile_img7
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        val gridView: GridView = findViewById(R.id.profile_grid_userprofile)
        val adapter = AdapterUserProfileImages(this, images)
        gridView.adapter = adapter
    }
}