package com.example.memoreal_prototype

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

class ProfileFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private lateinit var userName: TextView
    private lateinit var userFullName: TextView
    private lateinit var userContact: TextView
    private lateinit var userEmail: TextView
    private lateinit var userBDate: TextView
    private lateinit var userPhoto: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        setupToolbar(view)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)

        // Initialize the views here
        userName = view.findViewById(R.id.username)
        userFullName = view.findViewById(R.id.userFullName)
        userContact = view.findViewById(R.id.userContact)
        userEmail = view.findViewById(R.id.userEmail)
        userBDate = view.findViewById(R.id.userBDate)
        userPhoto = view.findViewById(R.id.userPhoto)

        // Call the function to fetch the user data
        fetchUser()

        deleteButton.setOnClickListener {
            confirmDeleteUser()
        }

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val settings = toolbar.findViewById<ImageView>(R.id.settings)

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }
    }

    private fun fetchUser () {
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

                            // Update the UI on the main thread
                            requireActivity().runOnUiThread {
                                userName.text = username
                                userFullName.text = fullName
                                userContact.text = contact
                                userEmail.text = email
                                userBDate.text = birthDate
                                // Optionally, load the user picture into userPhoto using a library like Glide or Picasso
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
                        // Optionally, navigate back or update the UI
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
                logOut()
            }
            .setNegativeButton("No", null)
            .show()
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
}
