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

class AdapterContact(
    private val context: Context,
    private val contactList: MutableList<ModelContact>,  // Changed to MutableList to allow updates
    private val onSendRequest: (String) -> Unit
) : RecyclerView.Adapter<AdapterContact.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val username: TextView = itemView.findViewById(R.id.username)
        val followButton: Button = itemView.findViewById(R.id.follow_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactList[position]
        holder.username.text = contact.username

        // Decode profile image
        val bitmap = decodeBase64ToBitmap(contact.profileImage)
        holder.profileImage.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.connectme_logo))

        // Update Follow Button state
        if (contact.isPending) {
            holder.followButton.text = "Pending"
            holder.followButton.isEnabled = false
        } else {
            holder.followButton.text = "Follow"
            holder.followButton.isEnabled = true
            holder.followButton.setOnClickListener {
                // Disable the button immediately and update the UI
                holder.followButton.text = "Pending"
                holder.followButton.isEnabled = false

                // Update contactList to reflect changes
                contactList[position].isPending = true
                notifyItemChanged(position)

                // Send follow request
                onSendRequest(contact.userId)
            }
        }
    }

    override fun getItemCount(): Int = contactList.size

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