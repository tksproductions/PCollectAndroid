package com.tksproductions.pcollect

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.tksproductions.pcollect.databinding.ActivityWishlistGridBinding
import kotlin.math.ceil
import kotlin.math.max

class WishlistGridActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWishlistGridBinding
    private lateinit var wishlistAdapter: WishlistAdapter
    private val aspectRatio = 1.0f / 1.0f
    private val gson =
        GsonBuilder()
            .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
            .create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idolName = intent.getStringExtra("idolName")
        if (idolName != null) {
            loadWishlistedPhotocards(idolName)
        }
    }

    private fun loadWishlistedPhotocards(idolName: String) {
        val sharedPreferences = getSharedPreferences("PhotocardPrefs", Context.MODE_PRIVATE)
        val photocardListJson = sharedPreferences.getString("${idolName}_photocardList", null)
        if (photocardListJson != null) {
            val type = object : TypeToken<List<Photocard>>() {}.type
            val photocardList = gson.fromJson<List<Photocard>>(photocardListJson, type)
            val wishlistedPhotocards = photocardList.filter { photocard -> photocard.isWishlisted }
            setupRecyclerView(wishlistedPhotocards)
        }
    }

    private fun setupRecyclerView(photocards: List<Photocard>) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val frameWidth = screenWidth * 0.95f
        val frameHeight = frameWidth * aspectRatio
        val (numColumns, imageWidth, imageHeight) = calculateGrid(frameWidth, frameHeight, photocards.size)

        wishlistAdapter = WishlistAdapter(photocards, imageWidth, imageHeight)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@WishlistGridActivity, numColumns)
            adapter = wishlistAdapter
            addItemDecoration(GridSpacingItemDecoration(calculateSpacing(photocards.size).toInt()))
        }
    }

    private fun calculateGrid(
        screenWidth: Float,
        frameHeight: Float,
        numImages: Int,
    ): Triple<Int, Float, Float> {
        var bestLayout = Triple(1, screenWidth, screenWidth * 1.5f)
        var maxArea = 0f

        for (columns in 1..numImages) {
            val imageWidth = screenWidth / columns * 0.95f
            val imageHeight = imageWidth * 1.5f
            val rows = ceil(numImages.toFloat() / columns).toInt()
            val totalHeight = imageHeight * rows

            if (totalHeight > frameHeight) {
                val adjustedImageHeight = frameHeight / rows * 0.95f
                val adjustedImageWidth = adjustedImageHeight * (2f / 3f)

                if (adjustedImageWidth * columns <= screenWidth) {
                    val area = adjustedImageWidth * adjustedImageHeight * numImages
                    if (area > maxArea) {
                        maxArea = area
                        bestLayout = Triple(columns, adjustedImageWidth, adjustedImageHeight)
                    }
                }
            } else {
                val area = imageWidth * imageHeight * numImages
                if (area > maxArea) {
                    maxArea = area
                    bestLayout = Triple(columns, imageWidth, imageHeight)
                }
            }
        }

        return Triple(bestLayout.first, bestLayout.second, bestLayout.third)
    }

    private fun calculateSpacing(photocardsCount: Int): Float {
        val maxCardsForMaxSpacing = 4
        val minSpacingForMaxCards = 10f
        val maxCardsForMinSpacing = 50
        val minSpacing = 0f

        return when {
            photocardsCount <= maxCardsForMaxSpacing -> minSpacingForMaxCards
            photocardsCount >= maxCardsForMinSpacing -> minSpacing
            else -> {
                val slope = (minSpacing - minSpacingForMaxCards) / (maxCardsForMinSpacing - maxCardsForMaxSpacing)
                max(minSpacingForMaxCards + slope * (photocardsCount - maxCardsForMaxSpacing), minSpacing)
            }
        }
    }
}

class GridSpacingItemDecoration(
    private val spacing: Int,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
        outRect.top = spacing
    }
}
