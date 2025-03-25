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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdapterChatMessageVanish(private val messages: List<ModelChat>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun getItemViewType(position: Int): Int {
        // Determine the view type based on whether the message is sent by the current user
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_SENT) {
            // Inflate vanish mode layout for sent messages
            val view = inflater.inflate(R.layout.chat_message_sent_vanish, parent, false)
            SentMessageVanishViewHolder(view)
        } else {
            // Inflate vanish mode layout for received messages
            val view = inflater.inflate(R.layout.chat_message_recieved_vanish, parent, false)
            ReceivedMessageVanishViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageVanishViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageVanishViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // ViewHolder for vanish mode sent messages
    inner class SentMessageVanishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_sent_vanish)
        private val timestampText: TextView = itemView.findViewById(R.id.text_timestamp_sent_vanish)

        fun bind(message: ModelChat) {
            messageText.text = message.message
            timestampText.text = formatTimestamp(message.timestamp)
        }
    }

    // ViewHolder for vanish mode received messages
    inner class ReceivedMessageVanishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.user_profile_received_vanish)
        private val messageText: TextView = itemView.findViewById(R.id.text_message_received_vanish)
        private val timestampText: TextView = itemView.findViewById(R.id.text_timestamp_received_vanish)

        fun bind(message: ModelChat) {
            messageText.text = message.message
            timestampText.text = formatTimestamp(message.timestamp)

            // Load the sender's profile image from Firebase
            val senderId = message.senderId
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
                            profileImage.setImageResource(R.drawable.pf1)
                        }
                    } else {
                        // If no profile image, set a placeholder
                        profileImage.setImageResource(R.drawable.pf1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdapterVanish", "Failed to load user data", error.toException())
                    profileImage.setImageResource(R.drawable.pf1)
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
