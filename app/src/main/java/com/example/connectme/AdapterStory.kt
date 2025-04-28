package com.example.connectme

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class AdapterStory(
    private val storyList: List<ModelStory>,
    private val onStoryClick: (ModelStory) -> Unit,
    private val onStoryLongClick: (ModelStory) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val USER_STORY = 1
        const val OTHER_STORY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return storyList[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == USER_STORY) {
            UserStoryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.user_story, parent, false)
            )
        } else {
            OtherStoryViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.story, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val story = storyList[position]
        try {
            val bitmap = decodeBase64ToBitmap(story.profileImage)
            when (holder) {
                is UserStoryViewHolder -> {
                    holder.imageView.setImageBitmap(bitmap)

                    holder.itemView.setOnClickListener {
                        onStoryClick(story)
                    }

                    holder.itemView.setOnLongClickListener {
                        onStoryLongClick(story)
                        true
                    }
                }
                is OtherStoryViewHolder -> {
                    holder.imageView.setImageBitmap(bitmap)

                    holder.itemView.setOnClickListener {
                        onStoryClick(story)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AdapterStory", "Error loading image", e)
            when (holder) {
                is UserStoryViewHolder -> {
                    holder.imageView.setImageResource(R.drawable.connectme_logo)
                    holder.addIcon.visibility = View.VISIBLE
                }
                is OtherStoryViewHolder -> {
                    holder.imageView.setImageResource(R.drawable.connectme_logo)
                }
            }
        }
    }

    override fun getItemCount(): Int = storyList.size

    private fun decodeBase64ToBitmap(encodedString: String): Bitmap {
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    class UserStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.user_ownstory_profile_image)
        val addIcon: ImageView = itemView.findViewById(R.id.add_story_icon)
    }

    class OtherStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.story_profile)
    }
}