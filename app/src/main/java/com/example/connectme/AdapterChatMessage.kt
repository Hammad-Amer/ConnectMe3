package com.example.connectme

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdapterChatMessage(
    private val messages: List<ModelChat>,
    private val currentUserId: String,
    private val onMessageLongClick: (ModelChat, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            val view = inflater.inflate(R.layout.chat_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.chat_message_recieved, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }

        holder.itemView.setOnLongClickListener {
            onMessageLongClick(message, position)
            true
        }
    }




    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_sent)
        private val timestampText: TextView = itemView.findViewById(R.id.text_timestamp_sent)

        fun bind(message: ModelChat) {
            messageText.text = message.message
            timestampText.text = formatTimestamp(message.timestamp)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ShapeableImageView = itemView.findViewById(R.id.user_profile_received)
        private val messageText: TextView = itemView.findViewById(R.id.text_message_received)
        private val timestampText: TextView = itemView.findViewById(R.id.text_timestamp_received)

        fun bind(message: ModelChat) {
            messageText.text = message.message
            timestampText.text = formatTimestamp(message.timestamp)

            loadProfileImage(message.senderId)
        }

        private fun loadProfileImage(senderId: String) {
            val userRef = FirebaseDatabase.getInstance().getReference("Users").child(senderId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val base64String = snapshot.child("profileImage").value?.toString()
                    if (!base64String.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            profileImage.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            Log.e("AdapterChatMessage", "Error decoding profile image", e)
                            profileImage.setImageResource(R.drawable.hard_pfp)
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.hard_pfp)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdapterChatMessage", "Failed to load user profile image", error.toException())
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
