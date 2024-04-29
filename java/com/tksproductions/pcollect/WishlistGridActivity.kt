// WishlistGridActivity.kt
package com.tksproductions.pcollect

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tksproductions.pcollect.databinding.ActivityWishlistGridBinding
import kotlin.math.ceil
import kotlin.math.max

class WishlistGridActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWishlistGridBinding
    private lateinit var wishlistAdapter: WishlistAdapter
    private val aspectRatio = 1.0f / 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val wishlistedPhotocards = intent.getParcelableArrayListExtra<Photocard>("wishlistedPhotocards")
        if (wishlistedPhotocards != null) {
            setupRecyclerView(wishlistedPhotocards)
        }
    }

    private fun setupRecyclerView(photocards: List<Photocard>) {
        wishlistAdapter = WishlistAdapter(photocards)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@WishlistGridActivity, calculateNumColumns(photocards.size))
            adapter = wishlistAdapter
            addItemDecoration(GridSpacingItemDecoration(calculateSpacing(photocards.size).toInt()))
        }
    }

    private fun calculateNumColumns(photocardsCount: Int): Int {
        val screenWidth = resources.displayMetrics.widthPixels
        val frameWidth = (screenWidth * 0.95f).toInt()
        val (numColumns, _, _) = calculateGrid(frameWidth.toFloat(), frameWidth.toFloat() * aspectRatio, photocardsCount)
        return numColumns
    }

    private fun calculateGrid(screenWidth: Float, frameHeight: Float, numImages: Int): Triple<Int, Float, Float> {
        if (numImages <= 0) {
            return Triple(0, 0f, 0f)
        }
        else if (numImages <= 1){
            return Triple(2, screenWidth, screenWidth * 1.5f)
        }

        var bestLayout = Triple(1, screenWidth, screenWidth * 1.5f)
        var maxArea = 0f

        for (columns in 1..numImages) {
            val imageWidth = screenWidth / columns
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

        return bestLayout
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

class GridSpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = spacing
        outRect.right = spacing
        outRect.bottom = spacing
        outRect.top = spacing
    }
}