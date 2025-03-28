package com.example.connectme

data class ModelContact(
    val userId: String = "",
    val username: String = "",
    val profileImage: String = "",
    var isFollowed: Boolean = false,
    var isPending: Boolean = false
)
