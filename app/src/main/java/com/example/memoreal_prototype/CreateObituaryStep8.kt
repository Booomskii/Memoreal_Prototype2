package com.example.memoreal_prototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.util.TypedValueCompat.dpToPx
import java.io.File
import java.io.IOException

class CreateObituaryStep8 : Fragment() {

    private lateinit var obituaryImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step8, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val frameImageView = view.findViewById<ImageView>(R.id.frame_image)
        val flowerImageView = view.findViewById<ImageView>(R.id.imgFlower)
        val candleImageView = view.findViewById<ImageView>(R.id.imgCandle)

        obituaryImage = view.findViewById(R.id.picture_image)

        val backgroundTheme = arguments?.getString("backgroundTheme")
        val pictureFrame = arguments?.getString("pictureFrame")
        val bgMusic = arguments?.getString("bgMusic")
        val virtualFlower = arguments?.getString("virtualFlower")
        val virtualCandle = arguments?.getString("virtualCandle")

        var frameName = ""
        var flowerName = ""
        var candleName = ""

        pictureFrame?.let {
            val frameParts = it.trim('(', ')').split(", ")
            if (frameParts.size == 2) {
                val frameId = frameParts[0]
                frameName = frameParts[1]
                Log.d("STEP8", "Frame ID: $frameId, Frame Name: $frameName")
            }
        }

        virtualFlower?.let {
            val flowerParts = it.trim('(', ')').split(", ")
            if (flowerParts.size == 2) {
                val flowerId = flowerParts[0]
                flowerName = flowerParts[1]
                Log.d("STEP8", "Flower ID: $flowerId, Flower Name: $flowerName")
            }
        }

        virtualCandle?.let {
            val candleParts = it.trim('(', ')').split(", ")
            if (candleParts.size == 2) {
                val candleId = candleParts[0]
                candleName = candleParts[1]
                Log.d("STEP8", "Candle ID: $candleId, Candle Name: $candleName")
            }
        }

        val backgroundThemeMap = mapOf(
            "Pattern 1" to R.drawable.pattern1,
            "Pattern 2" to R.drawable.pattern2,
            "Pattern 3" to R.drawable.pattern3,
            "Artistic 1" to R.drawable.artistic1,
            "Artistic 2" to R.drawable.artistic2,
            "Artistic 3" to R.drawable.artistic3,
            "Heaven 1" to R.drawable.heaven1,
            "Heaven 2" to R.drawable.heaven2,
            "Heaven 3" to R.drawable.heaven3
        )

        val pictureFrameMap = mapOf(
            "Classic 1" to R.drawable.classic1,
            "Classic 2" to R.drawable.classic2,
            "Classic 3" to R.drawable.classic3,
            "Gold 1" to R.drawable.gold1,
            "Gold 2" to R.drawable.gold2,
            "Gold 3" to R.drawable.gold3,
            "Wood 1" to R.drawable.wood1,
            "Wood 2" to R.drawable.wood2,
            "Wood 3" to R.drawable.wood3
        )

        val flowerMap = mapOf(
            "Rose" to R.drawable.rose,
            "Lily" to R.drawable.lily,
            "Carnation" to R.drawable.carnation,
            "Chrysanthemum" to R.drawable.chrysanthemum,
            "Iris" to R.drawable.iris,
            "Orchid" to R.drawable.orchid
        )

        val candleMap = mapOf(
            "Candle 1" to R.drawable.candle1,
            "Candle 2" to R.drawable.candle2,
            "Candle 3" to R.drawable.candle3,
            "Candle 4" to R.drawable.candle4,
            "Candle 5" to R.drawable.candle5,
            "Candle 6" to R.drawable.candle6,
            "Candle 7" to R.drawable.candle7,
            "Candle 8" to R.drawable.candle8
        )

        pictureFrame.let {
            val drawableResId = pictureFrameMap[it]
            if (drawableResId != null) {
                frameImageView.setImageResource(drawableResId)
            } else {
                frameImageView.setImageResource(R.drawable.classic1)
            }
        }

        flowerName.let {
            val drawableResId = flowerMap[it]
            if (drawableResId != null) {
                flowerImageView.setImageResource(drawableResId)
            } else {
                flowerImageView.setImageResource(R.drawable.default_flower_icon)
            }
        }

        candleName.let {
            val drawableResId = candleMap[it]
            if (drawableResId != null) {
                candleImageView.setImageResource(drawableResId)
            } else {
                candleImageView.setImageResource(R.drawable.default_candle_icon)
            }
        }

        loadImageFromSharedPreferences()

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        nextButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep8")
                .commit()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep7())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        return view
    }

    private fun loadImageFromSharedPreferences() {
        val sharedPreferences = requireContext().getSharedPreferences("obituaryImage", Context
            .MODE_PRIVATE)
        val savedImageFileName = sharedPreferences.getString("savedImageFileName", null) // Retrieve the saved file name

        if (savedImageFileName != null) {
            loadImageFromInternalStorage(savedImageFileName) // Load the image from internal storage
        } else {
            Toast.makeText(requireContext(), "No image file found in SharedPreferences", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageFromInternalStorage(fileName: String) {
        try {
            val file = File(requireContext().filesDir, fileName)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val widthPx = dpToPx(150) // 200dp
                val heightPx = dpToPx(140) // 190dp
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap,
                    widthPx.toInt(), heightPx.toInt(), true)
                obituaryImage.setImageBitmap(resizedBitmap)
            } else {
                Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Float {
        return dp * resources.displayMetrics.density
    }
}