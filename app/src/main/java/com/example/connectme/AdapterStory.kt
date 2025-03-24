package com.example.connectme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.user_story, parent, false)
            UserStoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.story, parent, false)
            OtherStoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val storyItem = storyList[position]

        if (holder is UserStoryViewHolder) {
            holder.imageView.setImageResource(storyItem.image)

            holder.itemView.setOnClickListener {
                onStoryClick(storyItem)
            }

            holder.itemView.setOnLongClickListener {
                onStoryLongClick(storyItem)
                true
            }
        } else if (holder is OtherStoryViewHolder) {
            holder.imageView.setImageResource(storyItem.image)
        }
    }

    override fun getItemCount(): Int {
        return storyList.size
    }

    class UserStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.user_ownstory_profile_image)
    }

    class OtherStoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.story_profile)
    }
}


