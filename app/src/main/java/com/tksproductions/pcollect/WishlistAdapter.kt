package com.tksproductions.pcollect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WishlistAdapter(
    private val photocards: List<Photocard>,
    private val photocardWidth: Float,
    private val photocardHeight: Float
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)
        val photocardCardView: CardView = itemView.findViewById(R.id.photocardCardView)
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

        holder.photocardCardView.layoutParams.width = photocardWidth.toInt()
        holder.photocardCardView.layoutParams.height = photocardHeight.toInt()
        holder.photocardCardView.requestLayout()

        holder.photocardImageView.post {
            val imageWidth = holder.photocardImageView.width
            val imageHeight = holder.photocardImageView.height
            val cornerRadius = calculateCornerRadius(imageWidth, imageHeight)
            holder.photocardCardView.radius = cornerRadius
        }
    }

    private fun calculateCornerRadius(imageWidth: Int, imageHeight: Int): Float {
        val minDimension = minOf(imageWidth, imageHeight)
        val cornerRadiusRatio = 0.12f
        return minDimension * cornerRadiusRatio
    }

    override fun getItemCount(): Int = photocards.size
}