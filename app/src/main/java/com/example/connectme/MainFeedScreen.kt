package com.example.connectme

import android.Manifest
import android.content.Intent
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

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainFeedScreen : AppCompatActivity() {

    private val storyList = mutableListOf<ModelStory>()
    private lateinit var adapterStory: AdapterStory


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



        sendNotification()

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
        checkAndDeleteExpiredStory()
        fetchStoriesFromFollowing()
    }

    private fun setupUIComponents() {
        setupFeedPostsRecyclerView()
        setupDMButton()
        setupBottomNavigation()
    }

    private fun handleStoryClick(story: ModelStory) {
        if (story.hasActiveStory) {
            openStoryViewer(story.userId)
        } else if (story.type == AdapterStory.USER_STORY) {
            Toast.makeText(this, "Press and hold to add story", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleStoryLongClick(story: ModelStory) {
        if (story.type == AdapterStory.USER_STORY) {
            openUploadStoryPage()
        }
    }

    private fun fetchStoriesFromFollowing() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        storyList.clear()

        database.child("Users/$currentUserId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(currentUserSnapshot: DataSnapshot) {
                val currentUserProfile = currentUserSnapshot.child("profileImage").getValue(String::class.java) ?: ""
                val currentUserStorySnapshot = currentUserSnapshot.child("stories")
                val hasActiveStory = currentUserStorySnapshot.exists() &&
                        (System.currentTimeMillis() - (currentUserStorySnapshot.child("timestamp").getValue(Long::class.java)
                            ?: 0L) < 86400000)

                storyList.add(ModelStory(
                    type = AdapterStory.USER_STORY,
                    userId = currentUserId,
                    profileImage = currentUserProfile,
                    mediaData = if (hasActiveStory) currentUserStorySnapshot.child("mediaData").getValue(String::class.java) else null,
                    mediaType = if (hasActiveStory) currentUserStorySnapshot.child("mediaType").getValue(String::class.java) else null,
                    timestamp = if (hasActiveStory) currentUserStorySnapshot.child("timestamp").getValue(Long::class.java) else null,
                    hasActiveStory = hasActiveStory
                ))

                database.child("Users/$currentUserId/following")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(followingSnapshot: DataSnapshot) {
                            followingSnapshot.children.forEach { child ->
                                val followedUserId = child.key ?: return@forEach
                                if (followedUserId == currentUserId) return@forEach

                                database.child("Users/$followedUserId").addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                        val profileImage = userSnapshot.child("profileImage").getValue(String::class.java) ?: ""
                                        val storySnapshot = userSnapshot.child("stories")
                                        val hasActiveStory = storySnapshot.exists() &&
                                                (System.currentTimeMillis() - (storySnapshot.child("timestamp").getValue(Long::class.java)
                                                    ?: 0L) < 86400000)

                                        if (hasActiveStory) {
                                            storyList.add(ModelStory(
                                                type = AdapterStory.OTHER_STORY,
                                                userId = followedUserId,
                                                profileImage = profileImage,
                                                mediaData = storySnapshot.child("mediaData").getValue(String::class.java),
                                                mediaType = storySnapshot.child("mediaType").getValue(String::class.java),
                                                timestamp = storySnapshot.child("timestamp").getValue(Long::class.java),
                                                hasActiveStory = true
                                            ))
                                        }
                                        adapterStory.notifyDataSetChanged()
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e("MainFeedScreen", "Error loading user $followedUserId", error.toException())
                                    }
                                })
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                            Log.e("MainFeedScreen", "Error loading follows", error.toException())
                        }
                    })
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("MainFeedScreen", "Error loading current user", error.toException())
            }
        })
    }

    private fun setupFeedPostsRecyclerView() {
        fetchFeedPostsFromFollowing()
    }

    private fun fetchFeedPostsFromFollowing() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference
        val postsList = mutableListOf<ModelFeedPosts>()

        database.child("Users/$currentUserId/following")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(followingSnapshot: DataSnapshot) {
                    val followingUserIds = mutableListOf<String>()
                    followingSnapshot.children.forEach { child ->
                        child.key?.let { followingUserIds.add(it) }
                    }
                    followingUserIds.add(currentUserId)

                    for (userId in followingUserIds) {
                        database.child("Users/$userId/posts")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(postsSnapshot: DataSnapshot) {
                                    for (postSnapshot in postsSnapshot.children) {
                                        val postId = postSnapshot.key ?: continue
                                        val caption = postSnapshot.child("caption").getValue(String::class.java) ?: ""
                                        val imageBase64 = postSnapshot.child("imageBase64").getValue(String::class.java) ?: ""
                                        val timestamp = postSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                                        val likesSnapshot = postSnapshot.child("likes")
                                        val likeCount = likesSnapshot.childrenCount.toInt()
                                        val isLiked = likesSnapshot.hasChild(currentUserId)

                                        database.child("Users/$userId/profileImage")
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(profileSnapshot: DataSnapshot) {
                                                    val profileImageBase64 = profileSnapshot.getValue(String::class.java) ?: ""

                                                    database.child("Users/$userId/username")
                                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(usernameSnapshot: DataSnapshot) {
                                                                val username = usernameSnapshot.getValue(String::class.java) ?: userId
                                                                val postModel = ModelFeedPosts(
                                                                    postId = postId,
                                                                    userId = userId,
                                                                    username = username,
                                                                    image = profileImageBase64,
                                                                    post = imageBase64,
                                                                    caption = caption,
                                                                    timestamp = timestamp,
                                                                    likeCount = likeCount,
                                                                    isLiked = isLiked
                                                                )
                                                                postsList.add(postModel)
                                                                postsList.sortByDescending { it.timestamp }
                                                                updatePostsRecyclerView(postsList)
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {}
                                                        })
                                                }

                                                override fun onCancelled(error: DatabaseError) {}
                                            })
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("MainFeedScreen", "Error fetching posts for user $userId", error.toException())
                                }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainFeedScreen", "Error fetching following list", error.toException())
                }
            })
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

    private fun openStoryViewer(userId: String) {
        startActivity(Intent(this, StoryView::class.java).apply {
            putExtra("USER_ID", userId)
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
