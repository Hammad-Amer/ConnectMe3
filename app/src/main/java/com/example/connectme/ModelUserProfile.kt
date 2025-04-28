package com.example.connectme

data class ModelUserProfile(
    val username: String,
    val profile_image: String, // Base64 encoded image string
    val post_count: Int,
    val follower_count: Int,
    val following_count: Int
)