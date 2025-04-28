package com.example.connectme

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("signup.php")
    fun registerUser(
        @Field("username") username: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("fullname") fullname: String,
        @Field("phone") phone: String
    ): Call<ApiResponse>

    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ApiResponse>


    @GET("fetchStories.php")
    fun fetchStories(
        @Query("user_id") user_id: Int? = 3
    ): Call<ApiResponseStory<List<ModelStory>>>


//        @FormUrlEncoded
//        @POST("uploadstory.php")
//        fun uploadStory(
//            @Field("userId") userId: Int,
//            @Field("mediaType") mediaType: String,
//            @Field("mediaData") mediaData: String
//        ): Call<ApiResponse>


    @GET("feed_posts.php")
    fun fetchFeedPosts(): Call<ApiResponseFeed>

    @FormUrlEncoded
    @POST("uploadstories.php")
    fun uploadStory(
        @Field("userId") userId: String,
        @Field("media") media: String,
        @Field("mediaType") mediaType: String
    ): Call<ApiResponse>

    @GET("profile.php")
    fun getUserProfile(
        @retrofit2.http.Query("user_id") userId: Int
    ): Call<UserProfileResponse>

    @GET("fetchpost.php")
    fun getUserPosts(
        @retrofit2.http.Query("user_id") userId: Int
    ): Call<PostsResponse>

    @FormUrlEncoded
    @POST("edit_profile.php")
    fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("username") username: String,
        @Field("fullname") fullname: String,
        @Field("phone") phone: String,
        @Field("pfp") pfp: String?
    ): Call<ApiResponsebetter>
}