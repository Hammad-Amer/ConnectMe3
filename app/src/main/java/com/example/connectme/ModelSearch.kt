package com.example.connectme

data class ModelSearch(
    val userId: String,
    val username: String,
    val profileImageUrl: String,
    var isFollowed: Boolean,
    var isPending: Boolean
)