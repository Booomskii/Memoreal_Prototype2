package com.example.memoreal_prototype

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.OkHttpClient
import java.io.File

abstract class UserSession: AppCompatActivity() {
    protected lateinit var sharedPreferences: SharedPreferences

    companion object {
        val client = OkHttpClient()
        val baseUrl = "http://192.168.1.2:4848/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Initialize class-level sharedPreferences
        sharedPreferences = EncryptedSharedPreferences.create(
            this,
            "userSession",  // File name
            masterKey,      // Master key for encryption
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isGuestUser = sharedPreferences.getBoolean("isGuestUser", false)
        val username = sharedPreferences.getString("username", "")
        val accessToken = sharedPreferences.getString("accessToken", "")

        Log.d("UserSession", "isLoggedIn: $isLoggedIn")
        Log.d("UserSession", "isGuestUser: $isGuestUser")
        Log.d("UserSession", "username: $username")
        Log.d("UserSession", "accessToken: $accessToken")

        if (!isLoggedIn && !isGuestUser) {
            // If not logged in and not a guest user, navigate to the MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    fun logOut() {
        try {
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false)
            editor.putBoolean("isGuestUser", false)
            editor.putString("username", "Guest User")
            editor.putString("accessToken", "")
            editor.apply()
        } catch (e: SecurityException) {
            // Handle decryption failure by clearing the preferences
            Log.e("UserSession", "SecurityException: Could not decrypt key. Clearing SharedPreferences.")
            val prefsFile = File(applicationContext.filesDir, "shared_prefs/userSession.xml")
            if (prefsFile.exists()) {
                prefsFile.delete()
            }
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}