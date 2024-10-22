package com.example.memoreal_prototype

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class SignUpActivity : AppCompatActivity() {
    private lateinit var emailAdd: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var conpassword: EditText
    private lateinit var sf:SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val toLogin = findViewById<ImageView>(R.id.imgBack)
        val createAcc = findViewById<Button>(R.id.btnCreateAcc)
        emailAdd = findViewById(R.id.editTextEmailAddress)
        username = findViewById(R.id.editTextUsername)
        password = findViewById<EditText>(R.id.editTextPassword)
        conpassword = findViewById<EditText>(R.id.editTextConfirmPassword)
        val agree = findViewById<CheckBox>(R.id.checkBoxTerms)
        sf = getSharedPreferences("signup_sf",MODE_PRIVATE)
        editor = sf.edit()
        toLogin.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        createAcc.setOnClickListener {
            val uname = username.text.toString()
            val pword = password.text.toString()
            val email = emailAdd.text.toString()
            val conpword = conpassword.text.toString()
            /*
            concise alternative this one
            viewModel.insertUser(
                User(
                    0,
                    username.text.toString(),
                    password.text.toString(),
                    emailAdd.text.toString(),
                )
            )*/
            if (inputValidator(email, uname, pword, conpword, agree)) {
                val user = com.example.memoreal_prototype.models.User(
                    0, null, null, null, uname, null, email,
                    null, null, "" // Leave hashedPassword empty since it's handled on the server
                )

                checkUserAvailability(uname, email) { isAvailable ->
                    if (!isAvailable) {
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Username or Email already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        registerUser(user, pword)
                        clearInput()
                        val intent = Intent(applicationContext, SignUpActivity2::class.java)
                        intent.putExtra("username", uname)
                        intent.putExtra("email", email)
                        intent.putExtra("password", pword)
                        startActivity(intent)
                    }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun inputValidator(emailAd:String?, username:String?, password:String?,
                               conpassword:String?, agree: CheckBox): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        return when {
            emailAd.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Enter your Email Address",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            !emailAd.matches(emailRegex) -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Enter a valid Email Address with '@' and domain name",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            username.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Enter your Username",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            password.isNullOrEmpty() -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Enter your Password",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            conpassword.isNullOrEmpty() || conpassword != password -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            (!passwordValidator(password)) -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Password must be at least 8 characters long, contains one " +
                            "uppercase letter, one " +
                            "number, and one special character!",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            !agree.isChecked -> {
                Toast.makeText(
                    this@SignUpActivity,
                    "Please agree to the Terms and Conditions",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun passwordValidator(password:String): Boolean{
        val minLength = password.length >= 8
        val hasUpperCase = password.any {it.isUpperCase()}
        val hasNumber = password.any {it.isDigit()}
        val hasSpecChar = password.any {it.isSpecialChar()}
        return minLength && hasUpperCase && hasNumber && hasSpecChar
    }

    private fun Char.isSpecialChar(): Boolean {
        return !this.isLetterOrDigit() && !this.isWhitespace()
    }

    private fun clearInput(){
        val textFields = listOf(emailAdd, username, password, conpassword)
        textFields.forEach { it.text.clear() }
    }

    private fun checkUserAvailability(username: String, email: String, callback: (Boolean) -> Unit) {
        val url = baseUrl+"api/checkUser?USERNAME=$username&EMAIL=$email"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("CheckUserAvailability", "Response Code: ${response.code}")
                Log.d("CheckUserAvailability", "Response Body: $responseBody")
                if (response.isSuccessful) {
                } else if (response.code == 409) {
                    callback(false)
                } else {
                    callback(false)
                }
            }
        })
    }

    private fun registerUser(user: com.example.memoreal_prototype.models.User, password: String) {
        val url = baseUrl + "api/addUser"

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
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RegisterUser", "Request failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Show success message
                    runOnUiThread {
                        Toast.makeText(applicationContext, "User registered successfully", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Parse the response to get the error message
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        // Try to extract the "message" from the JSON response
                        val jsonError = JSONObject(errorBody ?: "")
                        jsonError.getString("message")
                    } catch (e: Exception) {
                        "Registration failed: Unknown error"
                    }

                    // Show the error message as a toast
                    runOnUiThread {
                        Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    override fun onPause(){
        super.onPause()
        val email = emailAdd.text.toString()
        val uname = username.text.toString()
        editor.apply{
            putString("sf_email", email)
            putString("sf_uname", uname)
            commit()
        }
    }

    override fun onResume(){
        super.onResume()
        val email = sf.getString("sf_email", null)
        val uname = sf.getString("sf_uname", null)
        emailAdd.setText(email)
        username.setText(uname)
    }
}