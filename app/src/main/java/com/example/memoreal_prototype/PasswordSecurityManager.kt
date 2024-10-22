package com.example.memoreal_prototype

import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import android.util.Base64
import java.security.SecureRandom
import kotlin.random.Random

object PasswordUtils {

    // Define PBKDF2 parameters
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256 // 256 bits

    // Generate a salt for password hashing
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }

    // Hash the password using PBKDF2
    fun encryptPassword(password: String, salt: String): String {
        val saltBytes = Base64.decode(salt, Base64.DEFAULT)
        val spec = PBEKeySpec(password.toCharArray(), saltBytes, ITERATION_COUNT, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }

    // Password verification
    fun verifyPassword(password: String, hashedPassword: String, salt: String): Boolean {
        val hash = encryptPassword(password, salt)
        return hash == hashedPassword
    }
}