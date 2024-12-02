package com.example.memoreal_prototype

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.models.Obituary
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExploreFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl
    private lateinit var recyclerView: RecyclerView
    private lateinit var obituaryAdapter: ObituaryAdapter
    private lateinit var filterButton: Button
    private lateinit var searchInput: EditText
    private lateinit var dateBirthInput: TextView
    private lateinit var dateDeathInput: TextView

    private var selectedFilter: String = "All"
    private var selectedDateBirth: String? = null
    private var selectedDateDeath: String? = null
    private lateinit var noResultsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        setupToolbar(view)
        recyclerView = view.findViewById(R.id.recyclerView)
        noResultsTextView = view.findViewById(R.id.noResultsTextView)

        // Initialize RecyclerView with an empty list to start
        setupRecyclerView(emptyList())
        fetchObituaries()

        searchInput = view.findViewById(R.id.searchInput)
        dateBirthInput = view.findViewById(R.id.dateStartInput)
        dateDeathInput = view.findViewById(R.id.dateEndInput)
        filterButton = view.findViewById(R.id.filterButton)

        setupDatePickers()
        setupFilterButton()
        setupSearchListeners()

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)

        backButton.visibility = View.GONE
    }

    private fun setupRecyclerView(obituaries: List<Obituary>) {
        obituaryAdapter = ObituaryAdapter(
            obituaries,
            onItemClick = { obituary ->
                // Open details fragment with obituary ID
                val obituaryFragment = ObituaryFragment().apply {
                    arguments = Bundle().apply {
                        putInt("obituaryId", obituary.OBITUARYID)
                    }
                }
                (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, obituaryFragment)
                    .addToBackStack("ExploreFragment")
                    .commit()
            }
        )

        // Set layout manager for vertical scrolling
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = obituaryAdapter
    }


    private fun fetchObituaries() {
        val url = "$baseUrl" + "api/allObit"
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

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        // Date of Birth Picker for Start Date
        dateBirthInput.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDateBirth = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    dateBirthInput.text = selectedDateBirth
                    applyFilters()
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Date of Death Picker for End Date
        dateDeathInput.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    selectedDateDeath = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    dateDeathInput.text = selectedDateDeath
                    applyFilters()
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupFilterButton() {
        filterButton.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "Obituary Name", "Location", "Date of Birth", "Date of Death")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Filter Type")
            .setItems(options) { _, which ->
                selectedFilter = options[which]
                filterButton.text = selectedFilter
                filterButton.textSize = 12f // Set smaller font size for better UX when selecting options like "Obituary Name"
                adjustSearchUI()
                applyFilters() // Update the search results immediately if needed
            }
            .show()
    }

    private fun adjustSearchUI() {
        when (selectedFilter) {
            "Date of Birth", "Date of Death" -> {
                // Show date range inputs and hide search bar
                searchInput.visibility = View.GONE
                dateBirthInput.visibility = View.VISIBLE
                dateDeathInput.visibility = View.VISIBLE
            }
            else -> {
                // Show search bar and hide date range inputs
                searchInput.visibility = View.VISIBLE
                dateBirthInput.visibility = View.GONE
                dateDeathInput.visibility = View.GONE
            }
        }
    }

    private fun setupSearchListeners() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilters() {
        val query = when (selectedFilter) {
            "Date of Birth", "Date of Death" -> {
                val startDateStr = selectedDateBirth
                val endDateStr = selectedDateDeath

                if (!startDateStr.isNullOrEmpty() && !endDateStr.isNullOrEmpty()) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val startDate = dateFormat.parse(startDateStr)
                        val endDate = dateFormat.parse(endDateStr)

                        obituaryAdapter.filterByDateRange(startDate, endDate, selectedFilter)
                    } catch (e: ParseException) {
                        Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
                    }
                }
                ""
            }
            else -> searchInput.text.toString()
        }

        if (selectedFilter != "Date of Birth" && selectedFilter != "Date of Death") {
            obituaryAdapter.filter(query, selectedFilter)
        }

        // Show or hide the "No Results" message based on the filtered result count
        if (obituaryAdapter.itemCount == 0) {
            noResultsTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noResultsTextView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
