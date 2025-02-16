package com.example.connectme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterDMs(private val dmsList: List<ModelDMs>) : RecyclerView.Adapter<AdapterDMs.DMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DMViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_dms, parent, false)
        return DMViewHolder(view)
    }

    override fun onBindViewHolder(holder: DMViewHolder, position: Int) {
        val dm = dmsList[position]
        holder.name.text = dm.name
        holder.profileImage.setImageResource(dm.image)


    }

    override fun getItemCount(): Int = dmsList.size

    class DMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.user_profile_image)
        val name: TextView = itemView.findViewById(R.id.textName)
        val cameraIcon: ImageView = itemView.findViewById(R.id.imageCamera)
    }
}
