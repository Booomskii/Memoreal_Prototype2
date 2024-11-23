package com.example.memoreal_prototype

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

class AddFamilyDialogFragment : DialogFragment() {

    private val sharedViewModel: Step4SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_family, container, false)

        val etFamilyMemberName = view.findViewById<EditText>(R.id.etFamilyMemberName)
        val spinnerRelationship = view.findViewById<Spinner>(R.id.spinnerFamilyMemberRelationship)
        val etCustomRelationship = view.findViewById<EditText>(R.id.etCustomRelationship)
        val btnAddFamilyMember = view.findViewById<Button>(R.id.btnAddFamilyMember)
        val familyMemberList = view.findViewById<LinearLayout>(R.id.familyMemberList)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)

        // Set up the spinner with predefined relationships
        val relationships = listOf("Mother", "Father", "Sibling", "Spouse", "Child", "Grandparent", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationships)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRelationship.adapter = adapter

        setSpinnerTextColor(spinnerRelationship)

        // Show or hide the custom relationship EditText based on the selected item
        spinnerRelationship.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (relationships[position] == "Other") {
                    etCustomRelationship.visibility = View.VISIBLE
                } else {
                    etCustomRelationship.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Observe changes in the family member list and update the UI accordingly
        sharedViewModel.familyMembers.observe(viewLifecycleOwner) {
            updateFamilyMemberList(familyMemberList, it)
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnAddFamilyMember.setOnClickListener {
            val name = etFamilyMemberName.text.toString()
            val relationship = if (spinnerRelationship.selectedItem.toString() == "Other") {
                etCustomRelationship.text.toString()
            } else {
                spinnerRelationship.selectedItem.toString()
            }

            if (name.isNotEmpty() && relationship.isNotEmpty()) {
                sharedViewModel.addFamilyMember(name, relationship)

                // Clear the EditTexts for the next entry
                etFamilyMemberName.text.clear()
                etCustomRelationship.text.clear()
                spinnerRelationship.setSelection(0)
            } else {
                Toast.makeText(requireContext(), "Please enter both name and relationship", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun updateFamilyMemberList(familyMemberList: LinearLayout, familyMembers: List<Pair<String, String>>) {
        familyMemberList.removeAllViews()
        for ((index, familyMember) in familyMembers.withIndex()) {
            val familyMemberView = createFamilyMemberView(familyMember.first, familyMember.second, index)
            familyMemberList.addView(familyMemberView)
        }
    }

    private fun createFamilyMemberView(name: String, relationship: String, index: Int): View {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val nameTextView = TextView(requireContext()).apply {
            text = name
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val relationshipTextView = TextView(requireContext()).apply {
            text = relationship
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val removeButton = ImageView(requireContext()).apply {
            setImageResource(R.drawable.baseline_close_24)
            setOnClickListener {
                sharedViewModel.removeFamilyMember(index)
            }
        }

        layout.addView(nameTextView)
        layout.addView(relationshipTextView)
        layout.addView(removeButton)

        return layout
    }

    private fun setSpinnerTextColor(spinner: Spinner) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),  // 90% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
