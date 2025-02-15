package com.example.connectme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class AdapterFollowing(private val followerList: List<ModelFollowing>) : RecyclerView.Adapter<AdapterFollowing.FollowingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_followers, parent, false)
        return FollowingViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        val dm = followerList[position]
        holder.name.text = dm.name
        holder.profileImage.setImageResource(dm.image)
    }

    override fun getItemCount(): Int = followerList.size

    class FollowingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ShapeableImageView = itemView.findViewById(R.id.user_profile_image)
        val name: TextView = itemView.findViewById(R.id.textName_following)
        val cameraIcon: ImageView = itemView.findViewById(R.id.imageCamera)
    }
}
