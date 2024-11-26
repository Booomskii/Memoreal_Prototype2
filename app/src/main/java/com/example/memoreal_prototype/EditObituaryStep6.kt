package com.example.memoreal_prototype

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.activityViewModels
import com.example.memoreal_prototype.models.Obituary
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class EditObituaryStep6 : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()
    private var fetchedObituary: Obituary? = null
    private lateinit var guestBookSwitch: SwitchCompat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_edit_obituary_step6, container, false)

        // Initialize guestBookSwitch here, immediately after inflating the view
        guestBookSwitch = view.findViewById(R.id.switchGuestbook)

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val privacySpinner = view.findViewById<Spinner>(R.id.spinnerPrivacy)
        val obituaryId = sharedViewModel.obituaryId.value

        if (obituaryId != null) {
            Log.d("EditObituaryStep6", "Fetching obituary with ID: $obituaryId")
            fetchObituaryById(obituaryId)
        } else {
            Log.d("EditObituaryStep6", "No obituary ID found in sharedViewModel")
        }

        Log.d("Funeral Date Time:", sharedViewModel.funeralDateTime.value ?: "Not available")

        // Observe changes in guestBook state
        sharedViewModel.guestBook.observe(viewLifecycleOwner) { isChecked ->
            Log.d("EditObituaryStep6", "guestBookSwitch observed change: isChecked = $isChecked")
            guestBookSwitch.isChecked = isChecked ?: false // Ensure it defaults to false if null
        }

        // Set the initial state of the guestBookSwitch from the sharedViewModel value
        guestBookSwitch.isChecked = sharedViewModel.guestBook.value ?: false
        Log.d("EditObituaryStep6", "Initial guestBookSwitch state: isChecked = ${guestBookSwitch.isChecked}")

        // Set a listener for the switch to update the ViewModel when toggled
        guestBookSwitch.setOnCheckedChangeListener { _, isChecked ->
            Log.d("EditObituaryStep6", "guestBookSwitch changed: isChecked = $isChecked")
            sharedViewModel.guestBook.value = isChecked // This should trigger the observer

            // Save the switch state to SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("GuestbookSwitchState", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean("switch_state", isChecked)
                apply()
            }
        }

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.privacy_spinner,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        privacySpinner.adapter = adapter

        sharedViewModel.privacy.observe(viewLifecycleOwner) { privacy ->
            val position = adapter.getPosition(privacy)
            if (position >= 0) privacySpinner.setSelection(position)
        }

        // Save selected privacy option to ViewModel
        privacySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedPrivacy = privacySpinner.selectedItem.toString()
                (view as? TextView)?.setTextColor(Color.BLACK)
                sharedViewModel.privacy.value = selectedPrivacy
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if no item is selected
            }
        }

        backButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Return")
                .setMessage("Are you sure you want to go back? Changes made will not be saved!")
                .setPositiveButton("Yes") { _, _ ->
                    (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, MyObituariesFragment())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                        .commit()
                }
                .setNegativeButton("No", null)
                .show()
        }

        nextButton.setOnClickListener {
            Log.d("EditObituaryStep6", "Next button clicked")
            sharedViewModel.guestBook.value = guestBookSwitch.isChecked
            sharedViewModel.privacy.value = privacySpinner.selectedItem.toString()

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep7())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("EditObituaryStep6")
                .commit()
        }

        prevButton.setOnClickListener {
            Log.d("EditObituaryStep6", "Previous button clicked")
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep5())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        return view
    }

    private fun fetchObituaryById(obituaryId: Int) {
        val url = "$baseUrl" + "api/fetchObit/$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Obituary", "Failed to fetch obituary: ${e.message}")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to fetch obituary", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        Log.d("Fetch Obituary", "Response received: $responseBody")
                        val jsonObject = JSONObject(responseBody)
                        val obitCustIdArray = jsonObject.getJSONArray("OBITCUSTID")
                        val obitCustId = obitCustIdArray.getInt(0)

                        fetchedObituary = Obituary(
                            OBITUARYID = jsonObject.getInt("OBITUARYID"),
                            USERID = jsonObject.getInt("USERID"),
                            GALLERYID = jsonObject.getInt("GALLERYID"),
                            OBITCUSTID = obitCustId,
                            FAMILYID = jsonObject.getInt("FAMILYID"),
                            BIOGRAPHY = jsonObject.optString("BIOGRAPHY"),
                            OBITUARYNAME = jsonObject.getString("OBITUARYNAME"),
                            OBITUARYPHOTO = jsonObject.getString("OBITUARY_PHOTO"),
                            DATEOFBIRTH = jsonObject.getString("DATEOFBIRTH"),
                            DATEOFDEATH = jsonObject.getString("DATEOFDEATH"),
                            OBITUARYTEXT = jsonObject.optString("OBITUARYTEXT"),
                            KEYEVENTS = jsonObject.optString("KEYEVENTS"),
                            FUNDATETIME = jsonObject.optString("FUN_DATETIME"),
                            FUNLOCATION = jsonObject.optString("FUN_LOCATION"),
                            ADTLINFO = jsonObject.optString("ADTLINFO"),
                            PRIVACY = jsonObject.getString("PRIVACY"),
                            ENAGUESTBOOK = jsonObject.getBoolean("ENAGUESTBOOK"),
                            FAVORITEQUOTE = jsonObject.optString("FAVORITEQUOTE"),
                            CREATIONDATE = jsonObject.getString("CREATIONDATE"),
                            LASTMODIFIED = jsonObject.getString("LASTMODIFIED")
                        )

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                fetchedObituary?.let {
                                    Log.d("Fetch Obituary", "Updating sharedViewModel with fetched obituary data")
                                    sharedViewModel.enaGuestbook.postValue(it.ENAGUESTBOOK)
                                    guestBookSwitch.isChecked = it.ENAGUESTBOOK
                                }
                            }
                        }
                    } ?: run {
                        Log.e("Fetch Obituary", "Response body is null.")
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch Obituary", "Error: ${response.code} - ${response.message}")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}