package com.tksproductions.pcollect

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class IdolAdapter(
    private val idolList: MutableList<Idol>,
    private val onIdolSwapped: (Int, Int) -> Unit
) : RecyclerView.Adapter<IdolAdapter.IdolViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdolViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_idol, parent, false)
        return IdolViewHolder(view)
    }

    override fun onBindViewHolder(holder: IdolViewHolder, position: Int) {
        holder.bind(idolList[position])
    }

    override fun getItemCount(): Int = idolList.size

    inner class IdolViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnTouchListener {
        private val idolImageView: ImageView = itemView.findViewById(R.id.idolImageView)
        private val idolNameTextView: TextView = itemView.findViewById(R.id.idolNameTextView)
        private var longPressHandler = Handler(Looper.getMainLooper())
        private var longPressRunnable: Runnable? = null
        private var deleteConfirmationDialog: AlertDialog? = null
        private var isDragging = false

        init {
            itemView.setOnClickListener {
                val intent = Intent(it.context, PhotocardActivity::class.java)
                intent.putExtra("idolName", idolList[adapterPosition].name)
                it.context.startActivity(intent)
            }
            itemView.setOnTouchListener(this)
        }

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    longPressRunnable = Runnable {
                        if (!isDragging) {
                            showDeleteConfirmationDialog(adapterPosition)
                        }
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, 500)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isDragging) {
                        isDragging = true
                        longPressRunnable?.let {
                            longPressHandler.removeCallbacks(it)
                        }
                        dismissDeleteConfirmationDialog()
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    longPressRunnable?.let {
                        longPressHandler.removeCallbacks(it)
                    }
                }
            }
            return false
        }

        private fun showDeleteConfirmationDialog(position: Int) {
            val context = itemView.context
            deleteConfirmationDialog = AlertDialog.Builder(context, R.style.DarkDialogTheme)
                .setMessage("Are you sure you want to delete this idol?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteIdol(position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        private fun dismissDeleteConfirmationDialog() {
            deleteConfirmationDialog?.dismiss()
            deleteConfirmationDialog = null
        }

        private fun deleteIdol(position: Int) {
            val idolName = idolList[position].name
            deletePhotocards(idolName)
            idolList.removeAt(position)
            notifyItemRemoved(position)
            (itemView.context as? MainActivity)?.saveIdols()
        }

        private fun deletePhotocards(idolName: String) {
            val sharedPreferences = itemView.context.getSharedPreferences(
                "PhotocardPrefs",
                Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.remove("${idolName}_photocardList")
            editor.apply()
        }

        fun bind(idol: Idol) {
            idolNameTextView.text = idol.name
            idolImageView.setImageURI(idol.imageUri)
        }
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val movedIdol = idolList[fromPosition]
        idolList.removeAt(fromPosition)
        idolList.add(toPosition, movedIdol)
        notifyItemMoved(fromPosition, toPosition)
        onIdolSwapped(fromPosition, toPosition)
    }
}
