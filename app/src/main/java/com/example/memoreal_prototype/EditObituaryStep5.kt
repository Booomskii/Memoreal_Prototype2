package com.example.memoreal_prototype

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.memoreal_prototype.models.Obituary
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditObituaryStep5 : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()
    private var fetchedObituary: Obituary? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step5, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val funeralDateTimeET = view.findViewById<EditText>(R.id.etFunDateTime)
        val funeralLocationET = view.findViewById<EditText>(R.id.etFunLocation)
        val funeralAdtlInfoET = view.findViewById<EditText>(R.id.etAdtlInfo)
        val obituaryId = sharedViewModel.obituaryId.value

        sharedViewModel.funeralDateTime.observe(viewLifecycleOwner) { funeralDateTime ->
            funeralDateTimeET.setText(funeralDateTime)
        }

        sharedViewModel.funeralLocation.observe(viewLifecycleOwner) { funeralLocation ->
            funeralLocationET.setText(funeralLocation)
        }

        sharedViewModel.funeralAdtlInfo.observe(viewLifecycleOwner) { funeralAdtlInfo ->
            funeralAdtlInfoET.setText(funeralAdtlInfo)
        }

        funeralDateTimeET.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val calendar = Calendar.getInstance()

                // Show DatePickerDialog
                val datePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _, year, month, dayOfMonth ->
                        // After selecting the date, show TimePickerDialog
                        val timePickerDialog = TimePickerDialog(
                            requireContext(),
                            { _, hourOfDay, minute ->
                                // Format the date and time for MSSQL format (yyyy-MM-dd HH:mm:ss)
                                val calendar = Calendar.getInstance().apply {
                                    set(year, month, dayOfMonth, hourOfDay, minute)
                                }
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val formattedDate = dateFormat.format(calendar.time)
                                funeralDateTimeET.setText(formattedDate)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true // Use 24-hour format
                        )
                        timePickerDialog.show()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
                return@setOnTouchListener true
            }
            v.performClick()
            false
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
            sharedViewModel.funeralDateTime.value = funeralDateTimeET.text.toString()
            sharedViewModel.funeralLocation.value = funeralLocationET.text.toString()
            sharedViewModel.funeralAdtlInfo.value = funeralAdtlInfoET.text.toString()

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep6())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("EditObituaryStep5")
                .commit()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep4())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        if (obituaryId != null) {
            fetchObituaryById(obituaryId)
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
                                    Log.d("Fetch Obituary", "Updating sharedViewModel with fetched data")
                                    val originalFormat = SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss" +
                                            ".SSS'Z'", Locale.getDefault())
                                    val date = originalFormat.parse(it.FUNDATETIME!!)
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    val formattedDate = date?.let { dateFormat.format(it) }
                                    val formattedDate2 = date?.let { timeFormat.format(it) }

                                    sharedViewModel.funeralDateTime.value = formattedDate + " " + formattedDate2
                                    sharedViewModel.funeralLocation.postValue(it.FUNLOCATION)
                                    sharedViewModel.funeralAdtlInfo.postValue(it.ADTLINFO)
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