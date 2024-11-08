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
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.fragment.app.activityViewModels
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class CreateObituaryStep8 : Fragment() {

    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private var obitCustId = 0
    private var familyId = 0
    private var galleryId = 0
    private var userId = 0

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

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
        val imageUriStr = sharedViewModel.image.value
        val imageUri = Uri.parse(imageUriStr)

        val bgTheme = sharedViewModel.backgroundTheme.value
        val picFrame = sharedViewModel.pictureFrame.value
        val bgMusic = sharedViewModel.bgMusic.value
        val vflower = sharedViewModel.virtualFlower.value
        val vcandle = sharedViewModel.virtualCandle.value

        Log.d("Obituary", "Obituary Photo: ${sharedViewModel.image.value ?: "default_image_path"}")
        Log.d("Obituary", "Date of Birth: ${sharedViewModel.dateBirth.value?.let { formatDateToMSSQL(it) } ?: "1900-01-01"}")
        Log.d("Obituary", "Date of Death: ${sharedViewModel.datePassing.value?.let { formatDateToMSSQL(it) } ?: "1900-01-01"}")
        Log.d("Obituary", "Obituary Name: ${sharedViewModel.fullName.value ?: "Unknown Name"}")
        Log.d("Obituary", "Biography: ${sharedViewModel.biography.value ?: "No biography available."}")
        Log.d("Obituary", "Obituary Text: ${sharedViewModel.obituaryText.value ?: "No obituary text provided."}")
        Log.d("Obituary", "Key Events: ${sharedViewModel.keyEvents.value ?: "No key life events"}")
        Log.d("Obituary", "Funeral DateTime: ${sharedViewModel.funeralDateTime.value ?: "2024-01-01 00:00:00"}")
        Log.d("Obituary", "Funeral Location: ${sharedViewModel.funeralLocation.value ?: "Unknown Location"}")
        Log.d("Obituary", "Additional Info: ${sharedViewModel.funeralAdtlInfo.value ?: "No additional information."}")
        Log.d("Obituary", "Privacy: ${sharedViewModel.privacy.value ?: "Public"}")
        Log.d("Obituary", "Guestbook Enabled: ${sharedViewModel.guestBook.value ?: false}")
        Log.d("Obituary", "Favorite Quote: ${sharedViewModel.favQuote.value ?: "No favorite quote."}")


        obituaryImage.setImageURI(imageUri)

        picFrame?.let {
            val frameId = it.first
            frameName = it.second
        }

        vflower?.let {
            val flowerId = it.first
            flowerName = it.second
        }

        vcandle?.let {
            val candleId = it.first
            candleName = it.second
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
                    val imageUriString = sharedViewModel.image.value
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
                    val mediaList = sharedViewModel.mediaList.value
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
                    createGalleryAndMedia()
                    sharedViewModel.clearData()

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
        val memberName = sharedViewModel.familyNames.value ?: arrayListOf()
        val relationship = sharedViewModel.familyRelationships.value ?: arrayListOf()

        if (memberName.isEmpty() || relationship.isEmpty()) {
            Log.e("Bundle Error", "Both memberNames and relationships are null or empty. Skipping family member addition.")
            return
        }

        if (memberName.size == relationship.size) {
            // First, create a family entry
            addFamily { familyId ->
                if (familyId != 0) {
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
                    Log.e("Add Family", "Failed to create family, skipping member addition.")
                }
            }
        } else {
            Log.e("Bundle Error", "The familyNames and relationships lists have different sizes!")
        }
    }


    private fun addFamily(callback: (Int) -> Unit) {
        val url = baseUrl + "api/addFamily"
        val requestBody = JSONObject().toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Add Family", "Request failed: ${e.message}")
                callback(0) // Pass 0 to callback if the request fails
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    familyId = jsonResponse.getInt("FAMILYID")
                    Log.d("Add Family", "Family created successfully with ID: $familyId")
                    callback(familyId) // Pass the familyId to the callback
                } else {
                    Log.e("Add Family", "Failed to create family: ${response.message}")
                    callback(0) // Pass 0 if the family creation fails
                }
            }
        })
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

    private fun createGalleryAndMedia() {
        val mediaList = sharedViewModel.mediaList.value ?: arrayListOf()

        // Step 2: Create the gallery and media only if media exists
        if (mediaList.isNotEmpty()) {
            addGallery { createdGalleryId ->  // Pass a callback to handle the created gallery ID
                if (createdGalleryId != 0) { // Check if the gallery was created successfully
                    // Step 3: After the gallery is created, proceed to add the media
                    mediaList.forEach { mediaString ->
                        val mediaUri = Uri.parse(mediaString)
                        val mimeType = requireContext().contentResolver.getType(mediaUri)

                        mimeType?.let {
                            val fileType = when {
                                mimeType.startsWith("image/") -> "Image"
                                mimeType.startsWith("video/") -> "Video"
                                else -> ""
                            }

                            if (fileType.isNotEmpty()) {
                                val galleryMedia = com.example.memoreal_prototype.models.GalleryMedia(
                                    0,  // Gallery Media ID (auto-incremented)
                                    createdGalleryId,  // Use the newly created GALLERYID
                                    fileType,  // File type (Image or Video)
                                    mediaString,  // File name or URI
                                    ""  // This will be handled by the server (upload date)
                                )
                                registerMedia(galleryMedia)
                            } else {
                                Log.e("MediaType", "Unsupported media type: $mimeType")
                            }
                        } ?: Log.e("MediaType", "Unable to determine MIME type for: $mediaUri")
                    }
                    // Step 4: Publish the obituary after gallery and media are added
                    publishObituary()
                } else {
                    Log.e("Gallery Error", "Failed to create gallery. Skipping media addition.")
                }
            }
        } else {
            Log.e("Bundle Error", "Media List is null or empty. Skipping gallery media addition.")
        }
    }

    private fun addGallery(callback: (Int) -> Unit) {
        val url = baseUrl + "api/addGallery"
        val requestBody = JSONObject().toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Add Gallery", "Request failed: ${e.message}")
                callback(0) // Pass 0 to callback if the request fails
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    galleryId = jsonResponse.getInt("GALLERYID")
                    Log.d("Add Gallery", "Gallery created successfully with ID: $galleryId")
                    callback(galleryId)
                } else {
                    Log.e("Add Gallery", "Failed to create gallery: ${response.message}")
                    callback(0)
                }
            }
        })
    }

    private fun registerMedia(galleryMedia: com.example.memoreal_prototype.models.GalleryMedia) {
        val url = baseUrl + "api/addGalleryMedia"

        val json = JSONObject().apply {
            put("GALLERYID", galleryMedia.GALLERYID)
            put("MEDIATYPE", galleryMedia.MEDIATYPE)
            put("FILENAME", galleryMedia.FILENAME)
            put("UPLOADDATE", galleryMedia.UPLOADDATE)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register Gallery Media", "Request failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Register Gallery Media", "Gallery media registered successfully")
                } else {
                    Log.e("Register Gallery Media", "Failed to register gallery media: ${response
                        .message}")
                }
            }
        })
    }

    private fun getUserId(){
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Initialize EncryptedSharedPreferences
        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "userSession",  // File name
            masterKey,      // Master key for encryption
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        userId = sharedPreferences.getInt("userId", -1)
    }

    private fun publishObituary(){
        getUserId()
        val obituaryPhoto = sharedViewModel.image.value ?: "default_image_path"
        val dateOfBirth = sharedViewModel.dateBirth.value?.let { formatDateToMSSQL(it) } ?: "1900-01-01"
        val dateOfDeath = sharedViewModel.datePassing.value?.let { formatDateToMSSQL(it) } ?: "1900-01-01"
        val obituaryName = sharedViewModel.fullName.value ?: "Unknown Name"
        val biography = sharedViewModel.biography.value ?: "No biography available."
        val obituaryText = sharedViewModel.obituaryText.value ?: "No obituary text provided."
        val keyEvents = sharedViewModel.keyEvents.value ?: "No key life events"
        val funDateTime = sharedViewModel.funeralDateTime.value ?: "2024-01-01 00:00:00"
        val funLocation = sharedViewModel.funeralLocation.value ?: "Unknown Location"
        val adtlInfo = sharedViewModel.funeralAdtlInfo.value ?: "No additional information."
        val privacy = sharedViewModel.privacy.value ?: "Public"
        val enaGuestbook = sharedViewModel.guestBook.value ?: false
        val favoriteQuote = sharedViewModel.favQuote.value ?: "No favorite quote."
        /*val formattedFuneralDateTime = formatFuneralDateTime(funDateTime)*/
        val obituary = com.example.memoreal_prototype.models.Obituary(
            0,
            userId.toInt(),
            galleryId.toInt(),
            obitCustId.toInt(),
            familyId.toInt(),
            biography,
            obituaryName,
            obituaryPhoto,
            dateOfBirth,
            dateOfDeath,
            obituaryText,
            keyEvents,
            /*formattedFuneralDateTime,*/
            funDateTime,
            funLocation,
            adtlInfo,
            privacy,
            enaGuestbook,
            favoriteQuote,
            "",
            ""
        )

        registerObituary(obituary)
    }

    private fun registerObituary(obituary: com.example.memoreal_prototype.models.Obituary) {
        val url = baseUrl + "api/addObituary"

        val json = JSONObject().apply {
            put("OBITUARYID", obituary.OBITUARYID)
            put("USERID", obituary.USERID)
            put("GALLERYID", obituary.GALLERYID)
            put("OBITCUSTID", obituary.OBITCUSTID)
            put("FAMILYID", obituary.FAMILYID)
            put("BIOGRAPHY", obituary.BIOGRAPHY)
            put("OBITUARYNAME", obituary.OBITUARYNAME)
            put("OBITUARYPHOTO", obituary.OBITUARYPHOTO)
            put("DATEOFBIRTH", obituary.DATEOFBIRTH)
            put("DATEOFDEATH", obituary.DATEOFDEATH)
            put("OBITUARYTEXT", obituary.OBITUARYTEXT)
            put("KEYEVENTS", obituary.KEYEVENTS)
            put("FUNDATETIME", obituary.FUNDATETIME)
            put("FUNLOCATION", obituary.FUNLOCATION)
            put("ADTLINFO", obituary.ADTLINFO)
            put("PRIVACY", obituary.PRIVACY)
            put("ENAGUESTBOOK", obituary.ENAGUESTBOOK)
            put("FAVORITEQUOTE", obituary.FAVORITEQUOTE)
            put("CREATIONDATE", obituary.CREATIONDATE)
            put("LASTMODIFIED", obituary.LASTMODIFIED)
        }.toString()

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register Obituary", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast
                        .LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Obituary registered successfully", Toast
                            .LENGTH_LONG)
                            .show()
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

    fun formatFuneralDateTime(inputDateTime: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        return try {
            val date = inputFormat.parse(inputDateTime)
            if (date != null) {
                outputFormat.format(date)
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun formatDateToMSSQL(dateString: String): String {
        val originalFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = originalFormat.parse(dateString)
        return targetFormat.format(date)
    }
}