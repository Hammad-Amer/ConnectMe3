package com.example.connectme

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterDMs(private val dmList: MutableList<ModelDMs>) : RecyclerView.Adapter<AdapterDMs.DMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DMViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_dms, parent, false)
        return DMViewHolder(view)
    }

    override fun onBindViewHolder(holder: DMViewHolder, position: Int) {
        val dm = dmList[position]
        holder.name.text = dm.name

        // If a profile image string is available, decode and set it; otherwise, use the placeholder.
        if (!dm.profileImage.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(dm.profileImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.profileImage.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.profileImage.setImageResource(dm.image)
            }
        } else {
            holder.profileImage.setImageResource(dm.image)
        }
    }

    override fun getItemCount(): Int = dmList.size

    class DMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.user_profile_image)
        val name: TextView = itemView.findViewById(R.id.textName)
        val cameraIcon: ImageView = itemView.findViewById(R.id.imageCamera)
    }
}
