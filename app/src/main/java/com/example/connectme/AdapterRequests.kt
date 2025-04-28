package com.example.connectme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdapterRequests(
    private var requestList: MutableList<ModelRequest>,
    private val onAccept: (ModelRequest) -> Unit,
    private val onReject: (ModelRequest) -> Unit
) : RecyclerView.Adapter<AdapterRequests.RequestViewHolder>() {

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val username: TextView = itemView.findViewById(R.id.username)
        val acceptButton: Button = itemView.findViewById(R.id.acceptButton)
        val rejectButton: Button = itemView.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]

        holder.username.text = request.username


        val bitmap = decodeBase64ToBitmap(request.profileImageUrl)
        if (bitmap != null) {
            holder.profileImage.setImageBitmap(bitmap)
        } else {
            holder.profileImage.setImageResource(R.drawable.connectme_logo) // Default image
        }

        holder.acceptButton.setOnClickListener { onAccept(request) }
        holder.rejectButton.setOnClickListener { onReject(request) }
    }

    // Helper function to decode Base64 string to Bitmap
    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    override fun getItemCount() = requestList.size

    fun updateList(newList: List<ModelRequest>) {
        requestList.clear()
        requestList.addAll(newList)
        notifyDataSetChanged()
    }
    fun removeItem(request: ModelRequest) {
        val position = requestList.indexOf(request)
        if (position != -1) {
            requestList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}