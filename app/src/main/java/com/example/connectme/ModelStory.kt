package com.example.connectme


data class ModelStory(
    var type: Int,
    var userId: Int,
    var profileImage: String,
    var mediaData: String? = null,
    var mediaType: String? = null,
    var timestamp: Long? = null,
    var hasActiveStory: Boolean = false // New field
)
