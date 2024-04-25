package com.tksproductions.pcollect

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tksproductions.pcollect.databinding.ActivityPhotocardCatalogBinding

class PhotocardCatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotocardCatalogBinding
    private lateinit var photocardCatalogAdapter: PhotocardCatalogAdapter
    private val photocardList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotocardCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        val idolName = intent.getStringExtra("idolName") ?: return
        loadPhotocards(idolName)

        binding.addSelectedButton.setOnClickListener {
            val selectedPhotocards = photocardCatalogAdapter.getSelectedPhotocards()
            val intent = Intent()
            intent.putStringArrayListExtra("selectedPhotocards", ArrayList(selectedPhotocards))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        photocardCatalogAdapter = PhotocardCatalogAdapter(photocardList) { selectedPhotocard, isSelected ->
            if (isSelected) {
                photocardCatalogAdapter.selectPhotocard(selectedPhotocard)
            } else {
                photocardCatalogAdapter.deselectPhotocard(selectedPhotocard)
            }
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@PhotocardCatalogActivity, 2)
            adapter = photocardCatalogAdapter
        }
    }

    private fun loadPhotocards(idolName: String) {
        val assetManager = assets
        val photocardFiles = assetManager.list("Photocards/$idolName") ?: return
        photocardList.addAll(photocardFiles)
        photocardCatalogAdapter.notifyDataSetChanged()
    }

    inner class PhotocardCatalogAdapter(
        private val photocardList: List<String>,
        private val onItemClick: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<PhotocardCatalogAdapter.ViewHolder>() {

        private val selectedPhotocards = mutableSetOf<String>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)
            val photocardNameTextView: TextView = itemView.findViewById(R.id.photocardNameTextView)
            val selectButton: Button = itemView.findViewById(R.id.selectButton)

            init {
                selectButton.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val photocardName = photocardList[position]
                        val isSelected = selectedPhotocards.contains(photocardName)
                        onItemClick(photocardName, !isSelected)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_photocard_catalog, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val photocardName = photocardList[position]
            val idolName = intent.getStringExtra("idolName") ?: return
            val photocardUri = "file:///android_asset/Photocards/$idolName/$photocardName"
            Glide.with(holder.itemView)
                .load(photocardUri)
                .into(holder.photocardImageView)
            holder.photocardNameTextView.text = photocardName

            val isSelected = selectedPhotocards.contains(photocardName)
            holder.selectButton.text = if (isSelected) "Deselect" else "Select"
        }

        override fun getItemCount(): Int {
            return photocardList.size
        }

        fun selectPhotocard(photocard: String) {
            selectedPhotocards.add(photocard)
            notifyDataSetChanged()
        }

        fun deselectPhotocard(photocard: String) {
            selectedPhotocards.remove(photocard)
            notifyDataSetChanged()
        }

        fun getSelectedPhotocards(): Set<String> {
            return selectedPhotocards
        }
    }
}