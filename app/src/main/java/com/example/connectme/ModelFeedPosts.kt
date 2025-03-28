package com.example.connectme

data class ModelFeedPosts(
    val username: String = "",
    val userId: String = "",
    val image: String = "",
    val post: String = "",
    val caption: String = "",
    val timestamp: Long = 0,
    var isLiked: Boolean = false,
    val postId: String = "",
    var likeCount: Int = 0
)