package com.tksproductions.pcollect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
            openPhotocardCatalog()
        }
    }

    private fun loadPhotocards(idolName: String) {
        val directory = getExternalFilesDir("Photocards/$idolName") ?: return
        directory.listFiles()?.filter { it.isFile && it.name.endsWith(".jpg") }?.forEach { file ->
            photocardList.add(Photocard(Uri.fromFile(file), false, false, file.name))
        }
        photocardAdapter.notifyDataSetChanged()
    }

    private fun openPhotocardCatalog() {
        val idolName = intent.getStringExtra("idolName") ?: return
        val intent = Intent(this, PhotocardCatalogActivity::class.java)
        intent.putExtra("idolName", idolName)
        startActivityForResult(intent, REQUEST_PHOTOCARD_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PHOTOCARD_PICK && resultCode == RESULT_OK && data != null) {
            val photocardName = data.getStringExtra("photocardName") ?: return
            val idolName = intent.getStringExtra("idolName") ?: return
            val photocardUri = Uri.parse("file:///android_asset/Photocards/$idolName/$photocardName")
            photocardList.add(Photocard(photocardUri, false, false, photocardName))
            photocardAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val REQUEST_PHOTOCARD_PICK = 1
    }
}