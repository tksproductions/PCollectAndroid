package com.tksproductions.pcollect

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotocardAdapter(private val photocardList: MutableList<Photocard>, private val onPhotocardClickListener: OnPhotocardClickListener) :
    RecyclerView.Adapter<PhotocardAdapter.PhotocardViewHolder>() {

    class PhotocardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)
        val photocardBorder: View = itemView.findViewById(R.id.photocardBorder)

        fun bind(photocard: Photocard, isSelected: Boolean) {
            Glide.with(itemView)
                .load(photocard.imageUri)
                .into(photocardImageView)

            when {
                photocard.isCollected -> {
                    photocardImageView.alpha = 0.4f
                    photocardBorder.setBackgroundResource(R.drawable.photocard_border_collected)
                }
                photocard.isWishlisted -> {
                    photocardImageView.alpha = 1.0f
                    photocardBorder.setBackgroundResource(R.drawable.photocard_border_wishlisted)
                }
                else -> {
                    photocardImageView.alpha = 1.0f
                    photocardBorder.setBackgroundResource(R.drawable.photocard_border_normal)
                }
            }

            if (isSelected) {
                photocardImageView.setColorFilter(Color.parseColor("#80FF2E98"))
            } else {
                photocardImageView.clearColorFilter()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotocardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photocard, parent, false)
        return PhotocardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotocardViewHolder, position: Int) {
        val isSelected = onPhotocardClickListener.isPhotocardSelected(position)
        holder.bind(photocardList[position], isSelected)
        holder.itemView.setOnClickListener {
            onPhotocardClickListener.onPhotocardClick(position)
        }
    }

    override fun getItemCount(): Int = photocardList.size

    interface OnPhotocardClickListener {
        fun onPhotocardClick(position: Int)
        fun isPhotocardSelected(position: Int): Boolean
    }
}