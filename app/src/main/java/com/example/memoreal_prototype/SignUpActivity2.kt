package com.example.memoreal_prototype

import android.app.DatePickerDialog
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar


class SignUpActivity2 : AppCompatActivity() {

    private lateinit var uploadImg: ImageView
    private lateinit var image: String

    private var imageUri: Uri? = null
    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? ->
        if (uri != null) {
            uploadImg.setImageURI(uri)
            imageUri = uri
            image = imageUri.toString()
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)

        val username = intent.getStringExtra("username")
        val password = intent.getStringExtra("password")
        val email = intent.getStringExtra("email")
        val skip = findViewById<TextView>(R.id.textViewSkip)
        val cont = findViewById<Button>(R.id.btnContinue)
        val firstName = findViewById<EditText>(R.id.editTextFirstName)
        val lastName = findViewById<EditText>(R.id.editTextLastName)
        val middleInitial = findViewById<EditText>(R.id.editTextMI)
        val contactNum = findViewById<EditText>(R.id.editTextNumber)
        val birthDate = findViewById<EditText>(R.id.editTextDate)
        /*val genderSpinner = findViewById<Spinner>(R.id.spinnerGender)
        val genderItems = listOf("Select Gender", "Male", "Female", "Transgender", "Non-binary", "Genderfluid", "Cisgender")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderItems)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter*/

        /*genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Select Gender") {
                    Toast.makeText(this@SignUpActivity2, "Select Gender", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle selected gender
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(this@SignUpActivity2, "Select Gender", Toast.LENGTH_SHORT).show()
            }
        }*/

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
                    // Update the editText with the selected date
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
            val image = imageUri?.toString() ?: ""

            // Validate inputs
            if (inputValidator(fname, lname, mi, bdate, contact, image)) {
                // Create a user object without the password
                val user = com.example.memoreal_prototype.models.User(
                    0, fname, lname, mi, username!!, contact, email!!,
                    bdate, image, ""  // No hashed password needed here
                )

                // Call the function to update user details, passing the plain password
                registerUser2(user, password!!) // Pass the plain password
                loginSuccess() // Proceed to the next activity
                val intent = Intent(applicationContext, HomePageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        skip.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to skip entering your Personal Information?")
                .setPositiveButton("Yes") { _, _ ->
                    loginSuccess()
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inputValidator(fname:String?, lname:String?, mi:String?, bdate:String?,
                               contact:String?, image:String?): Boolean {
        return when {
            fname.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity2,
                    "Enter your First Name",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            lname.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity2,
                    "Enter your Last Name",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            mi.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity2,
                    "Enter your Middle Initial",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            contact.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity2,
                    "Enter your Contact Number",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            bdate.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity2,
                    "Enter your Birthdate",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            imageUri == null -> {
                Toast.makeText(
                    this@SignUpActivity2,
                    "Please upload an image",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun loginSuccess(){
        val sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putBoolean("isGuestUser", false)
        editor.putString("username", intent.getStringExtra("username"))
        editor.apply()
    }

    private fun registerUser2(user: com.example.memoreal_prototype.models.User, password: String) {
        val username = intent.getStringExtra("username")
        val url = baseUrl + "api/updateUser/$username"

        // Create a JSON object to send to the server
        val json = JSONObject().apply {
            put("FIRST_NAME", user.FIRST_NAME)
            put("LAST_NAME", user.LAST_NAME)
            put("MI", user.MI)
            put("USERNAME", user.USERNAME)
            put("CONTACT_NUMBER", user.CONTACT_NUMBER)
            put("EMAIL", user.EMAIL)
            put("BIRTHDATE", user.BIRTHDATE)
            put("PICTURE", user.PICTURE)
            put("PASSWORD", password)  // Include the plain text password
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
                    // Show success message
                    runOnUiThread {
                        Toast.makeText(applicationContext, "User info updated successfully", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Parse the response to get the error message
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        // Try to extract the "message" from the JSON response
                        val jsonError = JSONObject(errorBody ?: "")
                        jsonError.getString("message")
                    } catch (e: Exception) {
                        "Update failed: Unknown error"
                    }

                    // Show the error message as a toast
                    runOnUiThread {
                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        Toast.makeText(
            this@SignUpActivity2,
            "Complete the sign up process first or click 'Skip'",
            Toast.LENGTH_SHORT
        ).show()
    }
}
