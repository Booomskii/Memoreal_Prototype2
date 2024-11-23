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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class GuestbookFragment : Fragment() {

    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private var userId = 0
    private var tributeId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_guestbook, container, false)

        // Initialize UI elements
        val editTextGuestName = view.findViewById<EditText>(R.id.editTextGuestName)
        val editTextMessage = view.findViewById<EditText>(R.id.editTextMessage)
        val buttonSubmitGuestbook = view.findViewById<Button>(R.id.buttonSubmitGuestbook)

        getUserId()
        val guestbook = com.example.memoreal_prototype.models.Guestbook(
            0,
            userId,
            editTextGuestName.text.toString(),
            editTextMessage.text.toString(),
            ""
        )

        buttonSubmitGuestbook.setOnClickListener {
            addGuestbook(guestbook)
        }

        return view
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

    private fun addGuestbook(guestbook: com.example.memoreal_prototype.models.Guestbook) {
        val url = baseUrl + "api/addGuestbook"

        val json = JSONObject().apply {
            put("USERID", guestbook.USERID)
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
                } else {
                    Log.e("Register Guestbook", "Failed to register guestbook: ${response
                        .message}")
                }
            }
        })
    }
}