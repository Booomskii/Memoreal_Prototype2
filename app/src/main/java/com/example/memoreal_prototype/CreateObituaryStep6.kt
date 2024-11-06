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
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.activityViewModels

class CreateObituaryStep6 : Fragment() {

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step6, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val guestBookSwitch = view.findViewById<SwitchCompat>(R.id.switchGuestbook)
        val privacySpinner = view.findViewById<Spinner>(R.id.spinnerPrivacy)

        sharedViewModel.guestBook.observe(viewLifecycleOwner) { isChecked ->
            guestBookSwitch.isChecked = isChecked ?: false
        }

        guestBookSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedViewModel.guestBook.value = isChecked
        }

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

        sharedViewModel.privacy.observe(viewLifecycleOwner) { privacy ->
            val position = adapter.getPosition(privacy)
            if (position >= 0) privacySpinner.setSelection(position)
        }

        // Save selected privacy option to ViewModel
        privacySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
                sharedViewModel.privacy.value = privacySpinner.selectedItem.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if no item is selected
            }
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        nextButton.setOnClickListener {
            sharedViewModel.guestBook.value = guestBookSwitch.isChecked
            sharedViewModel.privacy.value = privacySpinner.selectedItem.toString()

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep7())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep6")
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