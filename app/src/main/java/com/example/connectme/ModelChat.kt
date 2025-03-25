package com.example.connectme

import com.google.firebase.database.Exclude

data class ModelChat(
    var message: String = "",
    val timestamp: Long = 0,
    val senderId: String = "",
    val receiverId: String = "",
    val vanish: Boolean = false,
    @get:Exclude var key: String? = null

)
