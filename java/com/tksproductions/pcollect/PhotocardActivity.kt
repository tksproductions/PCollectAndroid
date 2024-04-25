package com.tksproductions.pcollect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.tksproductions.pcollect.databinding.ActivityPhotocardBinding

class PhotocardActivity : AppCompatActivity(), PhotocardAdapter.OnPhotocardClickListener {

    private lateinit var binding: ActivityPhotocardBinding
    private lateinit var photocardAdapter: PhotocardAdapter
    private val photocardList = mutableListOf<Photocard>()
    private val selectedPhotocards = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotocardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupAddPhotocardButton()
        setupCategorizeButton()

        val idolName = intent.getStringExtra("idolName") ?: return
        loadPhotocards(idolName)
    }

    private fun setupRecyclerView() {
        photocardAdapter = PhotocardAdapter(photocardList, this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@PhotocardActivity, 2)
            adapter = photocardAdapter
        }
    }

    override fun isPhotocardSelected(position: Int): Boolean {
        return selectedPhotocards.contains(position)
    }

    private fun setupAddPhotocardButton() {
        binding.addPhotocardButton.setOnClickListener {
            showAddPhotocardOptions()
        }
    }

    private fun setupCategorizeButton() {
        binding.categorizeButton.setOnClickListener {
            showCategorizeOptions()
        }
    }

    private fun loadPhotocards(idolName: String) {
        val directory = getExternalFilesDir("Photocards/$idolName") ?: return
        directory.listFiles()?.filter { it.isFile && it.name.endsWith(".jpg") }?.forEach { file ->
            photocardList.add(Photocard(Uri.fromFile(file), false, false, file.name))
        }
        photocardAdapter.notifyDataSetChanged()
    }

    private fun showAddPhotocardOptions() {
        val options = arrayOf("Import from Gallery", "Add from Catalog")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photocard")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> importPhotocardFromGallery()
                1 -> openPhotocardCatalog()
            }
        }
        builder.show()
    }

    private fun importPhotocardFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PHOTOCARD_IMPORT)
    }

    private fun openPhotocardCatalog() {
        val idolName = intent.getStringExtra("idolName") ?: return
        val intent = Intent(this, PhotocardCatalogActivity::class.java)
        intent.putExtra("idolName", idolName)
        startActivityForResult(intent, REQUEST_PHOTOCARD_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_PHOTOCARD_IMPORT -> {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        photocardList.add(Photocard(selectedImageUri, false, false, "Imported Photocard"))
                        photocardAdapter.notifyDataSetChanged()
                    }
                }
                REQUEST_PHOTOCARD_PICK -> {
                    val selectedPhotocards = data.getStringArrayListExtra("selectedPhotocards") ?: return
                    val idolName = intent.getStringExtra("idolName") ?: return
                    selectedPhotocards.forEach { photocardName ->
                        val photocardUri = Uri.parse("file:///android_asset/Photocards/$idolName/$photocardName")
                        photocardList.add(Photocard(photocardUri, false, false, photocardName))
                    }
                    photocardAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onPhotocardClick(position: Int) {
        if (selectedPhotocards.contains(position)) {
            selectedPhotocards.remove(position)
        } else {
            selectedPhotocards.add(position)
        }
        updateUI()
    }

    private fun updateUI() {
        if (selectedPhotocards.isEmpty()) {
            binding.addPhotocardButton.visibility = View.VISIBLE
            binding.categorizeButton.visibility = View.GONE
        } else {
            binding.addPhotocardButton.visibility = View.GONE
            binding.categorizeButton.visibility = View.VISIBLE
        }
        photocardAdapter.notifyDataSetChanged()
    }

    private fun showCategorizeOptions() {
        val options = arrayOf("Collected", "Wishlisted", "Normal")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Categorize Photocards")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> categorizePhotocards(true, false)
                1 -> categorizePhotocards(false, true)
                2 -> categorizePhotocards(false, false)
            }
            selectedPhotocards.clear()
            updateUI()
        }
        builder.show()
    }

    private fun categorizePhotocards(isCollected: Boolean, isWishlisted: Boolean) {
        selectedPhotocards.forEach { position ->
            photocardList[position].isCollected = isCollected
            photocardList[position].isWishlisted = isWishlisted
        }
        photocardAdapter.notifyDataSetChanged()
    }

    companion object {
        private const val REQUEST_PHOTOCARD_IMPORT = 1
        private const val REQUEST_PHOTOCARD_PICK = 2
    }
}