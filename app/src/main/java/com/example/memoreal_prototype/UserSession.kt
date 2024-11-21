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
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

abstract class UserSession: AppCompatActivity() {
    protected lateinit var sharedPreferences: SharedPreferences

    companion object {
        val client = OkHttpClient()
        var baseUrl = "http://192.168.1.9:4848/"
        /*var baseUrl = "http://192.168.120.252:4848/"*/
        val d_idUrl = "https://api.d-id.com/talks"
        val apiKey = "Ym9vbXNraWkxcGVnQGdtYWlsLmNvbQ:xGOxNGNMuqiheQse3iC4B"
        val imgurClientId = "7699629ad3600c5"
        val imgurAccessToken = "e9bf673e90400cbfe7066a282f1f4fa166cc1e06"
        val imgurRefreshToken = "67fa196d39b08dab558bc8e77efcb636758cd46f"
        val authorization = "Basic $apiKey"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*val ipv4Address = getDeviceLocalIpAddress()
        baseUrl = "http://$ipv4Address:4848/"*/

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

    /*private fun getDeviceLocalIpAddress(): String {
        try {
            val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val ipAddress = address.hostAddress
                        if (ipAddress.indexOf(':') == -1) { // Filter out IPv6 addresses
                            return ipAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("UserSession", "Failed to get IP address: ${ex.message}")
        }
        return "192.168.1.2" // Fallback to a default IP if none is found
    }*/

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