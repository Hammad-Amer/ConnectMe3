package com.example.connectme

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.BaseAdapter

class AdapterUserProfileImages(private val context: Context, private val images: List<Bitmap>) : BaseAdapter() {

    override fun getCount(): Int = images.size

    override fun getItem(position: Int): Any = images[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_grid_image, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.grid_image)

        imageView.setImageBitmap(images[position])  // Set the Bitmap

        return view
    }
}
