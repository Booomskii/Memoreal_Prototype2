package com.example.memoreal_prototype

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
        val etFamilyMemberRelationship = view.findViewById<EditText>(R.id.etFamilyMemberRelationship)
        val btnAddFamilyMember = view.findViewById<Button>(R.id.btnAddFamilyMember)
        val familyMemberList = view.findViewById<LinearLayout>(R.id.familyMemberList)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)

        // Observe changes in the family member list and update the UI accordingly
        sharedViewModel.familyMembers.observe(viewLifecycleOwner) {
            updateFamilyMemberList(familyMemberList, it)
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        btnAddFamilyMember.setOnClickListener {
            val name = etFamilyMemberName.text.toString()
            val relationship = etFamilyMemberRelationship.text.toString()

            if (name.isNotEmpty() && relationship.isNotEmpty()) {
                sharedViewModel.addFamilyMember(name, relationship)

                // Clear the EditTexts for the next entry
                etFamilyMemberName.text.clear()
                etFamilyMemberRelationship.text.clear()
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

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),  // 90% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}


