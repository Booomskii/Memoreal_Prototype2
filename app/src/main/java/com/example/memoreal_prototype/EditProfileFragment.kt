package com.example.memoreal_prototype

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.appcompat.widget.Toolbar
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.bumptech.glide.Glide
import com.yalantis.ucrop.UCrop
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

class EditProfileFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl
    private var imageUri: Uri? = null
    private var email = ""
    private var originalImageUri: String? = null

    private lateinit var userName: TextView
    private lateinit var userFName: EditText
    private lateinit var userMI: EditText
    private lateinit var userLName: EditText
    private lateinit var userContact: EditText
    private lateinit var userBDate: EditText
    private lateinit var userPhoto: ImageView

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
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                try {
                    Glide.with(requireContext())
                        .load(resultUri)
                        .placeholder(R.drawable.baseline_person_24)
                        .error(R.drawable.baseline_person_24)
                        .circleCrop()
                        .into(userPhoto)

                    // Update the stored image URI
                    imageUri = resultUri
                    Log.d("SignUpActivity2", "Updated image URI: ${resultUri.toString()}")
                } catch (e: Exception) {
                    Log.e("EditProfileFragment", "Error processing cropped image: ${e.message}")
                    Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Log.e("EditProfileFragment", "Crop error: ${cropError?.message}")
            Toast.makeText(requireContext(), "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val save = toolbar.findViewById<TextView>(R.id.save)

        userName = view.findViewById(R.id.etUsername)
        userFName = view.findViewById(R.id.etUserFName)
        userMI = view.findViewById(R.id.etUserMI)
        userLName = view.findViewById(R.id.etUserLName)
        userContact = view.findViewById(R.id.etUserContact)
        userBDate = view.findViewById(R.id.etUserBDate)
        userPhoto = view.findViewById(R.id.userPhoto)

        userPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        fetchUser()

        userBDate.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = userBDate.compoundDrawables[2]
                drawableEnd?.let {
                    if (event.rawX >= (userBDate.right - it.bounds.width())) {
                        val calendar = Calendar.getInstance()
                        val datePickerDialog = DatePickerDialog(
                            requireContext(),
                            { _, year, month, dayOfMonth ->
                                userBDate.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year))
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
                        .replace(R.id.frame_layout, ProfileFragment())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                        .commit()
                }
                .setNegativeButton("No", null)
                .show()
        }

        save.setOnClickListener {
            val image = imageUri?.toString() ?: originalImageUri ?: ""
            val formattedBDate = formatBDateForMSSQL(userBDate.text.toString()) ?: ""
            val user = com.example.memoreal_prototype.models.User(
                0, userFName.text.toString(), userLName.text.toString(), userMI.text.toString(),
                userName.text.toString(), userContact.text.toString(), email,
                formattedBDate, image, ""
            )

            editUser(user)
        }

        return view
    }

    private fun fetchUser() {
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "userSession",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val userId = sharedPreferences.getInt("userId", -1)
        val url = "$baseUrl" + "api/fetchUser/$userId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch User", "Failed to fetch user: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to fetch user", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        try {
                            // Parse the JSON response as an object
                            val jsonObject = JSONObject(responseBody)

                            val firstname = jsonObject.optString("FIRST_NAME", "")
                            val middleInitial = jsonObject.optString("MI", "")
                            val lastname = jsonObject.optString("LAST_NAME", "")

                            val username = jsonObject.getString("USERNAME")
                            val contact = jsonObject.optString("CONTACT_NUMBER", "N/A")
                            val birthDate = jsonObject.optString("BIRTHDATE", "N/A")
                            val picture = jsonObject.optString("PICTURE", "")
                            originalImageUri = picture
                            val useremail = jsonObject.optString("EMAIL", "")

                            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                            val date = originalFormat.parse(birthDate)
                            val desiredFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
                            val formattedDate = desiredFormat.format(date)

                            // Update the UI on the main thread
                            requireActivity().runOnUiThread {
                                userName.setText(username)
                                userFName.setText(firstname)
                                userMI.setText(middleInitial)
                                userLName.setText(lastname)
                                userContact.setText(contact)
                                userBDate.setText(formattedDate)
                                email = useremail
                                Glide.with(requireContext())
                                    .load(picture)
                                    .placeholder(R.drawable.baseline_person_24) // Set placeholder image
                                    .error(R.drawable.baseline_person_24) // Set error image if loading fails
                                    .circleCrop()
                                    .into(userPhoto)
                                // Optionally, load the user picture into userPhoto using a library like Glide or Picasso
                            }
                        } catch (e: Exception) {
                            Log.e("Fetch User", "JSON parsing error: ${e.message}")
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error parsing user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch User", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun editUser(user: com.example.memoreal_prototype.models.User) {
        val url = baseUrl + "api/updateUser2/${user.EMAIL}"

        val json = JSONObject().apply {
            put("USERNAME", user.USERNAME)
            put("FIRST_NAME", user.FIRST_NAME)
            put("LAST_NAME", user.LAST_NAME)
            put("MI", user.MI)
            put("USERNAME", user.USERNAME)
            put("CONTACT_NUMBER", user.CONTACT_NUMBER)
            put("BIRTHDATE", user.BIRTHDATE)
            put("PICTURE", user.PICTURE)
            put("EMAIL", user.EMAIL)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateUserDetails", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "User info updated successfully", Toast.LENGTH_LONG).show()
                        (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                            .replace(R.id.frame_layout, ProfileFragment())
                            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                            .commit()
                    }
                } else {
                    Log.e("UpdateUserDetails", "Error: ${response.code} - ${response.message}")

                    val errorBody = response.body?.string()
                    requireActivity().runOnUiThread {
                        if (!errorBody.isNullOrEmpty()) {
                            try {
                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown error")
                                Log.e("UpdateUserDetails", "Server returned error message: $errorMessage")
                                Toast.makeText(requireContext(), "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Log.e("UpdateUserDetails", "Could not parse error message: ${errorBody}")
                                Toast.makeText(requireContext(), "Update failed: Could not parse error message", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Log.e("UpdateUserDetails", "Update failed: Empty response from server")
                            Toast.makeText(requireContext(), "Update failed: Empty response from server", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    private fun startCrop(uri: Uri) {
        val filename = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, filename))

        val widthPx = dpToPx(150f, requireContext())
        val heightPx = dpToPx(180f, requireContext())

        UCrop.of(uri, destinationUri)
            .withAspectRatio(150f, 180f)
            .withMaxResultSize(widthPx, heightPx)
            .start(requireContext(), this)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatBDateForMSSQL(birthDate: String): String? {
        return try {
            // Input format expected to be "dd/MM/yyyy"
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(birthDate)

            // Output format for MSSQL expected to be "yyyy-MM-dd"
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.e("EditProfileFragment", "Date format conversion error: ${e.message}")
            null
        }
    }
}
