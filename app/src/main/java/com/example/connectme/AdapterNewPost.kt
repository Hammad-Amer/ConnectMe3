package com.example.connectme

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

class AdapterNewPost(private val context: Context, private val imageUris: List<Uri>) : BaseAdapter() {

    override fun getCount(): Int = imageUris.size
    override fun getItem(position: Int): Any = imageUris[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_grid_image, parent, false)
        val imageView: ImageView = view.findViewById(R.id.grid_image)

        Glide.with(context).load(imageUris[position]).into(imageView)

        return view
    }
}
