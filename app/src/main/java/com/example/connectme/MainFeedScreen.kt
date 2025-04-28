package com.example.connectme

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.connectme.FirebaseConsts.USER_PATH
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFeedScreen : AppCompatActivity() {

    private val storyList = mutableListOf<ModelStory>()
    private lateinit var adapterStory: AdapterStory
    private val postsList = mutableListOf<ModelFeedPosts>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_feed_screen)

        adapterStory = AdapterStory(
            storyList,
            { story -> handleStoryClick(story) },
            { story -> handleStoryLongClick(story) }
        )

        setupRecyclerView()
        loadInitialData()
        setupUIComponents()



        //sendNotification()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(applicationContext)
                .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {

                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

                    }

                    override  fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
                        p1?.continuePermissionRequest()
                    }

                }).check()

        }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)


    }

    private fun sendNotification() {
        FirebaseDatabase.getInstance().getReference(USER_PATH).child(FirebaseAuth.getInstance().uid!!).child("token")
            .get().addOnSuccessListener {
                val token = it.getValue(String::class.java)

                val notification = Notification(
                    message = NotificationData(
                        token = token,
                        hashMapOf("title" to "Hello","body" to "Helllooooooo")

                    )
                )

                NotificationApi.create().sendNotification(notification)
                    .enqueue(object : Callback<Notification> {
                        override fun onResponse(
                            p0: Call<Notification>,
                            p1: Response<Notification>
                        ) {
                            Toast.makeText(this@MainFeedScreen, "Notification sent", Toast.LENGTH_SHORT).show()
                        }

                        override fun onFailure(p0: Call<Notification>, p1: Throwable) {
                            Toast.makeText(this@MainFeedScreen, "error ${p1.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
    }

    private fun setupRecyclerView() {
        findViewById<RecyclerView>(R.id.Story_recyclerview_mainfeed).apply {
            layoutManager = LinearLayoutManager(
                this@MainFeedScreen,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = adapterStory
            setHasFixedSize(true)
        }
    }


    private fun loadInitialData() {
        // checkAndDeleteExpiredStory()
        fetchStoriesFromFollowing()
    }

    private fun setupUIComponents() {
        setupFeedPostsRecyclerView()
        setupDMButton()
        setupBottomNavigation()
    }

    private fun handleStoryClick(story: ModelStory) {
        if (story.type == AdapterStory.USER_STORY) {
            if (story.hasActiveStory) {
                openStoryViewer(story.userId)
            } else {
                // No story: treat click as intent to upload
                openUploadStoryPage()
            }
        } else if (story.hasActiveStory) {
            openStoryViewer(story.userId)
        }
    }

    private fun handleStoryLongClick(story: ModelStory) {
        if (story.type == AdapterStory.USER_STORY) {
            openUploadStoryPage()
        }
    }

    private fun openStoryViewer(userId: Int) {
        val intent = Intent(this, StoryView::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun fetchStoriesFromFollowing() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = Globals.BASE_URL+"fetchstories.php?user_id=$currentUserId"

        val requestQueue = Volley.newRequestQueue(this)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnected

        // If there is no internet connection, fetch cached stories
        if (!isConnected) {
            fetchCachedStories()
        } else {
            // If there is an internet connection, fetch stories from Volley
            var hasActiveStory1 = false
            val request = object : StringRequest(
                Method.GET, url,
                { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        if (jsonObject.getBoolean("success")) {
                            storyList.clear()

                            val storiesArray = jsonObject.getJSONArray("data")

                            // First loop: check and add the user's story
                            for (i in 0 until storiesArray.length()) {
                                val storyObj = storiesArray.getJSONObject(i)

                                if (storyObj.getInt("userId") == currentUserId) {
                                    hasActiveStory1 = true
                                    val story = ModelStory(
                                        type = AdapterStory.USER_STORY,
                                        userId = storyObj.getInt("userId"),
                                        profileImage = storyObj.getString("profileImage") ?: "",
                                        mediaData = storyObj.getString("mediaData"),
                                        mediaType = storyObj.getString("mediaType"),
                                        timestamp = storyObj.getLong("timestamp"),
                                        hasActiveStory = true
                                    )
                                    storyList.add(story)
                                }
                            }

                            // If no active story from the user, add an empty one
                            if (!hasActiveStory1) {
                                val story = ModelStory(
                                    type = AdapterStory.USER_STORY,
                                    userId = currentUserId,
                                    profileImage = sharedPref.getString("profilePicture", "") ?: "",
                                    mediaData = "",
                                    mediaType = "",
                                    timestamp = 0L,
                                    hasActiveStory = false
                                )
                                storyList.add(story)
                            }

                            // Add other users' stories
                            for (i in 0 until storiesArray.length()) {
                                val storyObj = storiesArray.getJSONObject(i)
                                if (storyObj.getInt("userId") == currentUserId) continue

                                val story = ModelStory(
                                    type = AdapterStory.OTHER_STORY,
                                    userId = storyObj.getInt("userId"),
                                    profileImage = storyObj.getString("profileImage") ?: "",
                                    mediaData = storyObj.getString("mediaData"),
                                    mediaType = storyObj.getString("mediaType"),
                                    timestamp = storyObj.getLong("timestamp"),
                                    hasActiveStory = true
                                )
                                storyList.add(story)
                            }

                            // Update the adapter
                            adapterStory.notifyDataSetChanged()

                            // Cache the newly fetched stories
                            cacheFetchedStories(storiesArray)
                        } else {
                            Toast.makeText(this, "No stories found", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("Volley", "JSON error: ${e.message}")
                    }
                },
                { error ->
                    Log.e("Volley", "Error: ${error.message}")
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
                }
            ) {}
            requestQueue.add(request)
        }
    }

    // Fetch stories from the cache
    private fun fetchCachedStories() {
        val dbHelper = dbHelper(this)
        val cachedStories = dbHelper.getCachedStories()

        if (cachedStories.isNotEmpty()) {
            storyList.clear()
            storyList.addAll(cachedStories)
            adapterStory.notifyDataSetChanged()
        } else {
            Toast.makeText(this, "No cached stories found", Toast.LENGTH_SHORT).show()
        }
    }

    // Cache the fetched stories into the database
    private fun cacheFetchedStories(storiesArray: JSONArray) {
        val dbHelper = dbHelper(this)

        // Clear old cache first
        dbHelper.clearCachedStories()

        for (i in 0 until storiesArray.length()) {
            val storyObj = storiesArray.getJSONObject(i)

            // Build a ModelStory just for convenience
            val story = ModelStory(
                type = AdapterStory.OTHER_STORY,
                userId = storyObj.getInt("userId"),
                profileImage = storyObj.getString("profileImage") ?: "",
                mediaData = storyObj.getString("mediaData"),
                mediaType = storyObj.getString("mediaType"),
                timestamp = storyObj.getLong("timestamp"),
                hasActiveStory = true
            )

            // Now cache ALL required fields:
            dbHelper.cacheStory(
                story.userId,              // Int
                story.profileImage,        // String
                story.mediaType ?: "",     // String
                story.mediaData ?: "",     // String
                story.timestamp ?: 0L      // Long
            )
        }
    }

    private fun setupFeedPostsRecyclerView() {
        fetchFeedPostsFromFollowing()
    }

    private fun fetchFeedPostsFromFollowing() {
        val sharedPref = getSharedPreferences("ConnectMePref", MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("userId", 0)
        val url = Globals.BASE_URL+"get_feed_posts.php?userId=$currentUserId"
        val dbHelper = dbHelper(this)

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnected

        if (!isConnected) {

            // No internet: Load cached posts
            val cachedPosts = dbHelper.getCachedFeedPosts()
            if (cachedPosts.isNotEmpty()) {
                postsList.clear()
                postsList.addAll(cachedPosts.sortedByDescending { it.timestamp })
                updatePostsRecyclerView(postsList)
                Toast.makeText(this, "Loaded cached posts", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No internet & no cached posts", Toast.LENGTH_SHORT).show()
            }
            return

        }

        // Online: Fetch from server
        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    if (success) {
                        val postsArray = jsonResponse.getJSONArray("data")
                        postsList.clear()
                        val fetchedPosts = mutableListOf<ModelFeedPosts>()

                        for (i in 0 until postsArray.length()) {
                            val postObj = postsArray.getJSONObject(i)
                            val post = ModelFeedPosts(
                                postId = postObj.getString("postId"),
                                userId = postObj.getString("userId"),
                                username = postObj.getString("username"),
                                image = postObj.getString("pfp"),
                                post = postObj.getString("postImage"),
                                caption = postObj.getString("caption"),
                                timestamp = postObj.getLong("timestamp"),
                                likeCount = postObj.getInt("likeCount"),
                                isLiked = postObj.getBoolean("isLiked")
                            )
                            fetchedPosts.add(post)
                        }

                        // Sort and update UI
                        postsList.addAll(fetchedPosts.sortedByDescending { it.timestamp })
                        updatePostsRecyclerView(postsList)

                        // Cache the latest posts
                        dbHelper.clearCachedPosts()
                        dbHelper.cacheFeedPosts(fetchedPosts)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Failed to fetch posts", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return mapOf("userId" to currentUserId.toString())
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun updatePostsRecyclerView(postsList: List<ModelFeedPosts>) {
        val rvPosts = findViewById<RecyclerView>(R.id.feed_posts_recyclerview)
        rvPosts.layoutManager = LinearLayoutManager(this)
        rvPosts.adapter = AdapterFeedPosts(postsList)
    }

    private fun checkAndDeleteExpiredStory() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storiesRef = FirebaseDatabase.getInstance().getReference("Users/$currentUserId/stories")
        storiesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    if (System.currentTimeMillis() - timestamp >= 86400000) {
                        storiesRef.removeValue().addOnSuccessListener {
                            Log.d("MainFeedScreen", "Expired story removed")
                            adapterStory.notifyDataSetChanged()
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MainFeedScreen", "Error checking story expiration", error.toException())
            }
        })
    }



    private fun openUploadStoryPage() {
        startActivity(Intent(this, UploadStory::class.java))
    }

    private fun setupDMButton() {
        findViewById<ImageView>(R.id.connectme_dm).setOnClickListener {
            startActivity(Intent(this, DMs::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, Search::class.java))
                    true
                }
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