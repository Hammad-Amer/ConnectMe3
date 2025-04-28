package com.example.connectme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class AdapterSearch(
    private val context: Context,
    private val searchList: MutableList<ModelSearch>,
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
        val item = searchList[holder.adapterPosition]
        holder.username.text = item.username

        // Decode profile image
        val bitmap = decodeBase64ToBitmap(item.profileImageUrl)
        holder.profileImage.setImageBitmap(bitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.connectme_logo))

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
                holder.followButton.text = "Pending"
                holder.followButton.isEnabled = false

                searchList[holder.adapterPosition].isPending = true
                notifyItemChanged(holder.adapterPosition)

                onFollowClick(item.userId)
            }
        }

        holder.crossButton.visibility = if (item.isPending) View.VISIBLE else View.GONE
        holder.crossButton.setOnClickListener {
            val receiverId = item.userId

            val sharedPref = context.getSharedPreferences("ConnectMePref", Context.MODE_PRIVATE)
            val currentUserId = sharedPref.getInt("userId", 0)
            val url = "http://10.0.2.2/connectme/reject_request.php"


            val stringRequest = object : StringRequest(Method.POST, url,
                { response ->
                    Toast.makeText(context, "Request Unsent", Toast.LENGTH_SHORT).show()
                    Log.d("rejectRequest", "Response: $response")

                },
                { error ->
                    Log.e("rejectRequest", "Error: ${error.message}")
                }) {
                override fun getParams(): Map<String, String> {
                    return mapOf(
                        "receiver_id" to receiverId,
                        "sender_id" to currentUserId.toString()
                    )
                }
            }

            Volley.newRequestQueue(context).add(stringRequest)

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