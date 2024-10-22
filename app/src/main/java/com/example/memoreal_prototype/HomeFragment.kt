package com.example.memoreal_prototype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val fragmentManager = activity?.supportFragmentManager
        val activity = activity
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

        val btnCreateObituary = view.findViewById<Button>(R.id.btnCreateObituary)
        val btnBrowse = view.findViewById<Button>(R.id.buttonBrowse)
        val username = view.findViewById<TextView>(R.id.tvUsername)

        btnCreateObituary.setOnClickListener {
            fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, CreateObituaryStep1()) // Replace with MyObituaries fragment
                ?.addToBackStack(null) // Add to back stack for navigation back
                ?.commit()

            btnCreateObituary.isEnabled = false
        }

        val storedUsername = sharedPreferences.getString("username", "")
        if (sharedPreferences.getBoolean("isGuestUser", false)) {
            username.text = "Guest User"
        } else {
            username.text = storedUsername
        }

        return view
    }
}