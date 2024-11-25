package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class ResetPasswordFragment : Fragment() {

    private var userId: Int = -1

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private lateinit var userPassword: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)
        setupToolbar(view)

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val save = toolbar.findViewById<TextView>(R.id.save)

        getUserId()

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, ProfileFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        save.setOnClickListener {
            userPassword = view.findViewById(R.id.etPassword)

            updatePassword(userPassword.text.toString())
        }
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

    private fun updatePassword(password: String){
        val url = "$baseUrl" + "api/updateUserPass"
        Log.d("API", "Requesting URL: $url")

        val json = JSONObject().apply {
            put("USERID", userId)
            put("PASSWORD", password)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Update User Password", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(), "User password updated successfully", Toast
                                .LENGTH_LONG
                        ).show()
                        (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, ProfileFragment())
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left,
                                R.anim.slide_out_left,
                                R.anim.slide_out_right
                            )
                            .commit()
                    }
                } else {
                    Log.e("Update User Password", "Error: ${response.code} - ${response.message}")

                    val errorBody = response.body?.string()
                    requireActivity().runOnUiThread {
                        if (!errorBody.isNullOrEmpty()) {
                            try {
                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown error")
                                Log.e(
                                    "Update User Password",
                                    "Server returned error message:$errorMessage"
                                )
                                Toast.makeText(
                                    requireContext(),
                                    "Update failed: $errorMessage",
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Log.e(
                                    "Update User Password",
                                    "Could not parse error message: ${errorBody}"
                                )
                                Toast.makeText(
                                    requireContext(),
                                    "Update failed: Could not parse error message",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Log.e(
                                "Update User Password",
                                "Update failed: Empty response from server"
                            )
                            Toast.makeText(
                                requireContext(),
                                "Update failed: Empty response from server",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }
}