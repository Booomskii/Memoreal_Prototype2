package com.example.memoreal_prototype

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yalantis.ucrop.UCrop
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SignUpActivity2 : AppCompatActivity() {

    private lateinit var uploadImg: ImageView
    private var imageUri: Uri? = null
    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = null  // Reset previous image URI
            startCrop(uri)    // Start the crop activity
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            if (resultUri != null) {
                uploadImg.setImageURI(null)  // Clear previous image first
                uploadImg.setImageURI(resultUri)  // Set the new cropped image
                imageUri = resultUri            // Update stored image URI
                Log.d("SignUpActivity2", "Updated image URI: ${resultUri.toString()}")
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            Toast.makeText(this, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)

        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        val email = intent.getStringExtra("email")
        /*val skip = findViewById<TextView>(R.id.textViewSkip)*/
        val cont = findViewById<Button>(R.id.btnContinue)
        val firstName = findViewById<EditText>(R.id.editTextFirstName)
        val lastName = findViewById<EditText>(R.id.editTextLastName)
        val middleInitial = findViewById<EditText>(R.id.editTextMI)
        val contactNum = findViewById<EditText>(R.id.editTextNumber)
        val birthDate = findViewById<EditText>(R.id.editTextDate)

        uploadImg = findViewById(R.id.imageViewUploadPic)
        uploadImg.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        birthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    birthDate.setText(String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear))
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        cont.setOnClickListener {
            val fname = firstName.text.toString()
            val lname = lastName.text.toString()
            val mi = middleInitial.text.toString()
            val contact = contactNum.text.toString()
            val bdate = birthDate.text.toString()
            val formattedBDate = formatBDateForMSSQL(bdate) ?: ""
            val image = imageUri?.toString() ?: ""

            if (inputValidator(fname, lname, mi, bdate, contact, image)) {
                val user = com.example.memoreal_prototype.models.User(
                    0, fname, lname, mi, username!!, contact, email!!,
                    formattedBDate, image, ""
                )

                registerUser2(user, password!!)
            }
        }

        /*skip.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Skip Personal Information")
                .setMessage("Are you sure you want to skip entering your Personal Information?")
                .setPositiveButton("Yes") { _, _ ->
                    loginSuccess()
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }*/

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inputValidator(fname: String?, lname: String?, mi: String?, bdate: String?,
                               contact: String?, image: String?): Boolean {
        return when {
            fname.isNullOrEmpty() -> {
                Toast.makeText(this, "Enter your First Name", Toast.LENGTH_SHORT).show()
                false
            }
            lname.isNullOrEmpty() -> {
                Toast.makeText(this, "Enter your Last Name", Toast.LENGTH_SHORT).show()
                false
            }
            mi.isNullOrEmpty() -> {
                Toast.makeText(this, "Enter your Middle Initial", Toast.LENGTH_SHORT).show()
                false
            }
            contact.isNullOrEmpty() -> {
                Toast.makeText(this, "Enter your Contact Number", Toast.LENGTH_SHORT).show()
                false
            }
            bdate.isNullOrEmpty() -> {
                Toast.makeText(this, "Enter your Birthdate", Toast.LENGTH_SHORT).show()
                false
            }
            imageUri == null -> {
                Toast.makeText(this, "Please upload an image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun loginSuccess() {
        val sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putBoolean("isGuestUser", false)
        editor.putString("username", intent.getStringExtra("username"))
        editor.apply()
    }

    private fun registerUser2(user: com.example.memoreal_prototype.models.User, password: String) {
        val username = intent.getStringExtra("username")
        val url = "$baseUrl"+"api/updateUser/$username"

        val json = JSONObject().apply {
            put("FIRST_NAME", user.FIRST_NAME)
            put("LAST_NAME", user.LAST_NAME)
            put("MI", user.MI)
            put("USERNAME", user.USERNAME)
            put("CONTACT_NUMBER", user.CONTACT_NUMBER)
            put("EMAIL", user.EMAIL)
            put("BIRTHDATE", user.BIRTHDATE)
            put("PICTURE", user.PICTURE)
            put("PASSWORD", password)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateUserDetails", "Request failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    imageUri?.let {
                        val savedImagePath = saveImageToInternalStorage(it)
                        if (savedImagePath != null) {
                            // If image saved successfully, you may want to store the path
                            Log.d("SignUpActivity2", "Image saved at: $savedImagePath")
                            // Optionally, save the image path in shared preferences or session data for future use
                            val sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("profileImagePath", savedImagePath)
                            editor.apply()
                        } else {
                            Log.e("SignUpActivity2", "Failed to save image to internal storage.")
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Image saving failed, please try again.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    loginSuccess() // Proceed to the next activity
                    val intent = Intent(applicationContext, HomePageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    // Log the response code for debugging purposes
                    Log.e("UpdateUserDetails", "Error: ${response.code} - ${response.message}")

                    val errorBody = response.body?.string()
                    runOnUiThread {
                        if (errorBody != null) {
                            try {
                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown error")
                                Toast.makeText(applicationContext, "Update failed: $errorMessage", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(applicationContext, "Update failed: Could not parse error message", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(applicationContext, "Update failed: Empty response from server", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
    }

    private fun startCrop(uri: Uri) {
        val filename = "cropped_image_${System.currentTimeMillis()}.jpg"
        val destinationUri = Uri.fromFile(File(applicationContext.cacheDir, filename))

        val widthPx = dpToPx(150f, this)
        val heightPx = dpToPx(180f, this)

        UCrop.of(uri, destinationUri)
            .withAspectRatio(150f, 180f)
            .withMaxResultSize(widthPx, heightPx)
            .start(this)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        val context = this@SignUpActivity2 // Replace this with your current context if different
        val fileName = "profile_picture_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream: OutputStream = FileOutputStream(file)

            val buffer = ByteArray(1024)
            var length: Int

            while (inputStream?.read(buffer).also { length = it ?: -1 } != -1) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream?.close()

            // Return the file path to save in the database or for later use
            return file.absolutePath

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("SaveImage", "Failed to save image: ${e.message}")
            return null
        }
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
            Log.e("SignUpActivity2", "Date format conversion error: ${e.message}")
            null
        }
    }

    override fun onBackPressed() {
        Toast.makeText(
            this, "Complete the sign up process first or click 'Skip'",
            Toast.LENGTH_SHORT
        ).show()
    }
}
