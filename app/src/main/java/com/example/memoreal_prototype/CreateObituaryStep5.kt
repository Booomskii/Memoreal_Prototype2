package com.example.memoreal_prototype

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import java.util.Calendar

class CreateObituaryStep5 : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        val funeralDateTime = view.findViewById<EditText>(R.id.etFunDateTime)
        val funeralLocation = view.findViewById<EditText>(R.id.etFunLocation)
        val funeralAdtlInfo = view.findViewById<EditText>(R.id.etAdtlInfo)

        val mediaList = arguments?.getStringArrayList("mediaList")
        mediaList?.let {
            Log.d("mediaList", it.toString()) // Log the media URIs
        } ?: Log.d("mediaList", "No media URIs received")

        // Retrieve family names
        val familyNames = arguments?.getStringArrayList("familyNames")
        familyNames?.let {
            Log.d("familyNames", it.toString()) // Log the family names
        } ?: Log.d("familyNames", "No family names received")

        // Retrieve family relationships
        val familyRelationships = arguments?.getStringArrayList("familyRelationships")
        familyRelationships?.let {
            Log.d("familyRelationships", it.toString()) // Log the family relationships
        } ?: Log.d("familyRelationships", "No family relationships received")

        // Retrieve obituary text
        val obituaryText = arguments?.getString("obituaryText")
        obituaryText?.let {
            Log.d("obituaryText", it) // Log the obituary text
        } ?: Log.d("obituaryText", "No obituary text received")

        // Retrieve key events
        val keyEvents = arguments?.getString("keyEvents")
        keyEvents?.let {
            Log.d("keyEvents", it) // Log the key events text
        } ?: Log.d("keyEvents", "No key events received")

        funeralDateTime.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = funeralDateTime.compoundDrawables[2]
                drawableEnd?.let {
                    if (event.rawX >= (funeralDateTime.right - it.bounds.width())) {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                funeralDateTime.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                            },
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(
                                Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                        return@setOnTouchListener true
                    }
                }
            }
            v.performClick()
            false
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        nextButton.setOnClickListener {
            val bundle = Bundle().apply{
                putString("funeralDateTime", funeralDateTime.text.toString())
                putString("funeralLocation", funeralLocation.text.toString())
                putString("funeralAdtlInfo", funeralAdtlInfo.text.toString())
            }

            val createObituaryStep5 = CreateObituaryStep5()
            createObituaryStep5.arguments = bundle

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep6())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep4")
                .commit()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep4())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        return view
    }
}