package com.tksproductions.pcollect

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotocardAdapter(
    private val photocardList: MutableList<Photocard>,
    private val onPhotocardClickListener: OnPhotocardClickListener
) : RecyclerView.Adapter<PhotocardAdapter.PhotocardViewHolder>() {

    inner class PhotocardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)
        val photocardBorder: View = itemView.findViewById(R.id.photocardBorder)

        init {
            itemView.setOnClickListener {
                onPhotocardClickListener.onPhotocardClick(adapterPosition)
            }

            itemView.setOnLongClickListener {
                showDeleteConfirmationDialog(adapterPosition, photocardImageView)
                true
            }
        }

        fun bind(photocard: Photocard, isSelected: Boolean) {
            Glide.with(itemView)
                .load(photocard.imageUri)
                .into(photocardImageView)

            when {
                photocard.isCollected -> {
                    photocardImageView.alpha = 0.5f
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
    }

    override fun getItemCount(): Int = photocardList.size

    private fun showDeleteConfirmationDialog(position: Int, photocardImageView: ImageView) {
        val context = photocardImageView.context
        AlertDialog.Builder(context, R.style.DarkDialogTheme)
            .setMessage("Are you sure you want to delete this photocard?")
            .setPositiveButton("Delete") { _, _ ->
                deletePhotocard(position, photocardImageView)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePhotocard(position: Int, photocardImageView: ImageView) {
        photocardList.removeAt(position)
        notifyItemRemoved(position)
        (photocardImageView.context as? PhotocardActivity)?.savePhotocards()
    }

    interface OnPhotocardClickListener {
        fun onPhotocardClick(position: Int)
        fun isPhotocardSelected(position: Int): Boolean
    }
}