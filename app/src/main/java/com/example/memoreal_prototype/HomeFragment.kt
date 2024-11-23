package com.example.memoreal_prototype

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.memoreal_prototype.R

class HomeFragment : Fragment() {

    private lateinit var slideshowImageView: ImageView
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private val imageResources = arrayOf(R.drawable.slide_image_1, R.drawable.slide_image_2, R.drawable.slide_image_3)
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private val slideshowRunnable = object : Runnable {
        override fun run() {
            navigateToNextImage()
            handler.postDelayed(this, 5000) // 5-second interval
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val fragmentManager = activity?.supportFragmentManager
        val activity = activity
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "userSession",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val btnCreateObituary = view.findViewById<Button>(R.id.btnCreateObituary)
        val username = view.findViewById<TextView>(R.id.tvUsername)

        btnCreateObituary.setOnClickListener {
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, CreateObituaryStep1()) // Replace with MyObituaries fragment
                ?.addToBackStack(null) // Add to back stack for navigation back
                ?.commit()

            btnCreateObituary.isEnabled = false
        }

        val storedUsername = sharedPreferences.getString("username", "")
        if (sharedPreferences.getBoolean("isGuestUser", false)) {
            username.text = "Guest User"
        } else {
            username.text = storedUsername
        }

        // Slideshow functionality
        slideshowImageView = view.findViewById(R.id.slideshowImageView)
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)

        previousButton.setOnClickListener {
            navigateToPreviousImage()
        }

        nextButton.setOnClickListener {
            navigateToNextImage()
        }

        updateButtonVisibility()
        handler.postDelayed(slideshowRunnable, 5000) // Start automatic slideshow

        return view
    }

    private fun navigateToNextImage() {
        if (currentIndex < imageResources.size - 1) {
            currentIndex++
        } else {
            currentIndex = 0
        }
        updateSlideshowImage()
        updateButtonVisibility()
    }

    private fun navigateToPreviousImage() {
        if (currentIndex > 0) {
            currentIndex--
        } else {
            currentIndex = imageResources.size - 1
        }
        updateSlideshowImage()
        updateButtonVisibility()
    }

    private fun updateSlideshowImage() {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = 500
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 500

        slideshowImageView.startAnimation(fadeOut)
        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation) {
                slideshowImageView.setImageResource(imageResources[currentIndex])
                slideshowImageView.startAnimation(fadeIn)
            }
        })
    }

    private fun updateButtonVisibility() {
        previousButton.visibility = if (currentIndex == 0) View.GONE else View.VISIBLE
        nextButton.visibility = if (currentIndex == imageResources.size - 1) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(slideshowRunnable) // Stop the slideshow when the view is destroyed
    }
}
