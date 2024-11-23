package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import android.net.Uri
import android.widget.ImageButton
import android.widget.VideoView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class CreateObituaryStep1 : Fragment() {

    private var selectedButton: Button? = null
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    private lateinit var slideshowImageView: ImageView
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton

    private val imageResIds = listOf(
        R.drawable.slide_image_1,
        R.drawable.slide_image_2,
        R.drawable.slide_image_3
    )
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_obituary_step1, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val proceed = view.findViewById<Button>(R.id.btnProceed)
        val button1 = view.findViewById<Button>(R.id.btnPck1)
        val button2 = view.findViewById<Button>(R.id.btnPck2)
        val button3 = view.findViewById<Button>(R.id.btnPck3)
        val button4 = view.findViewById<Button>(R.id.btnPck4)
        val videoView = view.findViewById<VideoView>(R.id.aiSample)

        slideshowImageView = view.findViewById(R.id.slideshowImageView)
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)

        val buttons = listOf(button1, button2, button3, button4)

        sharedViewModel.selectedButtonId.observe(viewLifecycleOwner) { selectedId ->
            // Reset all buttons to their default style
            buttons.forEach { button ->
                button.setBackgroundColor(resources.getColor(R.color.memo_orange)) // Default color
                button.setTextColor(resources.getColor(R.color.black))
            }

            // Highlight the previously selected button
            buttons.find { it.id == selectedId }?.apply {
                setBackgroundColor(resources.getColor(R.color.memo_light_orange)) // Highlight color
                setTextColor(resources.getColor(R.color.white))
                selectedButton = this // Update selectedButton reference
            }
        }

        // Set click listeners for all buttons
        buttons.forEach { button ->
            button.setOnClickListener {
                onPackageSelected(button)
            }
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        proceed.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep2())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep1")
                .commit()
        }

        (activity as HomePageActivity).showBottomNavigation()

        // Set up video view to play the video
        val videoUri = Uri.parse("android.resource://${requireActivity().packageName}/raw/ai_sample")
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f) // Mute the video
            videoView.start()
        }

        previousButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                slideshowImageView.setImageResource(imageResIds[currentIndex])
                updateButtonVisibility()
            }
        }

        nextButton.setOnClickListener {
            if (currentIndex < imageResIds.size - 1) {
                currentIndex++
                slideshowImageView.setImageResource(imageResIds[currentIndex])
                updateButtonVisibility()
            }
        }

        // Set initial button visibility
        updateButtonVisibility()

        return view
    }

    private fun onPackageSelected(clickedButton: Button) {
        // Reset previously selected button, if any
        selectedButton?.apply {
            setBackgroundColor(resources.getColor(R.color.memo_orange)) // Default color
            setTextColor(resources.getColor(R.color.black))
        }

        // Set clicked button as selected
        clickedButton.setBackgroundColor(resources.getColor(R.color.memo_light_orange)) // New color for
        // selection
        clickedButton.setTextColor(resources.getColor(R.color.white)) // Change text color to white for visibility

        // Update selected button
        selectedButton = clickedButton

        val selectedPackage: String = when (clickedButton.id) {
            R.id.btnPck1 -> getString(R.string.package1)
            R.id.btnPck2 -> getString(R.string.package2)
            R.id.btnPck3 -> getString(R.string.package3)
            R.id.btnPck4 -> getString(R.string.package4)
            else -> ""
        }
        sharedViewModel.memorialCreationFee.value = true
        sharedViewModel.selectedPackage.value = selectedPackage
        sharedViewModel.selectedButtonId.value = clickedButton.id
    }

    private fun updateButtonVisibility() {
        when (currentIndex) {
            0 -> {
                previousButton.visibility = View.GONE
                nextButton.visibility = View.VISIBLE
            }
            imageResIds.size - 1 -> {
                previousButton.visibility = View.VISIBLE
                nextButton.visibility = View.GONE
            }
            else -> {
                previousButton.visibility = View.VISIBLE
                nextButton.visibility = View.VISIBLE
            }
        }
    }
}