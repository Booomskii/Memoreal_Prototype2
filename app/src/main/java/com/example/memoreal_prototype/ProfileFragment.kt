package com.example.memoreal_prototype

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private lateinit var userName: TextView
    private lateinit var userFullName: TextView
    private lateinit var userContact: TextView
    private lateinit var userEmail: TextView
    private lateinit var userBDate: TextView
    private lateinit var userPhoto: ImageView

    private var bdate = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setupToolbar(view)
        val ageTV = view.findViewById<TextView>(R.id.age)

        userName = view.findViewById(R.id.username)
        userFullName = view.findViewById(R.id.userFullName)
        userContact = view.findViewById(R.id.userContact)
        userEmail = view.findViewById(R.id.userEmail)
        userBDate = view.findViewById(R.id.userBDate)
        userPhoto = view.findViewById(R.id.userPhoto)

        fetchUser()

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)

        backButton.visibility = View.GONE

        val settings = view.findViewById<ImageView>(R.id.settings)
        settings.setOnClickListener {
            // Create an instance of PopupMenu
            val popupMenu = PopupMenu(requireContext(), settings)

            // Inflate the menu resource into the PopupMenu
            popupMenu.menuInflater.inflate(R.menu.profile_settings_menu, popupMenu.menu)

            // Set a listener for menu item clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editProfile -> {
                        editUser()
                        true
                    }
                    R.id.deleteProfile -> {
                        confirmDeleteUser()
                        true
                    }
                    R.id.resetPassword -> {
                        (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, ResetPasswordFragment())
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                            .commit()
                        true
                    }
                    else -> false
                }
            }
            // Show the popup menu
            popupMenu.show()
        }
    }

    private fun fetchUser() {
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
        val userId = sharedPreferences.getInt("userId", -1)
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

                            val firstname = jsonObject.optString("FIRST_NAME", "")
                            val middleInitial = jsonObject.optString("MI", "")
                            val lastname = jsonObject.optString("LAST_NAME", "")

                            // Construct the full name
                            val fullName = "$firstname $middleInitial $lastname".trim()
                            val username = jsonObject.getString("USERNAME")
                            val contact = jsonObject.optString("CONTACT_NUMBER", "N/A")
                            val email = jsonObject.getString("EMAIL")
                            val birthDate = jsonObject.optString("BIRTHDATE", "N/A")
                            val picture = jsonObject.optString("PICTURE", "")

                            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            val date = originalFormat.parse(birthDate)
                            val desiredFormat = SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault())
                            val desiredFormat2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formattedDate = desiredFormat.format(date)
                            val formattedDate2 = desiredFormat2.format(date)
                            bdate = formattedDate2

                            // Update the UI on the main thread
                            requireActivity().runOnUiThread {
                                userName.text = username
                                userFullName.text = fullName
                                userContact.text = contact
                                userEmail.text = email
                                userBDate.text = formattedDate

                                // Adjust the email text size if longer than 20 characters
                                if (email.length > 20) {
                                    userEmail.textSize = 14f // Reduce text size by 2 points from original 16sp
                                } else {
                                    userEmail.textSize = 16f // Set text size to original value
                                }

                                val ageTV = view?.findViewById<TextView>(R.id.age)
                                ageTV?.text = calculateAge(bdate).toString()

                                // Load the user photo from internal storage
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
                                        .into(userPhoto)
                                } else {
                                    // Set a default image if no picture is provided
                                    userPhoto.setImageResource(R.drawable.baseline_person_24)
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun deleteUser() {
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
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId == -1) {
            Toast.makeText(context, "Invalid user ID", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$baseUrl" + "api/deleteUser/$userId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Delete User", "Failed to delete user: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to delete user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Delete User", "User deleted successfully")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        logOut()
                    }
                } else {
                    Log.e("Delete User", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun confirmDeleteUser() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { _, _ ->
                deleteUser()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun editUser() {
        (activity as HomePageActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, EditProfileFragment())
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
            .commit()
    }

    private fun logOut() {
        // Clear the shared preferences
        try {
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

            val editor = sharedPreferences.edit()
            editor.clear() // Clear all preferences
            editor.apply() // Apply changes

        } catch (e: Exception) {
            Log.e("User Session", "Error clearing SharedPreferences: ${e.message}")
        }

        // Navigate to the MainActivity
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun calculateAge(birthDate: String): Int {
        if (birthDate.isEmpty()) {
            // If the birthDate is empty, return 0 as the age or handle it appropriately
            Log.e("Calculate Age", "Birthdate is empty, cannot calculate age.")
            return 0
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val birthDateCalendar = Calendar.getInstance()

        try {
            val date = dateFormat.parse(birthDate)
            birthDateCalendar.time = date
        } catch (e: ParseException) {
            e.printStackTrace()
            Log.e("Calculate Age", "Date parsing error: ${e.message}")
            return 0 // In case of parsing failure
        }

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR)

        // Check if the birthday hasn't occurred yet this year
        if (today.get(Calendar.MONTH) < birthDateCalendar.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birthDateCalendar.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) < birthDateCalendar.get(Calendar.DAY_OF_MONTH))
        ) {
            age--
        }

        return age
    }

}
