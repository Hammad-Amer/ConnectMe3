package com.example.connectme

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class AdapterUserProfileImages(private val context: Context, private val imageIds: IntArray) : BaseAdapter() {

    override fun getCount(): Int = imageIds.size

    override fun getItem(position: Int): Any = imageIds[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView: ImageView = convertView as? ImageView ?: ImageView(context).apply {

            layoutParams = ViewGroup.LayoutParams(430, 430)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            setPadding(1, 1, 1, 1)
        }

        imageView.setImageResource(imageIds[position])

        imageView.post{
            imageView.scaleType = ImageView.ScaleType.FIT_XY
        }
        return imageView
    }
}
