package com.example.memoreal_prototype

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.memoreal_prototype.models.Obituary
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class EditObituaryStep8 : Fragment() {

    val client = UserSession.client
    val baseUrl = UserSession.baseUrl

    private var obitCustId = 0
    private var familyId = 0
    private var galleryId = 0
    private var userId = 0

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()
    private val sharedViewModel2: Step4SharedViewModel by activityViewModels()
    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = false

    private var fetchedObituary: Obituary? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_obituary_step8, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val publishButton = view.findViewById<Button>(R.id.btnPublish)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val flowerImageView = view.findViewById<ImageView>(R.id.imgFlower)
        val candleImageView = view.findViewById<ImageView>(R.id.imgCandle)
        val obituaryImage = view.findViewById<ImageView>(R.id.obituary_image)
        val imageUriStr = sharedViewModel.image.value
        var imageUri = Uri.parse(imageUriStr)
        val playPauseButton = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val stopButton = view.findViewById<ImageButton>(R.id.btnStop)
        val musicLabel = view.findViewById<TextView>(R.id.musicName)

        val obituaryId = sharedViewModel.obituaryId.value

        if (obituaryId != null) {
            Log.d("EditObituaryStep6", "Fetching obituary with ID: $obituaryId")
            /*fetchObituaryById(obituaryId)*/
        } else {
            Log.d("EditObituaryStep6", "No obituary ID found in sharedViewModel")
        }

        // Load the image using Glide, which also gives you scaling options
        sharedViewModel.image.observe(viewLifecycleOwner) { imageUriString ->
            imageUriString?.let {
                val imageUri = Uri.parse(it)
                requireActivity().runOnUiThread {
                    obituaryImage.setImageURI(imageUri)
                }
            }
        }

        val bgTheme = sharedViewModel.backgroundTheme.value
        val picFrame = sharedViewModel.pictureFrame.value
        val bgMusic = sharedViewModel.bgMusic.value
        val vflower = sharedViewModel.virtualFlower.value
        val vcandle = sharedViewModel.virtualCandle.value

        sharedViewModel.bgMusic.observe(viewLifecycleOwner) { bgMusic ->
            musicLabel.setText(bgMusic)
        }

        var frameName = ""
        var flowerName = ""
        var candleName = ""

        Log.d("Obituary", "Obituary Photo: ${sharedViewModel.image.value ?: "default_image_path"}")
        Log.d("Obituary", "Date of Birth: ${sharedViewModel.dateBirth.value ?: "1900-01-01"}")
        Log.d("Obituary", "Date of Death: ${sharedViewModel.datePassing.value ?: "1900-01-01"}")
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
            0, bgTheme!!, frameName, bgMusic!!, flowerName, candleName
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

        sharedViewModel.bgMusic.observe(viewLifecycleOwner) { musicName ->
            // Update the MediaPlayer with the new music
            setupMediaPlayer()
            if (isPlaying) {
                startMusic()
                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24) // Set to pause icon
            }
        }

        setupMediaPlayer()
        startMusic()
        playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)

        playPauseButton.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
                playPauseButton.setImageResource(R.drawable.baseline_play_circle_24) // Set to play icon
            } else {
                startMusic()
                playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24) // Set to pause icon
            }
            isPlaying = !isPlaying // Toggle the isPlaying state
        }

        stopButton.setOnClickListener {
            stopMusic()
            playPauseButton.setImageResource(R.drawable.baseline_play_circle_24) // Set back to play icon
        }

        sharedViewModel.backgroundTheme.observe(viewLifecycleOwner) { bgTheme ->
            val backgroundResource = backgroundThemeMap[bgTheme]

            // Set the background only if a valid resource is found
            backgroundResource?.let {
                // Apply background to the main layout (e.g., NestedScrollView or ConstraintLayout)
                val mainLayout = view.findViewById<NestedScrollView>(R.id.nestedScrollView8)
                mainLayout.setBackgroundResource(it)
            }
        }

        setFrameForeground(frameName, obituaryImage, pictureFrameMap, R.drawable.classic1)
        loadImage(flowerName, flowerImageView, flowerMap, R.drawable.default_flower_icon)
        loadImage(candleName, candleImageView, candleMap, R.drawable.default_candle_icon)

        backButton.setOnClickListener {
            stopMusic()
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        publishButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Publish Obituary")
                .setMessage("Are you sure you want to publish this Obituary?")
                .setPositiveButton("Yes") { _, _ ->
                    // Create a list to store updated file paths
                    val updatedMediaList = mutableListOf<String>()

                    // Save the main image (if it exists)
                    val imageUriString = sharedViewModel.image.value
                    if (imageUriString != null) {
                        saveObituaryWithImage()
                    } else {
                        Log.e("SaveMedia", "Image Uri is null.")
                    }

                    // Save additional media files (images and videos)
                    val mediaList = sharedViewModel.mediaList.value
                    mediaList?.forEach { mediaUriString ->
                        val mediaUri = Uri.parse(mediaUriString)
                        // Determine if media is a video based on the URI's MIME type
                        val mimeType = requireContext().contentResolver.getType(mediaUri)
                        val isVideo = mimeType?.startsWith("video") == true

                        val savedFileName = saveMediaToInternalStorage(mediaUri, isVideo)
                        if (savedFileName != null) {
                            Log.d("SaveMedia", "Media saved successfully: $savedFileName")
                            // Add the saved file path to the updated list
                            updatedMediaList.add(savedFileName)
                        } else {
                            Log.e("SaveMedia", "Failed to save the media: $mediaUriString")
                        }
                    }

                    // Convert MutableList to ArrayList and update the sharedViewModel mediaList with the new internal storage paths
                    val aiVideoUrls = sharedViewModel.aiVideoUrl.value ?: ArrayList()
                    updatedMediaList.addAll(aiVideoUrls)

                    // Update sharedViewModel mediaList
                    sharedViewModel.mediaList.value = ArrayList(updatedMediaList)

                    // Proceed with the rest of the operations
                    updateObituaryCustomization(obituary_cust)
                    addEachFamilyMember()
                    createGalleryAndMedia()
                    stopMusic()

                    Toast.makeText(requireContext(), "Obituary created successfully", Toast.LENGTH_SHORT).show()
                    (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, HomeFragment())
                        .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_out_left,
                            R.anim.slide_out_right
                        )
                        .addToBackStack("EditObituaryStep8")
                        .commit()
                }
                .setNegativeButton("No", null)
                .show()
        }

        prevButton.setOnClickListener {
            stopMusic()
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep7())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        return view
    }

    private fun saveObituaryWithImage() {
        val imageUriString = sharedViewModel.image.value
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            val savedImagePath = saveImageToInternalStorage(imageUri)
            if (savedImagePath != null) {
                // Proceed to save the rest of the obituary data, including the saved image path
                Log.d("Step8Fragment", "Image saved to internal storage at: $savedImagePath")
                /*Toast.makeText(requireContext(), "Obituary saved successfully", Toast.LENGTH_SHORT).show()*/
            } else {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle the case where no image is available
            Log.d("Step8Fragment", "No image available to save")
            Toast.makeText(requireContext(), "Please upload an image before saving", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().filesDir, "saved_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Log.d("EditObituaryStep8", "Image saved to internal storage: ${file.absolutePath}")
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saving image to internal storage", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun loadImage(resourceName: String?, imageView: ImageView, resourceMap: Map<String, Int>, defaultResource: Int) {
        // Set the actual image resource (e.g., the photo inside the frame)
        val resourceId = resourceMap[resourceName] ?: defaultResource
        imageView.setImageResource(resourceId)
    }

    // Additional method to change the frame foreground programmatically
    private fun setFrameForeground(resourceName: String?, imageView: ImageView, frameMap: Map<String, Int>, defaultFrame: Int) {
        val frameResourceId = frameMap[resourceName] ?: defaultFrame
        imageView.foreground = resources.getDrawable(frameResourceId, requireContext().theme)
    }

    private fun saveMediaToInternalStorage(uri: Uri, isVideo: Boolean): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)

            val extension = if (isVideo) "mp4" else "jpg"
            val mediaType = if (isVideo) "videos" else "images"
            val fileName = "media_${System.currentTimeMillis()}.$extension"

            // Create a directory for media (either images or videos)
            val mediaDir = requireContext().getDir(mediaType, Context.MODE_PRIVATE)
            val file = File(mediaDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)

            inputStream?.close()
            outputStream.close()

            // Return the file path for direct access
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save media", Toast.LENGTH_SHORT).show()
            null
        }
    }


    private fun updateObituaryCustomization(obituaryCust: com.example.memoreal_prototype.models
        .Obituary_Customization) {
        val url = baseUrl + "api/updateObitCust/${obitCustId}"

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
            .put(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Update Obituary Customization", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast
                        .LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                } else {
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        val jsonError = JSONObject(errorBody ?: "")
                        jsonError.getString("message")
                    } catch (e: Exception) {
                        "Update failed: Unknown error"
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

    private fun getMimeTypeFromFilePath(filePath: String): String? {
        val extension = filePath.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun createGalleryAndMedia() {
        val mediaList = sharedViewModel.mediaList.value ?: arrayListOf()

        if (mediaList.isNotEmpty()) {
            if (galleryId != 0) {
                mediaList.forEach { savedFilePath ->
                    val mediaFile = File(savedFilePath)
                    val fileUri = Uri.fromFile(mediaFile)

                    var mimeType = requireContext().contentResolver.getType(fileUri)
                    if (mimeType == null) {
                        mimeType = getMimeTypeFromFilePath(savedFilePath)
                    }

                    mimeType?.let {
                        val fileType = when {
                            savedFilePath.contains("ai_video") -> "AI Video" // Set media type as AI Video if it matches the file name pattern
                            mimeType.startsWith("image/") -> "Image"
                            mimeType.startsWith("video/") -> "Video"
                            else -> ""
                        }

                        if (fileType.isNotEmpty()) {
                            val galleryMedia = com.example.memoreal_prototype.models.GalleryMedia(
                                0,
                                galleryId,
                                fileType,
                                savedFilePath,
                                ""
                            )
                            registerMedia(galleryMedia)
                        } else {
                            Log.e("MediaType", "Unsupported media type: $mimeType")
                        }
                    } ?: Log.e("MediaType", "Unable to determine MIME type for: $savedFilePath")
                }
                publishObituary {
                    sharedViewModel.clearData()
                    sharedViewModel2.clearMedia()
                }
            } else {
                Log.e("Gallery Error", "Failed to create gallery. Skipping media addition.")
            }
        }
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

    private fun publishObituary(onSuccess: () -> Unit) {
        Log.d("PublishObituary", "Starting publishObituary process")
        getUserId()

        // Set up and create the obituary
        val obituaryPhoto = sharedViewModel.image.value ?: "default_image_path"
        val dateOfBirth = sharedViewModel.dateBirth.value ?: "1900-01-01"
        val dateOfDeath = sharedViewModel.datePassing.value ?: "1900-01-01"
        val obituaryName = sharedViewModel.fullName.value ?: "Unknown Name"
        val biography = sharedViewModel.biography.value ?: "No biography available."
        val obituaryText = sharedViewModel.obituaryText.value ?: "No obituary text provided."
        val keyEvents = sharedViewModel.keyEvents.value ?: "No key life events"

        val inputDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val funDateTimeFormatted = try {
            val parsedDate = inputDateFormat.parse(sharedViewModel.funeralDateTime.value ?: "01/01/1900 00:00")
            outputDateFormat.format(parsedDate!!)
        } catch (e: ParseException) {
            Log.e("PublishObituary", "Error parsing funeralDateTime: ${e.message}")
            "1900-01-01 00:00:00" // Default date in case of parsing failure
        }

        val funLocation = sharedViewModel.funeralLocation.value ?: "Unknown Location"
        val adtlInfo = sharedViewModel.funeralAdtlInfo.value ?: "No additional information."
        val privacy = sharedViewModel.privacy.value ?: "Public"
        val enaGuestbook = sharedViewModel.guestBook.value ?: false
        val favoriteQuote = sharedViewModel.favQuote.value ?: "No favorite quote."

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
            funDateTimeFormatted,
            funLocation,
            adtlInfo,
            privacy,
            enaGuestbook,
            favoriteQuote,
            "",
            ""
        )

        Log.d("PublishObituary", "Obituary object created: $obituary")

        registerObituary(obituary)
        Log.d("PublishObituary", "Obituary registration initiated")
        onSuccess()
    }

    private fun registerObituary(obituary: com.example.memoreal_prototype.models.Obituary) {
        val url = baseUrl + "api/addObituary"

        Log.d("RegisterObituary", "Creating JSON for request")
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

        Log.d("RegisterObituary", "JSON created: $json")

        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        Log.d("RegisterObituary", "Sending request to $url")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("RegisterObituary", "Request failed: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("RegisterObituary", "Obituary registered successfully")
                } else {
                    val errorBody = response.body?.string()
                    val errorMessage = try {
                        val jsonError = JSONObject(errorBody ?: "")
                        jsonError.getString("message")
                    } catch (e: Exception) {
                        "Registration failed: Unknown error"
                    }
                    Log.e("RegisterObituary", "Request failed: $errorMessage")

                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun deleteGalleryMedia(galleryId: Int) {
        val url = "$baseUrl"+"api/deleteGalleryMedia/$galleryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .method("DELETE", null)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Delete Gallery Media", "Failed to delete gallery media: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to delete gallery media", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                } else {
                    Log.e("Delete Gallery Media", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
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
                        val obitCustumizationId = obitCustIdArray.getInt(0)

                        fetchedObituary = Obituary(
                            OBITUARYID = jsonObject.getInt("OBITUARYID"),
                            USERID = jsonObject.getInt("USERID"),
                            GALLERYID = jsonObject.getInt("GALLERYID"),
                            OBITCUSTID = obitCustumizationId,
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
                                    galleryId = it.GALLERYID
                                    familyId = it.FAMILYID
                                    obitCustId = it.OBITCUSTID
                                    userId = it.USERID
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

    private fun setupMediaPlayer() {
        // Release any existing media player instance to avoid state issues
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        val musicMap = mapOf(
            "Amazing Grace Instrumental" to R.raw.amazing_grace_instrumental,
            "Emotional Soaking Prayer" to R.raw.christian_instrumental_piano_worship_calm_emotional_soaking_prayer,
            "Living on the Prayer" to R.raw.living_on_the_prayer,
            "Mindfulness Meditation" to R.raw.mindfulness_meditation,
            "Peaceful Prayer Ambience" to R.raw.peaceful_prayer_meditation_piano_ambient_music,
            "Silent Prayer" to R.raw.silent_prayer_instrumental,
        )
        val selectedMusic = sharedViewModel.bgMusic.value
        val musicResId = musicMap[selectedMusic] ?: R.raw.amazing_grace_instrumental // Replace with default if not found

        mediaPlayer = MediaPlayer.create(requireContext(), musicResId)
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            view?.findViewById<ImageButton>(R.id.btnPlayPause)?.setImageResource(R.drawable.baseline_play_circle_24)
        }
    }


    private fun startMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            isPlaying = true
        }
    }

    private fun pauseMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
        }
    }

    // Function to stop playing the music
    private fun stopMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            isPlaying = false
            mediaPlayer.prepare() // Prepare the player again to allow for replay
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release MediaPlayer resources when the fragment is destroyed
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}