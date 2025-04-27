package com.example.connectme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdapterFeedPosts(private val feedPostsList: List<ModelFeedPosts>) : RecyclerView.Adapter<AdapterFeedPosts.FeedPostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_post, parent, false)
        return FeedPostViewHolder(view)
    }




    override fun onBindViewHolder(holder: FeedPostViewHolder, position: Int) {
        val post = feedPostsList[position]

        holder.username.text = post.username
        holder.username2.text = post.username
        holder.captionText.text = post.caption
        holder.profileImage.setImageBitmap(decodeBase64ToBitmap(post.image))
        holder.postImage.setImageBitmap(decodeBase64ToBitmap(post.post))
        holder.likecount1.text = "${post.likeCount}"

        if (post.isLiked) {
            holder.filledHeart.visibility = View.VISIBLE
            holder.emptyHeart.visibility = View.GONE
        } else {
            holder.emptyHeart.visibility = View.VISIBLE
        }

        val heartClickListener = View.OnClickListener {
            val context = holder.itemView.context

            // Get the current logged-in userId from SharedPreferences
            val sharedPref = context.getSharedPreferences("ConnectMePref", MODE_PRIVATE)
            val userId = sharedPref.getInt("userId", 0)
            val postId = post.postId

            val newLikeState = !post.isLiked
            post.isLiked = newLikeState
            if (newLikeState) post.likeCount++ else post.likeCount--

            holder.filledHeart.visibility = if (newLikeState) View.VISIBLE else View.GONE
            holder.emptyHeart.visibility = if (newLikeState) View.GONE else View.VISIBLE
            holder.likecount1.text = "${post.likeCount}"

            val url = "http://10.0.2.2/connectme/like_post.php"

            val stringRequest = object : StringRequest(
                Method.POST, url,
                { response ->
                    if (!response.contains("true")) {
                        post.isLiked = !newLikeState
                        if (newLikeState) post.likeCount-- else post.likeCount++
                        holder.filledHeart.visibility = if (!newLikeState) View.VISIBLE else View.GONE
                        holder.emptyHeart.visibility = if (!newLikeState) View.GONE else View.VISIBLE
                        holder.likecount1.text = "${post.likeCount}"
                        Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    post.isLiked = !newLikeState
                    if (newLikeState) post.likeCount-- else post.likeCount++
                    holder.filledHeart.visibility = if (!newLikeState) View.VISIBLE else View.GONE
                    holder.emptyHeart.visibility = if (!newLikeState) View.GONE else View.VISIBLE
                    holder.likecount1.text = "${post.likeCount}"
                    Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "user_id" to userId.toString(),
                        "post_id" to postId
                    )
                }
            }

            // Add the request to the queue
            Volley.newRequestQueue(context).add(stringRequest)

            // Update the item in the adapter
            notifyItemChanged(position)
        }

        // Set listeners for the heart buttons
        holder.filledHeart.setOnClickListener(heartClickListener)
        holder.emptyHeart.setOnClickListener(heartClickListener)
    }

    override fun getItemCount(): Int = feedPostsList.size

    class FeedPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.user_profile_image)
        val postImage: ImageView = itemView.findViewById(R.id.feed_post_image)
        val username: TextView = itemView.findViewById(R.id.feed_post_username)
        val username2: TextView = itemView.findViewById(R.id.feed_post_username2)
        val captionText: TextView = itemView.findViewById(R.id.caption)
        val heartButton: ImageButton = itemView.findViewById(R.id.feed_post_heart)
        val commentButton: ImageButton = itemView.findViewById(R.id.feed_post_comment)
        val shareButton: ImageButton = itemView.findViewById(R.id.feed_post_share)
        val likecount1: TextView = itemView.findViewById(R.id.likecount)


        val filledHeart: ImageButton = itemView.findViewById(R.id.feed_post_heart)
        val emptyHeart: ImageButton = itemView.findViewById(R.id.emptyheart)
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}