package com.tksproductions.pcollect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.tksproductions.pcollect.databinding.ActivityPhotocardBinding

class PhotocardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhotocardBinding
    private lateinit var photocardAdapter: PhotocardAdapter
    private val photocardList = mutableListOf<Photocard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotocardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupAddPhotocardButton()

        val idolName = intent.getStringExtra("idolName") ?: return
        loadPhotocards(idolName)
    }

    private fun setupRecyclerView() {
        photocardAdapter = PhotocardAdapter(photocardList)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@PhotocardActivity, 2)
            adapter = photocardAdapter
        }
    }

    private fun setupAddPhotocardButton() {
        binding.addPhotocardButton.setOnClickListener {
            showAddPhotocardOptions()
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

    companion object {
        private const val REQUEST_PHOTOCARD_IMPORT = 1
        private const val REQUEST_PHOTOCARD_PICK = 2
    }
}