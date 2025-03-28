package com.example.connectme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterSearch(
    private val context: Context,
    private val searchList: MutableList<ModelSearch>,  // Changed to MutableList to allow updates
    private val onFollowClick: (String) -> Unit
) : RecyclerView.Adapter<AdapterSearch.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val username: TextView = itemView.findViewById(R.id.textName_search_history)
        val followButton: Button = itemView.findViewById(R.id.follow_button)
        val crossButton: ImageView = itemView.findViewById(R.id.cross_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.search_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = searchList[position]
        holder.username.text = item.username

        // Decode profile image
        val bitmap = decodeBase64ToBitmap(item.profileImageUrl)
        holder.profileImage.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.connectme_logo))

        // Handle follow button state based on pending and follow status
        if (item.isPending) {
            holder.followButton.text = "Pending"
            holder.followButton.isEnabled = false
        } else {
            holder.followButton.text = if (item.isFollowed) "Following" else "Follow"
            holder.followButton.isEnabled = !item.isFollowed
        }

        // Follow button click logic
        holder.followButton.setOnClickListener {
            if (!item.isPending) {
                // Disable button and update UI
                holder.followButton.text = "Pending"
                holder.followButton.isEnabled = false

                searchList[position].isPending = true
                notifyItemChanged(position)

                onFollowClick(item.userId)
            }
        }

        holder.crossButton.visibility = if (item.isPending) View.VISIBLE else View.GONE
        holder.crossButton.setOnClickListener {
            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            val receiverId = item.userId

            val requestRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("Requests")
                .child(receiverId)
                .child("pending")
                .child(currentUserId)

            requestRef.removeValue().addOnSuccessListener {
                searchList[position].isPending = false
                notifyItemChanged(position)
            }.addOnFailureListener {
                android.widget.Toast.makeText(context, "Failed to unsend request", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = searchList.size

    private fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        return try {
            if (base64String.isNullOrEmpty()) return null
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}