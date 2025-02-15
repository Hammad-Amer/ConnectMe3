package com.example.connectme

class ModelChat (
    val message: String,
    val timestamp: String,
    val isSentByUser: Boolean,
    val profileImage: Int? = null
)