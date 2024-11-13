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
import androidx.fragment.app.activityViewModels
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateObituaryStep5 : Fragment() {

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

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
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        nextButton.setOnClickListener {
            sharedViewModel.funeralDateTime.value = funeralDateTimeET.text.toString()
            sharedViewModel.funeralLocation.value = funeralLocationET.text.toString()
            sharedViewModel.funeralAdtlInfo.value = funeralAdtlInfoET.text.toString()

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep6())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep5")
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