package com.example.memoreal_prototype

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract.Profile
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.example.memoreal_prototype.R
import com.example.memoreal_prototype.UserSession.Companion.baseUrl
import com.example.memoreal_prototype.UserSession.Companion.client
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var slideshowImageView: ImageView
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var profilePicture: ImageButton
    private val imageResources = arrayOf(R.drawable.slide_image_1, R.drawable.slide_image_2, R.drawable.slide_image_3)
    private var currentIndex = 0
    private var userId = 0
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
        profilePicture = view.findViewById(R.id.imgProfilePic)

        getUserId()
        fetchUser(userId)

        btnCreateObituary.setOnClickListener {
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, CreateObituaryStep1()) // Replace with MyObituaries fragment
                ?.addToBackStack(null) // Add to back stack for navigation back
                ?.commit()

            btnCreateObituary.isEnabled = false
        }

        profilePicture.setOnClickListener{
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, ProfileFragment()) // Replace with MyObituaries
                // fragment
                ?.addToBackStack("Home Fragment") // Add to back stack for navigation back
                ?.commit()

            // Get the BottomNavigationView from the activity
            val bottomNavigationView = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)

            // Set the Profile tab as active
            bottomNavigationView?.selectedItemId = R.id.nav_profile
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

    private fun fetchUser(userId: Int) {
        val url = "$baseUrl" + "api/fetchUser/$userId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch User", "Failed to fetch user: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to fetch user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            // Parse the JSON response as an object
                            val jsonObject = JSONObject(responseBody)

                            val picture = jsonObject.optString("PICTURE", "")

                            requireActivity().runOnUiThread {
                                if (picture.isNotEmpty()) {
                                    // Remove "file://" prefix if present
                                    val cleanPath = picture.replace("file://", "")

                                    // Create a File object for the given path
                                    val imgFile = File(cleanPath)

                                    // Load image using Glide
                                    Glide.with(requireContext())
                                        .load(imgFile)
                                        .placeholder(R.drawable.baseline_person_24) // Set placeholder image
                                        .error(R.drawable.baseline_person_24) // Set error image if loading fails
                                        .circleCrop()
                                        .into(profilePicture)
                                } else {
                                    // Set a default image if no picture is provided
                                    profilePicture.setImageResource(R.drawable.baseline_person_24)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Fetch User", "JSON parsing error: ${e.message}")
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error parsing user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch User", "Error: ${response.code} - ${response.message}")
                }
            }
        })
    }

    private fun getUserId(){
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
        userId = sharedPreferences.getInt("userId", -1)
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
