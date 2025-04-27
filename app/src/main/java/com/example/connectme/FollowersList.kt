package com.example.connectme

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class FollowersList : AppCompatActivity() {

    private lateinit var adapter: AdapterFollowing
    private val followersList = mutableListOf<ModelFollowing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_followers_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_followers_list)
        adapter = AdapterFollowing(followersList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.go_backto_profile).setOnClickListener { finish() }

        fetchFollowers()
    }

    private fun fetchFollowers() {

        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val url = Globals.BASE_URL+"getfollowers.php?user_id=$userId"
        Log.d("FollowersList", "Fetching followers from: $url")

        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                try {

                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        val followersArray = json.getJSONObject("data").getJSONArray("followers")

                        followersList.clear()
                        for (i in 0 until followersArray.length()) {
                            val follower = followersArray.getJSONObject(i)
                            val username = follower.getString("username")
                            val profilePicture = follower.getString("profile_picture")

                            try {
                                Base64.decode(profilePicture, Base64.DEFAULT)
                            } catch (e: Exception) {
                                Log.e("FollowersList", "Invalid Base64 image: ${e.message}")
                            }

                            followersList.add(ModelFollowing(profilePicture, username))
                        }

                        adapter.notifyDataSetChanged()
                        Log.d("FollowersList", "Loaded ${followersList.size} followers")
                    } else {
                        Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                        Log.w("FollowersList", "Error response: ${json.getString("message")}")
                    }
                } catch (e: Exception) {
                    Log.e("FollowersList", "JSON parsing error: ${e.message}")
                    Toast.makeText(this, "Response error", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("FollowersList", "Volley error: ${error.message}")
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}