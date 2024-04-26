package com.tksproductions.pcollect

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.app.AlertDialog
import android.content.Context

class IdolAdapter(private val idolList: MutableList<Idol>) : RecyclerView.Adapter<IdolAdapter.IdolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_idol, parent, false)
        return IdolViewHolder(view)
    }

    override fun onBindViewHolder(holder: IdolViewHolder, position: Int) {
        holder.bind(idolList[position])
    }

    override fun getItemCount(): Int = idolList.size

    inner class IdolViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idolImageView: ImageView = itemView.findViewById(R.id.idolImageView)
        private val idolNameTextView: TextView = itemView.findViewById(R.id.idolNameTextView)

        init {
            itemView.setOnClickListener {
                val intent = Intent(it.context, PhotocardActivity::class.java)
                intent.putExtra("idolName", idolList[adapterPosition].name)
                it.context.startActivity(intent)
            }
            itemView.setOnLongClickListener {
                showDeleteConfirmationDialog(adapterPosition)
                true
            }
        }
        private fun showDeleteConfirmationDialog(position: Int) {
            val context = itemView.context
            AlertDialog.Builder(context, R.style.DarkDialogTheme)
                .setMessage("Are you sure you want to delete this idol?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteIdol(position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun deleteIdol(position: Int) {
            idolList.removeAt(position)
            notifyItemRemoved(position)
            (itemView.context as? MainActivity)?.saveIdols()
        }

        fun bind(idol: Idol) {
            idolNameTextView.text = idol.name
            idolImageView.setImageURI(idol.imageUri)
        }
    }
}
