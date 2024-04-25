package com.tksproductions.pcollect

import android.app.Activity
import android.app.AlertDialog
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
import com.tksproductions.pcollect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var idolAdapter: IdolAdapter
    private val idolList = mutableListOf<Idol>()
    private var selectedImageUri: Uri? = null
    private lateinit var addButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idolAdapter = IdolAdapter(idolList)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = idolAdapter
        }

        binding.btnAddIdol.setOnClickListener {
            showAddIdolDialog()
        }
    }

    private fun showAddIdolDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_idol, null)
        val idolNameEditText = dialogView.findViewById<EditText>(R.id.idolNameEditText)
        val idolImageView = dialogView.findViewById<ImageView>(R.id.idolImageView)
        val selectImageTextView = dialogView.findViewById<TextView>(R.id.selectImageTextView)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Add Idol")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel") { _, _ ->
                selectedImageUri = null
            }
            .create()

        alertDialog.setOnShowListener {
            addButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.isEnabled = false

            selectImageTextView.visibility = View.VISIBLE

            idolImageView.setOnClickListener {
                openImagePicker()
            }

            addButton.setOnClickListener {
                val idolName = idolNameEditText.text.toString()
                if (idolName.isNotEmpty() && selectedImageUri != null) {
                    val newIdol = Idol(idolName, selectedImageUri!!)
                    idolList.add(newIdol)
                    idolAdapter.notifyItemInserted(idolList.size - 1)
                    selectedImageUri = null
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, "Please enter a name and select an image for the idol", Toast.LENGTH_SHORT).show()
                }
            }
        }

        alertDialog.show()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data

            val dialogView = layoutInflater.inflate(R.layout.dialog_add_idol, null)
            val idolImageView = dialogView.findViewById<ImageView>(R.id.idolImageView)
            val selectImageTextView = dialogView.findViewById<TextView>(R.id.selectImageTextView)

            idolImageView.setImageURI(selectedImageUri)
            selectImageTextView.visibility = View.GONE

            addButton.isEnabled = true
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}

data class Idol(var name: String, var imageUri: Uri)
data class Photocard(var imageUri: Uri, var isCollected: Boolean, var isWishlisted: Boolean, var name: String)