package com.tksproductions.pcollect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tksproductions.pcollect.databinding.ActivityPhotocardCatalogBinding

class PhotocardCatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotocardCatalogBinding
    private lateinit var photocardCatalogAdapter: PhotocardCatalogAdapter
    private val photocardList = mutableListOf<Pair<String, String>>()
    private lateinit var noResultsLayout: ConstraintLayout
    private lateinit var requestIdolTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotocardCatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        noResultsLayout = findViewById(R.id.noResultsLayout)
        requestIdolTextView = findViewById(R.id.requestIdolTextView)

        val initialSearchName = intent.getStringExtra("idolName") ?: ""
        binding.searchView.setQuery(initialSearchName, false)
        performSearch(initialSearchName)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performSearch(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    performSearch(it)
                }
                return true
            }
        })

        binding.addSelectedButton.setOnClickListener {
            val selectedPhotocards = photocardCatalogAdapter.getSelectedPhotocards()
            if (selectedPhotocards.isEmpty()) {
                photocardCatalogAdapter.selectAllPhotocards()
                binding.addSelectedButton.text = "Add Selected"
            } else {
                val intent = Intent()
                intent.putStringArrayListExtra("selectedPhotocards", ArrayList(selectedPhotocards))
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        requestIdolTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pcollect.app"))
            startActivity(intent)
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
            layoutManager = GridLayoutManager(this@PhotocardCatalogActivity, 4)
            adapter = photocardCatalogAdapter
        }
    }

    private fun sanitizeSearchName(searchName: String): String {
        return searchName.replace("\\s".toRegex(), "").replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
    }

    private fun performSearch(searchName: String) {
        val sanitizedSearchName = sanitizeSearchName(searchName)
        loadPhotocards(sanitizedSearchName)
    }

    private fun loadPhotocards(sanitizedSearchName: String) {
        photocardList.clear()

        if (sanitizedSearchName.isEmpty()) {
            showNoResults(false)
            photocardCatalogAdapter.notifyDataSetChanged()
            return
        }

        val assetManager = assets
        val idolFolders = assetManager.list("Photocards") ?: return

        val matchingIdolFolders = idolFolders.filter { idolFolder ->
            val sanitizedIdolName = sanitizeSearchName(idolFolder)
            sanitizedIdolName == sanitizedSearchName || sanitizedIdolName.startsWith(sanitizedSearchName)
        }

        if (matchingIdolFolders.isEmpty()) {
            showNoResults(true)
            photocardCatalogAdapter.notifyDataSetChanged()
            return
        }

        for (idolFolder in matchingIdolFolders) {
            val photocardFiles = assetManager.list("Photocards/$idolFolder") ?: continue
            for (photocardFile in photocardFiles) {
                photocardList.add(Pair(idolFolder, photocardFile))
            }
        }

        showNoResults(false)
        photocardCatalogAdapter.notifyDataSetChanged()
    }

    private fun showNoResults(show: Boolean) {
        if (show) {
            binding.recyclerView.visibility = View.GONE
            noResultsLayout.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            noResultsLayout.visibility = View.GONE
        }
    }

    inner class PhotocardCatalogAdapter(
        private val photocardList: List<Pair<String, String>>,
        private val onItemClick: (String, Boolean) -> Unit
    ) : RecyclerView.Adapter<PhotocardCatalogAdapter.ViewHolder>() {

        private val selectedPhotocards = mutableSetOf<String>()

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val photocardImageView: ImageView = itemView.findViewById(R.id.photocardImageView)

            init {
                photocardImageView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val (idolFolder, photocardName) = photocardList[position]
                        val photocard = "$idolFolder/$photocardName"
                        val isSelected = selectedPhotocards.contains(photocard)
                        onItemClick(photocard, !isSelected)
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
            val (idolFolder, photocardName) = photocardList[position]
            val photocardUri = "file:///android_asset/Photocards/$idolFolder/$photocardName"
            Glide.with(holder.itemView)
                .load(photocardUri)
                .into(holder.photocardImageView)

            val photocard = "$idolFolder/$photocardName"
            val isSelected = selectedPhotocards.contains(photocard)
            holder.photocardImageView.alpha = if (isSelected) 0.5f else 1.0f
            if (selectedPhotocards.isEmpty()) {
                (holder.itemView.context as PhotocardCatalogActivity).binding.addSelectedButton.text = "Select All"
            } else {
                (holder.itemView.context as PhotocardCatalogActivity).binding.addSelectedButton.text = "Add Selected"
            }
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
        fun selectAllPhotocards() {
            selectedPhotocards.clear()
            for ((idolFolder, photocardName) in photocardList) {
                val photocard = "$idolFolder/$photocardName"
                selectedPhotocards.add(photocard)
            }
            notifyDataSetChanged()
        }
    }
}