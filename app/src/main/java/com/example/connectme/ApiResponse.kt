package com.example.connectme

// For generic API responses like login/register
data class ApiResponse(
    val status: String,
    val message: String,
    val username: String? = null,
    val userId: Int? = null,
    val pfp: String? = null
)

// For user profile fetching
data class UserProfileResponse(
    val status: String,
    val message: String? = null,
    val data: UserProfileData? = null
)

data class UserProfileData(
    val username: String,
    val email: String,
    val fullname: String,
    val phone: String,
    val pfp: String?, // nullable String
    val follower_count: Int, // new field
    val following_count: Int // new field
)

data class ApiResponseFeed(
    val status: String,      // "success" or "error"
    val message: String,     // A message giving more context about the response
    val data: FeedData? = null // The actual feed data (could be a list of stories or posts, depending on the endpoint)
)

data class FeedData(
    val stories: List<ModelStory>? = null,   // If the response is for stories, include this
    val posts: List<ModelFeedPosts>? = null  // If the response is for posts, include this
)


// For fetching posts
data class PostsResponse(
    val status: String,
    val data: List<PostResponse>
)

data class PostResponse(
    val id: Int,
    val caption: String,
    val image_base64: String, // Expecting the base64 string as per the response
    val like_count: Int,
    val liked_by: List<String>,
    val timestamp: String
)

data class ApiResponsebetter(
    val status: String,  // "success" or "error"
    val message: String  // Description of the response (success or error message)
)

data class ApiResponseStory<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)