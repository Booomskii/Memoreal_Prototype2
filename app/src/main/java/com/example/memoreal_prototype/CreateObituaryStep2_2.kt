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

class CreateObituaryStep2_2 : Fragment() {

    private var plan: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step2_2, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val proceedButton = view.findViewById<Button>(R.id.btnProceed2)
        val successMessage = view.findViewById<TextView>(R.id.tvSuccessMsg)

        arguments?.let {
            plan = it.getString("plan", "")
            Log.d("Step 2_2", "Plan: $plan")
        }

        if (plan == "Basic Plan" || plan == "Premium Plan" || plan == "Lifetime Plan"){
            successMessage.text = getString(R.string.paymentSuccessMsg2)
        }
        else {
            successMessage.text = getString(R.string.paymentSuccessMsg1)
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        proceedButton.setOnClickListener {
            val existingBundle = this.arguments
            val createObituaryStep3 = CreateObituaryStep3()
            createObituaryStep3.arguments = existingBundle
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, createObituaryStep3)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep2_2")
                .commit()
        }

        return view
    }
}