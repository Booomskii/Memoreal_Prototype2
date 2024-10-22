package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class CreateObituaryStep1 : Fragment() {
    private var isPlanSelected = false
    private var selectedButton: Button? = null
    private var bundle: Bundle? = null
    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_obituary_step1, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val proceed = view.findViewById<Button>(R.id.btnProceed)
        val button1 = view.findViewById<Button>(R.id.btnPck1)
        val button2 = view.findViewById<Button>(R.id.btnPck2)
        val button3 = view.findViewById<Button>(R.id.btnPck3)
        val button4 = view.findViewById<Button>(R.id.btnPck4)

        val buttons = listOf(button1, button2, button3, button4)

        // Set click listeners for all buttons
        for (button in buttons) {
            button.setOnClickListener {
                onPackageSelected(button)
            }
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        proceed.setOnClickListener {
            val createObituaryStep2 = CreateObituaryStep2()
            createObituaryStep2.arguments = bundle

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, createObituaryStep2)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep1")
                .commit()
        }

        (activity as HomePageActivity).showBottomNavigation()

        return view
    }

    private fun onPackageSelected(clickedButton: Button) {
        // Reset previously selected button, if any
        selectedButton?.apply {
            setBackgroundColor(resources.getColor(R.color.memo_orange)) // Default color
            setTextColor(resources.getColor(R.color.black))
        }

        // Set clicked button as selected
        clickedButton.setBackgroundColor(resources.getColor(R.color.memo_light_orange)) // New color for
        // selection
        clickedButton.setTextColor(resources.getColor(R.color.white)) // Change text color to white for visibility

        // Update selected button
        selectedButton = clickedButton

        val selectedPackage: String = when (clickedButton.id) {
            R.id.btnPck1 -> getString(R.string.package1)
            R.id.btnPck2 -> getString(R.string.package2)
            R.id.btnPck3 -> getString(R.string.package3)
            R.id.btnPck4 -> getString(R.string.package4)
            else -> ""
        }

        // Prepare the bundle with the selected value
        bundle = Bundle().apply { // Update the class-level bundle
            putBoolean("isRadioButton1Checked", true)
            putString("selectedPackage", selectedPackage) // Put selected package value in the bundle
        }
    }
}