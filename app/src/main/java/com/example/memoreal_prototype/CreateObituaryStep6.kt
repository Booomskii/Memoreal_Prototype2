package com.example.memoreal_prototype

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat

class CreateObituaryStep6 : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step6, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val guestBookSwitch = view.findViewById<SwitchCompat>(R.id.switchGuestbook)
        val privacySpinner = view.findViewById<Spinner>(R.id.spinnerPrivacy)

        val sharedPreferences = requireContext().getSharedPreferences("GuestbookSwitchState",
            Context
                .MODE_PRIVATE)
        val switchState = sharedPreferences.getBoolean("switch_state", false)
        guestBookSwitch.isChecked = switchState

        Log.d("STEP 6 SF - Bundle:", this.arguments.toString())

        guestBookSwitch.setOnCheckedChangeListener { _, isChecked ->
            val sharedPreferences = requireContext().getSharedPreferences("GuestbookSwitchState", Context
                .MODE_PRIVATE)
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

        val funeralDateTime = arguments?.getString("funeralDateTime")
        funeralDateTime?.let {
            Log.d("funeralDateTime", it.toString()) // Log the media URIs
        } ?: Log.d("funeralDateTime", "No funeralDateTime received")

        // Retrieve family names
        val funeralLocation = arguments?.getString("funeralLocation")
        funeralLocation?.let {
            Log.d("funeralLocation", it.toString()) // Log the family names
        } ?: Log.d("funeralLocation", "No funeralLocation received")

        // Retrieve family relationships
        val funeralAdtlInfo = arguments?.getString("funeralAdtlInfo")
        funeralAdtlInfo?.let {
            Log.d("funeralAdtlInfo", it.toString()) // Log the family relationships
        } ?: Log.d("funeralAdtlInfo", "No funeralAdtlInfo received")

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        nextButton.setOnClickListener {
            val bundle = Bundle().apply{
                putBoolean("guestBookSwitchState", guestBookSwitch.isChecked)
                putString("privacyType", privacySpinner.selectedItem.toString())
            }

            val createObituaryStep7 = CreateObituaryStep7()
            val existingBundle = this.arguments
            existingBundle?.let { bundle.putAll(it) }
            createObituaryStep7.arguments = bundle

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, createObituaryStep7)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep5")
                .commit()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep5())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        return view
    }

}