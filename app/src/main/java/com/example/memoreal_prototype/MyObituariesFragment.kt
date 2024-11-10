package com.example.memoreal_prototype

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.memoreal_prototype.models.Obituary

class MyObituariesFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl
    private var userId = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var obituaryAdapter: ObituaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_obituaries, container, false)
        setupToolbar(view)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Initialize with an empty list
        setupRecyclerView(emptyList())

        fetchObituariesByUser()

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }
    }

    private fun fetchObituariesByUser() {
        getUserId()
        val url = "$baseUrl"+"api/allObitByUser/"+"$userId"
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
                            obituaryAdapter.updateObituaries(obituaries)
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

    private fun getUserId(){
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Initialize EncryptedSharedPreferences
        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "userSession",  // File name
            masterKey,      // Master key for encryption
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        userId = sharedPreferences.getInt("userId", -1)
    }

    private fun parseObituaries(json: String): List<Obituary> {
        val jsonArray = JSONArray(json)
        val obituaries = mutableListOf<Obituary>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            obituaries.add(Obituary(
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
            ))
        }
        return obituaries
    }

    private fun setupRecyclerView(obituaries: List<Obituary>) {
        obituaryAdapter = ObituaryAdapter(obituaries) { obituaryId ->
            // Show a confirmation dialog before deleting
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Obituary")
                .setMessage("Are you sure you want to delete this obituary?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteObituary(obituaryId)
                }
                .setNegativeButton("No", null)
                .show()
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = obituaryAdapter
    }

    private fun deleteObituary(obituaryId: Int) {
        val url = "$baseUrl"+"api/deleteObituary/$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .method("DELETE", null)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Delete Obituary", "Failed to delete obituary: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to delete obituary", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Obituary deleted successfully", Toast.LENGTH_SHORT).show()
                        // Refresh the list of obituaries
                        fetchObituariesByUser()
                    }
                } else {
                    Log.e("Delete Obituary", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
