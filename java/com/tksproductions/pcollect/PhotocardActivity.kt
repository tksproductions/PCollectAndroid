package com.tksproductions.pcollect

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.tksproductions.pcollect.databinding.ActivityPhotocardBinding
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileOutputStream

class PhotocardActivity : AppCompatActivity(), PhotocardAdapter.OnPhotocardClickListener {

    private lateinit var binding: ActivityPhotocardBinding
    private lateinit var photocardAdapter: PhotocardAdapter
    private val photocardList = mutableListOf<Photocard>()
    private val selectedPhotocards = mutableListOf<Int>()
    private lateinit var idolName: String
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
        .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhotocardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (OpenCVLoader.initDebug()) {
            Log.i("OpenCV", "OpenCV successfully loaded.")
        } else {
            Log.e("OpenCV", "Failed to load OpenCV.")
        }

        setupRecyclerView()
        setupAddPhotocardButton()
        setupCategorizeButton()

        idolName = intent.getStringExtra("idolName") ?: return
        binding.addPhotocardButton.text = getString(R.string.add_idol_photocards, idolName)
        loadPhotocards()
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

    private fun loadPhotocards() {
        val sharedPreferences = getSharedPreferences("PhotocardPrefs", Context.MODE_PRIVATE)
        val photocardListJson = sharedPreferences.getString("${idolName}_photocardList", null)
        if (photocardListJson != null) {
            val type = object : TypeToken<List<Photocard>>() {}.type
            val savedPhotocardList = gson.fromJson<List<Photocard>>(photocardListJson, type)
            photocardList.clear()
            photocardList.addAll(savedPhotocardList)
            sortPhotocards()
            photocardAdapter.notifyDataSetChanged()
        }
        binding.textNoPhotocards.visibility = if (photocardList.isEmpty()) View.VISIBLE else View.GONE
    }

    fun savePhotocards() {
        val sharedPreferences = getSharedPreferences("PhotocardPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val photocardListJson = gson.toJson(photocardList)
        editor.putString("${idolName}_photocardList", photocardListJson)
        editor.apply()
    }

    private fun showAddPhotocardOptions() {
        val options = arrayOf("Add from Catalog", "Import from Gallery", "Extract from Template")
        val builder = AlertDialog.Builder(this, R.style.DarkDialogTheme)
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openPhotocardCatalog()
                1 -> importPhotocardFromGallery()
                2 -> extractPhotocardsFromTemplate()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun importPhotocardFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, REQUEST_PHOTOCARD_IMPORT)
    }

    private fun openPhotocardCatalog() {
        val intent = Intent(this, PhotocardCatalogActivity::class.java)
        intent.putExtra("idolName", idolName)
        startActivityForResult(intent, REQUEST_PHOTOCARD_PICK)
    }

    private fun extractPhotocardsFromTemplate() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, REQUEST_TEMPLATE_IMPORT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_PHOTOCARD_IMPORT -> {
                    if (data.clipData != null) {
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val selectedImageUri = clipData.getItemAt(i).uri
                            if (selectedImageUri != null) {
                                photocardList.add(Photocard(selectedImageUri, false, false, "Imported Photocard"))
                            }
                        }
                    } else if (data.data != null) {
                        val selectedImageUri = data.data
                        if (selectedImageUri != null) {
                            photocardList.add(Photocard(selectedImageUri, false, false, "Imported Photocard"))
                        }
                    }
                    sortPhotocards()
                    savePhotocards()
                    photocardAdapter.notifyDataSetChanged()
                    binding.textNoPhotocards.visibility = View.GONE
                }
                REQUEST_PHOTOCARD_PICK -> {
                    val selectedPhotocards = data.getStringArrayListExtra("selectedPhotocards") ?: return
                    selectedPhotocards.forEach { photocardName ->
                        val photocardUri = Uri.parse("file:///android_asset/Photocards/$photocardName")
                        println(photocardUri)
                        photocardList.add(Photocard(photocardUri, false, false, photocardName))
                    }
                    sortPhotocards()
                    savePhotocards()
                    photocardAdapter.notifyDataSetChanged()
                    binding.textNoPhotocards.visibility = View.GONE
                }
                REQUEST_TEMPLATE_IMPORT -> {
                    val selectedImageUri = data.data
                    if (selectedImageUri != null) {
                        val contentResolver = applicationContext.contentResolver
                        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        contentResolver.takePersistableUriPermission(selectedImageUri, takeFlags)
                        extractPhotocardsFromUri(selectedImageUri)
                        binding.textNoPhotocards.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun extractPhotocardsFromUri(uri: Uri) {
        val inputBitmap = uriToBitmap(uri)
        if (inputBitmap != null) {
            val extractedBitmaps = OpenCVUtils.extractPhotos(inputBitmap)
            extractedBitmaps.forEach { (bitmap, rect) ->
                val photocardUri = saveBitmapToFile(bitmap)
                photocardList.add(Photocard(photocardUri, false, false, "Extracted Photocard"))
            }
            sortPhotocards()
            savePhotocards()
            photocardAdapter.notifyDataSetChanged()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val contentResolver = applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val file = File(applicationContext.filesDir, "extracted_photocard_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
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
        val options = arrayOf("Collected", "Wishlisted", "Default", "Delete")
        val builder = AlertDialog.Builder(this, R.style.DarkDialogTheme)
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> categorizePhotocards(true, false)
                1 -> categorizePhotocards(false, true)
                2 -> categorizePhotocards(false, false)
                3 -> deleteSelectedPhotocards()
            }
            selectedPhotocards.clear()
            savePhotocards()
            updateUI()
        }
        builder.show()
    }

    private fun categorizePhotocards(isCollected: Boolean, isWishlisted: Boolean) {
        selectedPhotocards.forEach { position ->
            photocardList[position].isCollected = isCollected
            photocardList[position].isWishlisted = isWishlisted
        }
        sortPhotocards()
        photocardAdapter.notifyDataSetChanged()
    }

    private fun deleteSelectedPhotocards() {
        val deletePositions = selectedPhotocards.sorted().reversed()
        deletePositions.forEach { position ->
            photocardList.removeAt(position)
        }
        photocardAdapter.notifyDataSetChanged()
    }

    private fun sortPhotocards() {
        photocardList.sortWith(compareBy<Photocard> { it.isCollected }
            .thenByDescending { it.isWishlisted }
            .thenBy { !it.isWishlisted && !it.isCollected })
    }

    companion object {
        private const val REQUEST_PHOTOCARD_IMPORT = 1
        private const val REQUEST_PHOTOCARD_PICK = 2
        private const val REQUEST_TEMPLATE_IMPORT = 3
    }
}