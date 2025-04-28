package com.example.connectme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfile : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var adapter: AdapterUserProfileImages
    private lateinit var profilePic: com.google.android.material.imageview.ShapeableImageView
    private val imageBitmaps = mutableListOf<Bitmap>()  // Store Bitmaps instead of URIs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)

        gridView = findViewById(R.id.profile_grid_userprofile)
        profilePic = findViewById(R.id.Main_profile_pic_userprofile)

        adapter = AdapterUserProfileImages(this, imageBitmaps)
        gridView.adapter = adapter

        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)


        fetchUserProfile()
        fetchUserPosts()

        findViewById<ImageView>(R.id.edit_profile_userprofile).setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        findViewById<LinearLayout>(R.id.follower_box).setOnClickListener {
            startActivity(Intent(this, FollowersList::class.java))
        }

        findViewById<LinearLayout>(R.id.following_box).setOnClickListener {
            startActivity(Intent(this, FollowingList::class.java))
        }

        findViewById<Button>(R.id.logout).setOnClickListener {
            sharedPref.edit().clear().apply()

            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK  // Clear backstack
            startActivity(intent)
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
                R.id.nav_profile -> true
                R.id.nav_contacts -> {
                    startActivity(Intent(this, Contacts::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Error decoding image", Toast.LENGTH_SHORT).show()
            }
            null
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun fetchUserProfile() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        ApiClient.apiService.getUserProfile(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    val userProfile = response.body()

                    userProfile?.data?.let {
                        findViewById<TextView>(R.id.profile_name_userprofile).text = it.username
                        findViewById<TextView>(R.id.follower_count).text = it.follower_count.toString()
                        findViewById<TextView>(R.id.following_count).text = it.following_count.toString()

                        val bitmap = base64ToBitmap(it.pfp ?: "")
                        profilePic.setImageBitmap(bitmap)
                    }
                } else {
                    Log.e("UserProfile", "Error fetching profile: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@UserProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserProfile", "Network failure fetching profile", t)
            }
        })

    }

    private fun fetchUserPosts() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)
        Toast.makeText(this, "User ID: $userId", Toast.LENGTH_SHORT).show()

        if (userId != 0){
            ApiClient.apiService.getUserPosts(userId).enqueue(object : Callback<PostsResponse> {
                override fun onResponse(call: Call<PostsResponse>, response: Response<PostsResponse>) {
                    if (response.isSuccessful) {
                        val postsResponse = response.body()
                        val posts = postsResponse?.data

                        imageBitmaps.clear()
                        posts?.forEach { post ->
                            val bitmap = base64ToBitmap(post.image_base64)
                            if (bitmap != null) {
                                imageBitmaps.add(bitmap)
                            }
                        }

                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@UserProfile, "Error fetching posts", Toast.LENGTH_SHORT).show()
                        Log.e("UserProfile", "Error fetching posts: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<PostsResponse>, t: Throwable) {
                    Toast.makeText(this@UserProfile, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserProfile", "Network failure fetching posts", t)
                }
            })
        } else {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
        }
    }
}