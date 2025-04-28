package com.example.connectme

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterFollowing(private val followersList: List<ModelFollowing>) :
    RecyclerView.Adapter<AdapterFollowing.FollowersViewHolder>() {

    class FollowersViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.user_profile_image)
        val username: TextView = view.findViewById(R.id.textName_following)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowersViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_followers, parent, false)
        return FollowersViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowersViewHolder, position: Int) {
        val follower = followersList[position]
        holder.username.text = follower.username

        try {
            if (follower.profileBase64.isNotEmpty()) {
                val decodedBytes = Base64.decode(follower.profileBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                bitmap?.let {
                    holder.profileImage.setImageBitmap(it)
                } ?: holder.profileImage.setImageResource(R.drawable.connectme_logo)
            } else {
                holder.profileImage.setImageResource(R.drawable.connectme_logo)
            }
        } catch (e: Exception) {
            holder.profileImage.setImageResource(R.drawable.connectme_logo)
        }
    }

    override fun getItemCount(): Int = followersList.size
}