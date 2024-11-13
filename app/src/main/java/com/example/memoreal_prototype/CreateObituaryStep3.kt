package com.example.memoreal_prototype

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.yalantis.ucrop.UCrop

class CreateObituaryStep3 : Fragment() {

    private lateinit var uploadImg: ImageView
    private var imageUri: Uri? = null
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = null  // Reset previous image URI
            startCrop(uri)    // Start the crop activity
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                sharedViewModel.image.value = null
                uploadImg.setImageURI(null)  // Clear previous image first
                uploadImg.setImageURI(resultUri)  // Set the new cropped image
                imageUri = resultUri            // Update stored image URI
                sharedViewModel.image.value = resultUri.toString()  // Update the ViewModel immediately
                Log.d("Step3Fragment", "Updated image URI: ${resultUri.toString()}")
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(requireContext(), "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step3, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val fullNameET = view.findViewById<EditText>(R.id.etFullName)
        val dateBirthET = view.findViewById<EditText>(R.id.etDateBirth)
        val datePassingET = view.findViewById<EditText>(R.id.etDatePassing)
        val biographyET = view.findViewById<EditText>(R.id.etBiography)

        sharedViewModel.fullName.observe(viewLifecycleOwner) { fullName ->
            fullNameET.setText(fullName)
        }

        sharedViewModel.dateBirth.observe(viewLifecycleOwner) { dateBirth ->
            dateBirthET.setText(dateBirth)
        }

        sharedViewModel.datePassing.observe(viewLifecycleOwner) { datePassing ->
            datePassingET.setText(datePassing)
        }

        sharedViewModel.biography.observe(viewLifecycleOwner) { biography ->
            biographyET.setText(biography)
        }

        sharedViewModel.image.observe(viewLifecycleOwner) { imageUriString ->
            imageUriString?.let {
                imageUri = Uri.parse(it)
                uploadImg.setImageURI(imageUri)
            }
        }

        uploadImg = view.findViewById(R.id.ivUploadPic)
        uploadImg.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        dateBirthET.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = dateBirthET.compoundDrawables[2]
                drawableEnd?.let {
                    if (event.rawX >= (dateBirthET.right - it.bounds.width())) {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                dateBirthET.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                            },
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePickerDialog.show()
                        return@setOnTouchListener true
                    }
                }
            }
            v.performClick()
            false
        }

        datePassingET.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = datePassingET.compoundDrawables[2]
                drawableEnd?.let {
                    if (event.rawX >= (datePassingET.right - it.bounds.width())) {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                datePassingET.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
                            },
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
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
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .commit()
        }

        nextButton.setOnClickListener {
            val fullName = fullNameET.text.toString()
            val dateBirthStr = dateBirthET.text.toString()
            val datePassingStr = datePassingET.text.toString()
            val biography = biographyET.text.toString()

            // Check imageUri properly before using it
            if (inputValidator(fullName, dateBirthStr, datePassingStr, biography, imageUri)) {
                val dateBirth = parseDate(dateBirthET.text.toString(), "Date of birth")
                val datePassing = parseDate(datePassingET.text.toString(), "Date of passing")
                if (dateBirth != null && datePassing != null && !dateValidator(dateBirth, datePassing)) {
                    Toast.makeText(
                        requireContext(),
                        "Date of Birth cannot be greater than Date of Passing",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    sharedViewModel.fullName.value = fullName
                    sharedViewModel.dateBirth.value = dateBirthStr
                    sharedViewModel.datePassing.value = datePassingStr
                    sharedViewModel.biography.value = biography
                    sharedViewModel.image.value = imageUri.toString()

                    (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, CreateObituaryStep4())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .addToBackStack("CreateObituaryStep3")
                        .commit()
                }
            }
        }

        return view
    }

    private fun inputValidator(fullName: String, dateBirth: String, datePassing: String, biography: String, imageUri: Uri?): Boolean {
        return when {
            fullName.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter the person's full name", Toast.LENGTH_SHORT).show()
                false
            }
            dateBirth.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter the person's date of birth", Toast.LENGTH_SHORT).show()
                false
            }
            datePassing.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter the person's date of passing", Toast.LENGTH_SHORT).show()
                false
            }
            biography.isEmpty() -> {
                Toast.makeText(requireContext(), "Enter the person's biography", Toast.LENGTH_SHORT).show()
                false
            }
            imageUri == null -> {
                Toast.makeText(requireContext(), "Please upload an image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    fun parseDate(dateStr: String, fieldName: String): Date? {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return if (dateStr.isNotEmpty()) {
            try {
                dateFormat.parse(dateStr)
            } catch (e: ParseException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Invalid $fieldName format", Toast.LENGTH_SHORT).show()
                null
            }
        } else {
            Toast.makeText(requireContext(), "$fieldName is required", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun dateValidator(dateBirth: Date, datePassing: Date): Boolean {
        return !dateBirth.after(Date()) && !datePassing.before(dateBirth)
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))

        // Convert 200dp and 230dp to pixels
        val widthPx = dpToPx(150f, requireContext())
        val heightPx = dpToPx(180f, requireContext())

        UCrop.of(uri, destinationUri)
            .withAspectRatio(150f, 180f) // Setting the aspect ratio to 200:230
            .withMaxResultSize(widthPx, heightPx) // Set maximum result size
            .start(requireContext(), this)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
