package com.example.memoreal_prototype

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.example.memoreal_prototype.UserSession.Companion.baseUrl
import com.example.memoreal_prototype.UserSession.Companion.client
import com.example.memoreal_prototype.models.Obituary
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var slideshowImageView: ImageView
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var profilePicture: ImageButton
    private lateinit var obituaryAdapter: ObituaryAdapterHome
    private lateinit var recyclerView: RecyclerView

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
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerViewHome)
        slideshowImageView = view.findViewById(R.id.slideshowImageView)
        previousButton = view.findViewById(R.id.previousButton)
        nextButton = view.findViewById(R.id.nextButton)
        profilePicture = view.findViewById(R.id.imgProfilePic)

        // Retrieve and display the user's profile picture and other details
        setupUserProfile(view)

        // Setup slideshow controls
        previousButton.setOnClickListener { navigateToPreviousImage() }
        nextButton.setOnClickListener { navigateToNextImage() }
        updateButtonVisibility()
        handler.postDelayed(slideshowRunnable, 5000) // Start automatic slideshow

        // Fetch user data and obituary list
        getUserId()
        fetchUser(userId)
        fetchObituaries()

        return view
    }

    private fun setupRecyclerView(obituaries: List<Obituary>) {
        obituaryAdapter = ObituaryAdapterHome(
            originalObituaries = obituaries,
            onItemClick = { obituary ->
                val obituaryFragment = ObituaryFragment().apply {
                    arguments = Bundle().apply {
                        putInt("obituaryId", obituary.OBITUARYID)
                    }
                }
                (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, obituaryFragment)
                    .addToBackStack("HomeFragment")
                    .commit()
            }
        )

        // Set layout manager to horizontal scrolling
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = obituaryAdapter
    }



    private fun fetchObituaries() {
        val url = "$baseUrl"+"api/allObit"
        Log.d("API", "Requesting URL: $url")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Obituaries", "Failed to fetch obituaries: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to fetch obituaries", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val obituaries = parseObituaries(responseBody)
                        requireActivity().runOnUiThread {
                            setupRecyclerView(obituaries)
                        }
                    } ?: run {
                        Log.e("Fetch Obituaries", "Response body is null.")
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("Fetch Obituaries", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseObituaries(json: String): List<Obituary> {
        val jsonArray = JSONArray(json)
        val obituaries = mutableListOf<Obituary>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            obituaries.add(
                Obituary(
                    jsonObject.getInt("OBITUARYID"),
                    jsonObject.getInt("USERID"),
                    jsonObject.getInt("GALLERYID"),
                    jsonObject.getInt("FAMILYID"),
                    jsonObject.getInt("OBITCUSTID"),
                    jsonObject.optString("BIOGRAPHY"),
                    jsonObject.getString("OBITUARYNAME"),
                    jsonObject.getString("OBITUARY_PHOTO"),
                    jsonObject.getString("DATEOFBIRTH"),
                    jsonObject.getString("DATEOFDEATH"),
                    jsonObject.getString("KEYEVENTS"),
                    jsonObject.getString("OBITUARYTEXT"),
                    jsonObject.optString("FUN_DATETIME"),
                    jsonObject.optString("FUN_LOCATION"),
                    jsonObject.optString("ADTLINFO"),
                    jsonObject.optString("FAVORITEQUOTE"),
                    jsonObject.getBoolean("ENAGUESTBOOK"),
                    jsonObject.getString("PRIVACY"),
                    jsonObject.getString("CREATIONDATE"),
                    jsonObject.getString("LASTMODIFIED")
                )
            )
        }
        return obituaries
    }

    private fun setupUserProfile(view: View) {
        val btnCreateObituary = view.findViewById<Button>(R.id.btnCreateObituary)
        val username = view.findViewById<TextView>(R.id.tvUsername)

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

        val storedUsername = sharedPreferences.getString("username", "")
        if (sharedPreferences.getBoolean("isGuestUser", false)) {
            username.text = "Guest User"
        } else {
            username.text = storedUsername
        }

        btnCreateObituary.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, CreateObituaryStep1())
                ?.addToBackStack(null)
                ?.commit()
            btnCreateObituary.isEnabled = false
        }

        profilePicture.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, ProfileFragment())
                ?.addToBackStack("Home Fragment")
                ?.commit()

            val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
            bottomNavigationView?.selectedItemId = R.id.nav_profile
        }
    }

    private fun fetchUser(userId: Int) {
        val url = "$baseUrl"+"api/fetchUser/$userId"
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
                            val jsonObject = JSONObject(responseBody)
                            val picture = jsonObject.optString("PICTURE", "")

                            requireActivity().runOnUiThread {
                                if (picture.isNotEmpty()) {
                                    val cleanPath = picture.replace("file://", "")
                                    val imgFile = File(cleanPath)

                                    Glide.with(requireContext())
                                        .load(imgFile)
                                        .placeholder(R.drawable.baseline_person_24)
                                        .error(R.drawable.baseline_person_24)
                                        .circleCrop()
                                        .into(profilePicture)
                                } else {
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

    private fun getUserId() {
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
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
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
