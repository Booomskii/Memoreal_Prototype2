package com.example.memoreal_prototype

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.util.TypedValueCompat.dpToPx
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CreateObituaryStep8 : Fragment() {
    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private var obitCustId = 0
    private var familyId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step8, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val publishButton = view.findViewById<Button>(R.id.btnPublish)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val frameImageView = view.findViewById<ImageView>(R.id.frame_image)
        val flowerImageView = view.findViewById<ImageView>(R.id.imgFlower)
        val candleImageView = view.findViewById<ImageView>(R.id.imgCandle)
        val obituaryImage = view.findViewById<ImageView>(R.id.picture_image)
        var frameName = ""
        var flowerName = ""
        var candleName = ""
        val imageUriStr = arguments?.getString("image")
        val imageUri = Uri.parse(imageUriStr)

        val bgTheme = arguments?.getString("backgroundTheme")
        val picFrame = arguments?.getString("pictureFrame")
        val bgMusic = arguments?.getString("bgMusic")
        val vflower = arguments?.getString("virtualFlower")
        val vcandle = arguments?.getString("virtualCandle")

        val selectedPackage = arguments?.getString("selectedPackage") ?: "Default Package"
        val obituaryPhoto = arguments?.getString("image") ?: "default_image_path"
        val dateOfBirth = arguments?.getString("dateBirth") ?: "01-01-1900"
        val dateOfDeath = arguments?.getString("datePassing") ?: "01-01-1900"
        val obituaryName = arguments?.getString("fullName") ?: "Unknown Name"
        val biography = arguments?.getString("biography") ?: "No biography available."
        val obituaryText = arguments?.getString("obituaryText") ?: "No obituary text provided."
        val keyEvents = arguments?.getString("keyEvents") ?: "No key life events"
        val mediaList = arguments?.getStringArrayList("mediaList") ?: arrayListOf()
        val funDateTime = arguments?.getString("funeralDateTime") ?: "01-01-1900 00:00"
        val funLocation = arguments?.getString("funeralLocation") ?: "Unknown Location"
        val adtlInfo = arguments?.getString("funeralAdtlInfo") ?: "No additional information."
        val privacy = arguments?.getString("privacyType") ?: "Public"
        val enaGuestbook = arguments?.getBoolean("guestBookSwitchState") ?: "false"
        val favoriteQuote = arguments?.getString("favQuote") ?: "No favorite quote."

        /*val family = com.example.memoreal_prototype.models.Family(
            0, memberName!!, relationship!!
        )*/

        obituaryImage.setImageURI(imageUri)

        picFrame?.let {
            val frameParts = it.trim('(', ')').split(", ")
            if (frameParts.size == 2) {
                val frameId = frameParts[0]
                frameName = frameParts[1]
            }
        }

        vflower?.let {
            val flowerParts = it.trim('(', ')').split(", ")
            if (flowerParts.size == 2) {
                val flowerId = flowerParts[0]
                flowerName = flowerParts[1]
            }
        }

        vcandle?.let {
            val candleParts = it.trim('(', ')').split(", ")
            if (candleParts.size == 2) {
                val candleId = candleParts[0]
                candleName = candleParts[1]
            }
        }

        val obituary_cust = com.example.memoreal_prototype.models.Obituary_Customization(
            0, bgTheme!!, bgMusic!!, frameName, flowerName, candleName
        )

        val backgroundThemeMap = mapOf(
            "Pattern 1" to R.drawable.pattern1,
            "Pattern 2" to R.drawable.pattern2,
            "Pattern 3" to R.drawable.pattern3,
            "Artistic 1" to R.drawable.artistic1,
            "Artistic 2" to R.drawable.artistic2,
            "Artistic 3" to R.drawable.artistic3,
            "Heaven 1" to R.drawable.heaven1,
            "Heaven 2" to R.drawable.heaven2,
            "Heaven 3" to R.drawable.heaven3
        )

        val pictureFrameMap = mapOf(
            "Classic 1" to R.drawable.classic1,
            "Classic 2" to R.drawable.classic2,
            "Classic 3" to R.drawable.classic3,
            "Gold 1" to R.drawable.gold1,
            "Gold 2" to R.drawable.gold2,
            "Gold 3" to R.drawable.gold3,
            "Wood 1" to R.drawable.wood1,
            "Wood 2" to R.drawable.wood2,
            "Wood 3" to R.drawable.wood3
        )

        val flowerMap = mapOf(
            "Rose" to R.drawable.rose,
            "Lily" to R.drawable.lily,
            "Carnation" to R.drawable.carnation,
            "Chrysanthemum" to R.drawable.chrysanthemum,
            "Iris" to R.drawable.iris,
            "Orchid" to R.drawable.orchid
        )

        val candleMap = mapOf(
            "Candle 1" to R.drawable.candle1,
            "Candle 2" to R.drawable.candle2,
            "Candle 3" to R.drawable.candle3,
            "Candle 4" to R.drawable.candle4,
            "Candle 5" to R.drawable.candle5,
            "Candle 6" to R.drawable.candle6,
            "Candle 7" to R.drawable.candle7,
            "Candle 8" to R.drawable.candle8
        )

        frameName.let {
            val drawableResId = pictureFrameMap[it]
            if (drawableResId != null) {
                frameImageView.setImageResource(drawableResId)
            } else {
                frameImageView.setImageResource(R.drawable.classic1)
            }
        }

        flowerName.let {
            val drawableResId = flowerMap[it]
            if (drawableResId != null) {
                flowerImageView.setImageResource(drawableResId)
            } else {
                flowerImageView.setImageResource(R.drawable.default_flower_icon)
            }
        }

        candleName.let {
            val drawableResId = candleMap[it]
            if (drawableResId != null) {
                candleImageView.setImageResource(drawableResId)
            } else {
                candleImageView.setImageResource(R.drawable.default_candle_icon)
            }
        }

        /*loadImageFromSharedPreferences()*/

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        publishButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to publish this Obituary?")
                .setPositiveButton("Yes") { _, _ ->
                    val imageUriString = arguments?.getString("image")
                    if (imageUriString != null) {
                        val imageUri = Uri.parse(imageUriString)
                        val savedFileName = saveImageToInternalStorage(imageUri)
                        if (savedFileName != null) {
                            Log.d("SaveImage", "Image saved successfully: $savedFileName")
                        } else {
                            Log.e("SaveImage", "Failed to save the image.")
                        }
                    } else {
                        Log.e("SaveImage", "Image Uri is null.")
                    }
                    val mediaList = arguments?.getStringArrayList("mediaList")
                    mediaList?.forEach { mediaUriString ->
                        val mediaUri = Uri.parse(mediaUriString)
                        val savedFileName = saveImageToInternalStorage(mediaUri)
                        if (savedFileName != null) {
                            Log.d("SaveImage", "Image saved successfully: $savedFileName")
                        } else {
                            Log.e("SaveImage", "Failed to save the image: $mediaUriString")
                        }
                    }

                    registerObituaryCustomization(obituary_cust)
                    addEachFamilyMember()

                    Toast.makeText(requireContext(),"Obituary created successfully", Toast.LENGTH_SHORT).show()
                    (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, HomeFragment())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                        .addToBackStack("CreateObituaryStep8")
                        .commit()
                }
                .setNegativeButton("No", null)
                .show()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep7())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        return view
    }


    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            // Save the file name to SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("obituaryImage", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("savedImageFileName", fileName).apply()

            fileName // Return file name
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun registerObituaryCustomization(obituaryCust: com.example.memoreal_prototype.models.Obituary_Customization) {
        val url = baseUrl + "api/addObituaryCust"

        val json = JSONObject().apply {
            put("BGTHEME", obituaryCust.BGTHEME)
            put("PICFRAME", obituaryCust.PICFRAME)
            put("BGMUSIC", obituaryCust.BGMUSIC)
            put("VFLOWER", obituaryCust.VFLOWER)
            put("VCANDLE", obituaryCust.VCANDLE)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register Obituary Customization", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast
                        .LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val jsonResponse = JSONObject(it)
                        obitCustId = jsonResponse.getInt("obitCustId")
                        Log.d("OBITUARY CUSTOMIZATION ID:", obitCustId.toString())
                    }
                } else {
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        val jsonError = JSONObject(errorBody ?: "")
                        jsonError.getString("message")
                    } catch (e: Exception) {
                        "Registration failed: Unknown error"
                    }
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun addEachFamilyMember() {
        val memberName = arguments?.getStringArrayList("familyNames") ?: arrayListOf()
        val relationship = arguments?.getStringArrayList("familyRelationships") ?: arrayListOf()

        if (memberName.isEmpty() || relationship.isEmpty()) {
            Log.e("Bundle Error", "Both memberNames and relationships are null or empty. Skipping family member addition.")
            return
        }

        if (memberName.size == relationship.size) {
            // First, create a family entry
            val familyId = addFamily()

            // Now, add each family member with the new family ID
            for (i in memberName.indices) {
                val name = memberName[i]
                val relation = relationship[i]

                val familyMember = com.example.memoreal_prototype.models.FamilyMembers(
                    0, // This will be handled by the database
                    familyId,
                    name,
                    relation
                )
                registerFamilyMember(familyMember)
            }
        } else {
            Log.e("Bundle Error", "The familyNames and relationships lists have different sizes!")
        }
    }

    private fun addFamily(): Int {
        val url = baseUrl + "api/addFamily"
        val requestBody = JSONObject().apply {
        }.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Add Family", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    familyId = jsonResponse.getInt("FAMILYID")
                    Log.d("Add Family", "Family created successfully with ID: $familyId")
                } else {
                    Log.e("Add Family", "Failed to create family: ${response.message}")
                }
            }
        })

        return familyId
    }

    private fun registerFamilyMember(familyMember: com.example.memoreal_prototype.models.FamilyMembers) {
        val url = baseUrl + "api/addFamilyMember"

        val json = JSONObject().apply {
            put("FAMILYID", familyMember.FAMILYID)
            put("MEMBERNAME", familyMember.MEMBERNAME)
            put("RELATIONSHIP", familyMember.RELATIONSHIP)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register Family Member", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Register Family Member", "Family member registered successfully")
                } else {
                    Log.e("Register Family Member", "Failed to register family member: ${response.message}")
                }
            }
        })
    }


    private fun publishObituary(obituary: com.example.memoreal_prototype.models.Obituary) {
        val url = baseUrl + "api/addObituary"

        val json = JSONObject().apply {
            /*put("USERID", obituaryCust.USERID)*/
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register Obituary Customization", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast
                        .LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Show success message
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "User registered successfully", Toast
                            .LENGTH_LONG)
                            .show()
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
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

}