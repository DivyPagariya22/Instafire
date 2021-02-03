package com.example.android.instafire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.instafire.models.Post

class PostAdapter(val context: Context , val posts : List<Post>) :
    RecyclerView.Adapter<PostAdapter.postHolder>() {


    inner class postHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.username)
        val timeAgo = itemView.findViewById<TextView>(R.id.timeAgo)
        val description = itemView.findViewById<TextView>(R.id.description)
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): postHolder {
        val postholder = postHolder(LayoutInflater.from(context).inflate(R.layout.item,parent,false))
        return postholder
    }

    override fun onBindViewHolder(holder: postHolder, position: Int) {
        val current = posts[position]
        holder.username.text = current.user?.username
        holder.description.text = current.description
        holder.timeAgo.text = DateUtils.getRelativeTimeSpanString(current.creationTime)
        val url = current.imageUrl
        Glide.with(holder.imageView.getContext())
            .load(url)
            .centerCrop()
            .into(holder.imageView);
    }

    override fun getItemCount(): Int {
       return posts.size
    }
}