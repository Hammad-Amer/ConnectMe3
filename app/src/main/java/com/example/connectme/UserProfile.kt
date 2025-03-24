package com.example.connectme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class UserProfile : AppCompatActivity() {

    private val images = intArrayOf(
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
        setContentView(R.layout.activity_user_profile)

        val gridView: GridView = findViewById(R.id.profile_grid_userprofile)
        val adapter = AdapterUserProfileImages(this, images)
        gridView.adapter = adapter

        val gotoeditprofile = findViewById<ImageView>(R.id.edit_profile_userprofile)
        gotoeditprofile.setOnClickListener {
            val intent = Intent(this, EditProfile::class.java)
            startActivity(intent)
        }

        val gotofollower = findViewById<LinearLayout>(R.id.follower_box)
        gotofollower.setOnClickListener {
            val intent = Intent(this, FollowersList::class.java)
            startActivity(intent)
        }
        val gotofollowing = findViewById<LinearLayout>(R.id.following_box)
        gotofollowing.setOnClickListener {
            val intent = Intent(this, FollowingList::class.java)
            startActivity(intent)
        }

        val logoutButton = findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginPage::class.java))
            finish()
        }



        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainFeedScreen::class.java))
                    true
                }
                R.id.nav_search -> {
                    startActivity(Intent(this, Search::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, NewPost::class.java))
                    true
                }
                R.id.nav_profile ->
                    true

                R.id.nav_contacts -> {
                    startActivity(Intent(this, Contacts::class.java))
                    true
                }
                else -> false
            }
        }
    }
}