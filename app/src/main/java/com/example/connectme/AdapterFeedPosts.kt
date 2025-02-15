package com.example.connectme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterFeedPosts(private val feedPostsList: List<ModelFeedPosts>) : RecyclerView.Adapter<AdapterFeedPosts.FeedPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_post, parent, false)
        return FeedPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int) {
        val post = feedPostsList[position]
        holder.username.text = post.username
        holder.username2.text = post.username
        holder.status.text = post.status
        holder.profileImage.setImageResource(post.image)
        holder.postImage.setImageResource(post.post)


    }

    override fun getItemCount(): Int = feedPostsList.size

    class FeedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.user_profile_image)
        val postImage: ImageView = itemView.findViewById(R.id.feed_post_image)
        val username: TextView = itemView.findViewById(R.id.feed_post_username)
        val username2: TextView = itemView.findViewById(R.id.feed_post_username2)
        val status: TextView = itemView.findViewById(R.id.feed_post_userdescription)
        val heartButton: ImageButton = itemView.findViewById(R.id.feed_post_heart)
        val commentButton: ImageButton = itemView.findViewById(R.id.feed_post_comment)
        val shareButton: ImageButton = itemView.findViewById(R.id.feed_post_share)
    }
}
