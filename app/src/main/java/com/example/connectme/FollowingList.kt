package com.example.connectme

import android.graphics.BitmapFactory
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class FollowingList : AppCompatActivity() {

    private lateinit var adapter: AdapterFollowing
    private val followingList = mutableListOf<ModelFollowing>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_following_list)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_following_list)
        adapter = AdapterFollowing(followingList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.go_backto_profile).setOnClickListener {
            finish()
        }

        fetchFollowing()
    }

    private fun fetchFollowing() {

        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val userId = sharedPref.getInt("userId", 0)


        val url = Globals.BASE_URL+"getfollowing.php?user_id=$userId"

        val requestQueue = Volley.newRequestQueue(this)

        val jsonRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                val status = response.getString("status")
                if (status == "success") {
                    followingList.clear()
                    val followingArray = response.getJSONObject("data").getJSONArray("following")

                    for (i in 0 until followingArray.length()) {
                        val obj = followingArray.getJSONObject(i)
                        val username = obj.getString("username")
                        val profilePicture = obj.optString("profile_picture", "")

                        followingList.add(ModelFollowing(profilePicture, username))
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    val message = response.optString("message", "Something went wrong")
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley", "Error: ${error.message}")
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(jsonRequest)
    }
}