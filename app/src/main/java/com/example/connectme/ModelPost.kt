package com.example.connectme

data class ModelPost(
    val postId: String = "",
    val userId: String = "",
    val imageBase64: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
