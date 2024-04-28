package com.tksproductions.pcollect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WishlistAdapter(private val photocards: List<Photocard>) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wishlist_photocard, parent, false)
        return WishlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val photocard = photocards[position]
        Glide.with(holder.itemView)
            .load(photocard.imageUri)
            .into(holder.photocardImageView)
    }

    override fun getItemCount(): Int = photocards.size
}