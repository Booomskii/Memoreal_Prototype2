package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.memoreal_prototype.adapters.GuestbookAdapter
import com.example.memoreal_prototype.models.Guestbook
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GuestbookFragment : Fragment() {

    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private var userId = 0
    private var guestbookId = 0
    private var isEditMode = false
    private lateinit var editTextGuestName: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSubmitGuestbook: Button
    private lateinit var guestbookAdapter: GuestbookAdapter
    private val guestbookList = mutableListOf<Guestbook>()
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ENAGUESTBOOK", sharedViewModel.enaGuestbook.value.toString())
        val view: View = if (sharedViewModel.enaGuestbook.value == false) {
            inflater.inflate(R.layout.fragment_guestbook_disabled, container, false)
        } else {
            inflater.inflate(R.layout.fragment_guestbook, container, false)
        }

        if (sharedViewModel.enaGuestbook.value == true) {
            // Initialize UI elements after the correct layout has been inflated
            editTextGuestName = view.findViewById(R.id.editTextGuestName)
            editTextMessage = view.findViewById(R.id.editTextMessage)
            buttonSubmitGuestbook = view.findViewById(R.id.buttonSubmitGuestbook)
            val recyclerViewGuestbook = view.findViewById<RecyclerView>(R.id.recyclerViewGuestbook)

            // Set up RecyclerView
            guestbookAdapter = GuestbookAdapter(guestbookList)
            recyclerViewGuestbook.layoutManager = LinearLayoutManager(requireContext())
            recyclerViewGuestbook.adapter = guestbookAdapter

            getUserId()

            sharedViewModel.obituaryId.observe(viewLifecycleOwner) { id ->
                if (id != null) {
                    fetchGuestbook(id)
                }
            }

            buttonSubmitGuestbook.setOnClickListener {
                val guestName = editTextGuestName.text.toString()
                val message = editTextMessage.text.toString()

                val guestbook = Guestbook(
                    GUESTBOOKID = 0,
                    USERID = userId,
                    OBITUARYID = sharedViewModel.obituaryId.value!!,
                    GUESTNAME = guestName,
                    MESSAGE = message,
                    POSTINGDATE = "",
                    PROFILEPICTURE = "",
                    FULLNAME = ""
                )

                if (isEditMode) {
                    updateGuestbook(guestbook) // Update the existing entry
                } else {
                    addGuestbook(guestbook) // Add a new entry
                }
            }
        }

        return view
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
        fetchUser(userId)
    }

    private fun addGuestbook(guestbook: Guestbook) {
        val url = baseUrl + "api/addGuestbook"

        val json = JSONObject().apply {
            put("USERID", guestbook.USERID)
            put("OBITUARYID", guestbook.OBITUARYID)
            put("GUESTNAME", guestbook.GUESTNAME)
            put("MESSAGE", guestbook.MESSAGE)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register Guestbook", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Register Guestbook", "Guestbook registered successfully")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Guestbook entry added", Toast.LENGTH_SHORT).show()

                        // Update the button text to "Save" and switch to edit mode
                        buttonSubmitGuestbook.text = getString(R.string.save)
                        isEditMode = true

                        // Fetch the guestbook entries again to refresh the list (optional)
                        fetchGuestbook(sharedViewModel.obituaryId.value!!)
                    }
                } else {
                    Log.e("Register Guestbook", "Failed to register guestbook: ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            context, "Only Registered Users can enlist to the Guestbook",
                            Toast
                                .LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun updateGuestbook(guestbook: Guestbook) {
        val url = baseUrl + "api/updateGuestbook/" + guestbookId

        val json = JSONObject().apply {
            put("USERID", guestbook.USERID)
            put("OBITUARYID", guestbook.OBITUARYID)
            put("GUESTNAME", guestbook.GUESTNAME)
            put("MESSAGE", guestbook.MESSAGE)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Update Guestbook", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Update Guestbook", "Guestbook updated successfully")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Guestbook entry updated", Toast.LENGTH_SHORT).show()
                        fetchGuestbook(sharedViewModel.obituaryId.value!!) // Refresh the list after updating the entry
                    }
                } else {
                    Log.e("Update Guestbook", "Failed to update guestbook: ${response.message}")
                }
            }
        })
    }

    private fun fetchGuestbook(obituaryId: Int) {
        val url = baseUrl + "api/allGuestbook?OBITUARYID=$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Guestbook", "Failed to fetch guestbook: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to fetch guestbook", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)

                        guestbookList.clear()
                        isEditMode = false // Reset edit mode initially

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val GBId = jsonObject.getInt("GUESTBOOKID")
                            val guestbookUserId = jsonObject.getInt("USERID")

                            val firstname = jsonObject.optString("FIRST_NAME", "")
                            val middleInitial = jsonObject.optString("MI", "")
                            val lastname = jsonObject.optString("LAST_NAME", "")
                            val guestName = jsonObject.getString("GUESTNAME")
                            val message = jsonObject.getString("MESSAGE")
                            val fullName = "$firstname $middleInitial $lastname".trim()

                            val guestbookEntry = Guestbook(
                                GUESTBOOKID = GBId,
                                USERID = guestbookUserId,
                                OBITUARYID = jsonObject.getInt("OBITUARYID"),
                                GUESTNAME = guestName,
                                MESSAGE = message,
                                POSTINGDATE = jsonObject.getString("POSTINGDATE"),
                                PROFILEPICTURE = jsonObject.getString("PICTURE"),
                                FULLNAME = fullName
                            )

                            guestbookList.add(guestbookEntry)

                            // Update the EditTexts and button text if the entry belongs to the current user
                            if (guestbookUserId == userId) {
                                requireActivity().runOnUiThread {
                                    guestbookId = GBId
                                    editTextGuestName.setText(guestName)
                                    editTextMessage.setText(message)
                                    buttonSubmitGuestbook.text = getString(R.string.save)
                                    isEditMode = true // Set edit mode to true for the current user's entry
                                }
                            }
                        }

                        requireActivity().runOnUiThread {
                            guestbookAdapter.notifyDataSetChanged()
                        }
                    }
                } else {
                    Log.e("Fetch Guestbook", "Failed to fetch guestbook: ${response.message}")
                }
            }
        })
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

                            val firstname = jsonObject.optString("FIRST_NAME", "")
                            val middleInitial = jsonObject.optString("MI", "")
                            val lastname = jsonObject.optString("LAST_NAME", "")

                            val fullName = "$firstname $middleInitial $lastname".trim()

                            // Update the UI on the main thread
                            requireActivity().runOnUiThread {
                                editTextGuestName.setText(fullName)
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
}

