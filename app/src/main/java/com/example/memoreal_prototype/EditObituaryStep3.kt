package com.example.memoreal_prototype

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import com.example.memoreal_prototype.models.Obituary
import com.example.memoreal_prototype.models.Obituary_Customization
import com.yalantis.ucrop.UCrop
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditObituaryStep3 : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private lateinit var uploadImg: ImageView
    private var imageUri: Uri? = null
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    private var fetchedObituary: Obituary? = null

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
        val view = inflater.inflate(R.layout.fragment_edit_obituary_step3, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val fullNameET = view.findViewById<EditText>(R.id.etFullName)
        val dateBirthET = view.findViewById<EditText>(R.id.etDateBirth)
        val datePassingET = view.findViewById<EditText>(R.id.etDatePassing)
        val biographyET = view.findViewById<EditText>(R.id.etBiography)
        val obituaryId = arguments?.getInt("obituaryId")

        if (obituaryId != null) {
            sharedViewModel.obituaryId.value = obituaryId
            fetchObituaryById(obituaryId)
        }

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

        // Updated DatePickerDialog for Date of Birth EditText
        dateBirthET.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = dateBirthET.compoundDrawables[2]
                drawableEnd?.let {
                    if (event.rawX >= (dateBirthET.right - it.bounds.width())) {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                // Format the selected date as yyyy-MM-dd for MSSQL compatibility
                                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                dateBirthET.setText(formattedDate)
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

        // Updated DatePickerDialog for Date of Passing EditText
        datePassingET.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = datePassingET.compoundDrawables[2]
                drawableEnd?.let {
                    if (event.rawX >= (datePassingET.right - it.bounds.width())) {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                // Format the selected date as yyyy-MM-dd for MSSQL compatibility
                                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                datePassingET.setText(formattedDate)
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
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Return")
                .setMessage("Are you sure you want to go back? Changes made will not be saved!")
                .setPositiveButton("Yes") { _, _ ->
                    (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, MyObituariesFragment())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                        .commit()
                }
                .setNegativeButton("No", null)
                .show()
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
                        .replace(R.id.frame_layout, EditObituaryStep4())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                        .addToBackStack("EditObituaryStep3")
                        .commit()
                }
            }
        }

        return view
    }

    private fun fetchObituaryById(obituaryId: Int) {
        val url = "$baseUrl" + "api/fetchObit/$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Obituary", "Failed to fetch obituary: ${e.message}")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to fetch obituary", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody)
                        val obitCustIdArray = jsonObject.getJSONArray("OBITCUSTID")
                        val obitCustId = obitCustIdArray.getInt(0)

                        sharedViewModel.clearData()

                        fetchedObituary = Obituary(
                            OBITUARYID = jsonObject.getInt("OBITUARYID"),
                            USERID = jsonObject.getInt("USERID"),
                            GALLERYID = jsonObject.getInt("GALLERYID"),
                            OBITCUSTID = obitCustId,
                            FAMILYID = jsonObject.getInt("FAMILYID"),
                            BIOGRAPHY = jsonObject.optString("BIOGRAPHY"),
                            OBITUARYNAME = jsonObject.getString("OBITUARYNAME"),
                            OBITUARYPHOTO = jsonObject.getString("OBITUARY_PHOTO"),
                            DATEOFBIRTH = jsonObject.getString("DATEOFBIRTH"),
                            DATEOFDEATH = jsonObject.getString("DATEOFDEATH"),
                            OBITUARYTEXT = jsonObject.optString("OBITUARYTEXT"),
                            KEYEVENTS = jsonObject.optString("KEYEVENTS"),
                            FUNDATETIME = jsonObject.optString("FUN_DATETIME"),
                            FUNLOCATION = jsonObject.optString("FUN_LOCATION"),
                            ADTLINFO = jsonObject.optString("ADTLINFO"),
                            PRIVACY = jsonObject.getString("PRIVACY"),
                            ENAGUESTBOOK = jsonObject.getBoolean("ENAGUESTBOOK"),
                            FAVORITEQUOTE = jsonObject.optString("FAVORITEQUOTE"),
                            CREATIONDATE = jsonObject.getString("CREATIONDATE"),
                            LASTMODIFIED = jsonObject.getString("LASTMODIFIED")
                        )

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                fetchedObituary?.let {
                                    Log.d("Fetch Obituary", "Updating sharedViewModel with fetched data")
                                    val originalFormat = SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss" +
                                            ".SSS'Z'", Locale.getDefault())
                                    val date = originalFormat.parse(it.DATEOFBIRTH)
                                    val date2 = originalFormat.parse(it.DATEOFDEATH)

                                    // Change the desired format to "MMM. dd, yyyy" to get "Nov. 19, 2024"
                                    val desiredFormat = SimpleDateFormat("yyyy-dd-MM", Locale.getDefault())

                                    val formattedDate = date?.let { desiredFormat.format(it) }
                                    val formattedDate2 = date2?.let { desiredFormat.format(it) }

                                    sharedViewModel.obituaryId.postValue(it.OBITUARYID)
                                    sharedViewModel.fullName.postValue(it.OBITUARYNAME)
                                    sharedViewModel.dateBirth.postValue(formattedDate)
                                    sharedViewModel.datePassing.postValue(formattedDate2)
                                    sharedViewModel.biography.postValue(it.BIOGRAPHY)
                                    sharedViewModel.image.postValue(it.OBITUARYPHOTO)
                                    sharedViewModel.image.value = it.OBITUARYPHOTO
                                }
                            }
                        }
                    } ?: run {
                        Log.e("Fetch Obituary", "Response body is null.")
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch Obituary", "Error: ${response.code} - ${response.message}")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
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

    private fun parseDate(dateStr: String, fieldName: String): Date? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return if (dateStr.isNotEmpty()) {
            try {
                val date = dateFormat.parse(dateStr)
                Log.d("EditObituaryStep3", "$fieldName parsed successfully: $date")
                date
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
        val filename = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, filename))

        // Convert 200dp and 230dp to pixels
        val widthPx = dpToPx(150f, requireContext())
        val heightPx = dpToPx(180f, requireContext())

        UCrop.of(uri, destinationUri)
            .withAspectRatio(150f, 180f) // Setting the aspect ratio to 150:180
            .withMaxResultSize(widthPx, heightPx) // Set maximum result size
            .start(requireContext(), this)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}