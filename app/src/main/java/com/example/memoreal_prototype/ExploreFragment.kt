package com.example.memoreal_prototype

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.models.Obituary
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class ExploreFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl
    private lateinit var recyclerView: RecyclerView
    private lateinit var obituaryAdapter: ObituaryAdapter
    private lateinit var searchInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        setupToolbar(view)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Initialize RecyclerView with an empty list to start
        setupRecyclerView(emptyList())
        fetchObituaries()

        // Initialize the search input
        searchInput = view.findViewById(R.id.searchInput)
        setupSearchListener()

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_out_left,
                    R.anim.slide_out_right
                )
                .commit()
        }
    }

    private fun setupRecyclerView(obituaries: List<Obituary>) {
        obituaryAdapter = ObituaryAdapter(obituaries)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
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

    private fun setupSearchListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                obituaryAdapter.filter(s.toString()) // Call the filter method
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
