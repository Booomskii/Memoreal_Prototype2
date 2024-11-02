package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels

class CreateObituaryStep2 : Fragment() {

    private var isRadioButton1Checked: Boolean = false
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step2, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val payNow = view.findViewById<Button>(R.id.btnPayNow)
        val packTextView = view.findViewById<TextView>(R.id.tvPack)
        val creationFeeTextView = view.findViewById<TextView>(R.id.tvCreationFee)
        val packFeeTextView = view.findViewById<TextView>(R.id.tvPackFee)
        val totalTV = view.findViewById<TextView>(R.id.tvTotalAmount)
        var creationFee = 0.0f
        creationFee = getString(R.string.memorialCreationFeeDec).toFloat()
        val formattedCreationFee = "₱${"%.2f".format(creationFee)}"
        var packFee = 0.0f

        val selectedPackage = sharedViewModel.selectedPackage.value
        Log.d("CreateObituaryStep2", "Package: $selectedPackage")

        if (selectedPackage == "10 CREDITS") {
            packTextView.text = getString(R.string.package1)
            packFeeTextView.text = "₱${"%.2f".format(getString(R.string.package1Price).toFloat())}"
            packFee = getString(R.string.package1Price).toFloat()
        } else if (selectedPackage == "25 CREDITS") {
            packTextView.text = getString(R.string.package2)
            packFeeTextView.text = "₱${"%.2f".format(getString(R.string.package2Price).toFloat())}"
            packFee = getString(R.string.package2Price).toFloat()
        } else if (selectedPackage == "75 CREDITS") {
            packTextView.text = getString(R.string.package3)
            packFeeTextView.text = "₱${"%.2f".format(getString(R.string.package3Price).toFloat())}"
            packFee = getString(R.string.package3Price).toFloat()
        } else if (selectedPackage == "125 CREDITS") {
            packTextView.text = getString(R.string.package4)
            packFeeTextView.text = "₱${"%.2f".format(getString(R.string.package4Price).toFloat())}"
            packFee = getString(R.string.package4Price).toFloat()
        } else if (selectedPackage == ""){
            packTextView.text = getString(R.string.noPack)
            packFeeTextView.text = getString(R.string.zero)
        }

        creationFeeTextView.text = formattedCreationFee
        val total = creationFee + packFee
        totalTV.text = "₱${"%.2f".format(total)}".format(total)

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        payNow.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep2_2())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep2")
                .commit()
        }

        (activity as HomePageActivity).showBottomNavigation()

        return view
    }
}