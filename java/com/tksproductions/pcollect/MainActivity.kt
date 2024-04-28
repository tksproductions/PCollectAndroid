package com.tksproductions.pcollect

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.tksproductions.pcollect.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var idolAdapter: IdolAdapter
    private val idolList = mutableListOf<Idol>()
    private var selectedImageUri: Uri? = null
    private lateinit var addButton: View
    private val gson = GsonBuilder()
        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
        .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()

        idolAdapter = IdolAdapter(idolList) { fromPosition, toPosition ->
            onIdolSwapped(fromPosition, toPosition)
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = idolAdapter
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                idolAdapter.onItemMove(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

        binding.btnAddIdol.setOnClickListener {
            showAddIdolDialog()
        }

        loadIdols()
    }

    private lateinit var alertDialog: AlertDialog

    private fun showAddIdolDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_idol, null)
        val idolNameEditText = dialogView.findViewById<EditText>(R.id.idolNameEditText)
        val idolImageView = dialogView.findViewById<ImageView>(R.id.idolImageView)
        val selectImageTextView = dialogView.findViewById<TextView>(R.id.selectImageTextView)

        selectImageTextView.visibility = View.VISIBLE
        idolImageView.setImageDrawable(null)

        alertDialog = AlertDialog.Builder(this, R.style.DarkDialogTheme)
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val idolName = idolNameEditText.text.toString()
                if (idolName.isNotEmpty() && selectedImageUri != null) {
                    if (idolList.any { it.name == idolName }) {
                        Toast.makeText(this, "An idol with this name already exists.", Toast.LENGTH_SHORT).show()
                    } else {
                        val newIdol = Idol(idolName, selectedImageUri!!)
                        idolList.add(newIdol)
                        saveIdols()
                        idolAdapter.notifyItemInserted(idolList.size - 1)
                        selectedImageUri = null
                        binding.textWelcome.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, "Please enter a name and select an image for the idol", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                selectedImageUri = null
            }
            .create()

        idolImageView.setOnClickListener {
            openImagePicker()
        }

        alertDialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            val dialogView = alertDialog.findViewById<View>(android.R.id.content)
            val idolImageView = dialogView.findViewById<ImageView>(R.id.idolImageView)
            val selectImageTextView = dialogView.findViewById<TextView>(R.id.selectImageTextView)

            selectedImageUri?.let { uri ->
                val contentResolver = applicationContext.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            }

            idolImageView.setImageURI(selectedImageUri)
            selectImageTextView.visibility = View.GONE
        }
    }

    private fun loadIdols() {
        val sharedPreferences = getSharedPreferences("IdolPrefs", Context.MODE_PRIVATE)
        val idolListJson = sharedPreferences.getString("idolList", null)
        if (idolListJson != null) {
            val type = object : TypeToken<List<Idol>>() {}.type
            val savedIdolList = gson.fromJson<List<Idol>>(idolListJson, type)
            idolList.clear()
            idolList.addAll(savedIdolList)
            idolAdapter.notifyDataSetChanged()
        }
        binding.textWelcome.visibility = if (idolList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun onIdolSwapped(fromPosition: Int, toPosition: Int) {
        saveIdols()
    }

    fun saveIdols() {
        val sharedPreferences = getSharedPreferences("IdolPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val idolListJson = gson.toJson(idolList)
        editor.putString("idolList", idolListJson)
        editor.apply()
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}

data class Idol(var name: String, var imageUri: Uri)
data class Photocard(var imageUri: Uri, var isCollected: Boolean, var isWishlisted: Boolean, var name: String)

class UriTypeAdapter : TypeAdapter<Uri>() {
    override fun write(out: JsonWriter, value: Uri?) {
        out.value(value.toString())
    }

    override fun read(`in`: JsonReader): Uri {
        return Uri.parse(`in`.nextString())
    }
}