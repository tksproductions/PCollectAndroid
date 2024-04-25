package com.tksproductions.pcollect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotocardAdapter(private val photocardList: MutableList<Photocard>) :
    RecyclerView.Adapter<PhotocardAdapter.PhotocardViewHolder>() {

    class PhotocardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)

        fun bind(photocard: Photocard) {
            Glide.with(itemView)
                .load(photocard.imageUri)
                .into(photocardImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotocardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photocard, parent, false)
        return PhotocardViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotocardViewHolder, position: Int) {
        holder.bind(photocardList[position])
    }

    override fun getItemCount(): Int = photocardList.size
}