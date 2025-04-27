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
import com.android.volley.toolbox.ImageLoader
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class AdapterChatMessageVanish(
    private val messages: List<ModelChat>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val view = inflater.inflate(R.layout.chat_message_sent_vanish, parent, false)
            SentMessageVanishViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.chat_message_recieved_vanish, parent, false)
            ReceivedMessageVanishViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageVanishViewHolder) holder.bind(message)
        else if (holder is ReceivedMessageVanishViewHolder) holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageVanishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_sent_vanish)
        private val timestampText: TextView = itemView.findViewById(R.id.text_timestamp_sent_vanish)

        fun bind(message: ModelChat) {
            messageText.text = message.message
            timestampText.text = formatTimestamp(message.timestamp)
        }
    }

    inner class ReceivedMessageVanishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.user_profile_received_vanish)
        private val messageText: TextView = itemView.findViewById(R.id.text_message_received_vanish)
        private val timestampText: TextView = itemView.findViewById(R.id.text_timestamp_received_vanish)

        fun bind(message: ModelChat) {
            messageText.text = message.message
            timestampText.text = formatTimestamp(message.timestamp)
            loadProfileImage(message.senderId)
        }

        private fun loadProfileImage(senderId: String) {
            val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(senderId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val base64String = snapshot.child("profileImage").value?.toString()
                    if (!base64String.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profileImage.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("AdapterVanish", "Error decoding profile image", e)
                            profileImage.setImageResource(R.drawable.hard_pfp)
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.hard_pfp)
                    }
                }




                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdapterVanish", "Failed to load profile image", error.toException())
                    profileImage.setImageResource(R.drawable.hard_pfp)
                }
            })
        }
    }

    private fun formatTimestamp(timestamp: Long?): String {
        return timestamp?.let {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(it))
        } ?: ""
    }
}
