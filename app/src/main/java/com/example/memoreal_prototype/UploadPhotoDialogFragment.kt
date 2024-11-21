package com.example.memoreal_prototype

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class UploadPhotoDialogFragment : DialogFragment() {

    private val PICK_IMAGE_REQUEST_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var btnClose: ImageButton
    private lateinit var btnGenerateVideo: Button
    private lateinit var btnSelectImage: Button
    private lateinit var imageView: ImageView
    private lateinit var etPrompt: EditText
    private lateinit var radioGroupVoice: RadioGroup

    // Initialize handler for delaying tasks
    private val handler = Handler(Looper.getMainLooper())

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()
    val client = UserSession.client
    val baseUrl = UserSession.baseUrl
    val imgurAccessToken = UserSession.imgurAccessToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_photo_dialog, container, false)

        // Initialize UI components
        progressBar = view.findViewById(R.id.progressBar)
        btnClose = view.findViewById(R.id.btnClose)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnGenerateVideo = view.findViewById(R.id.btnGenerateVideo)
        imageView = view.findViewById(R.id.imageView)
        etPrompt = view.findViewById(R.id.etPrompt)
        radioGroupVoice = view.findViewById(R.id.radioGroupVoice)

        btnClose.setOnClickListener {
            dismiss()  // Close the dialog and return to the parent fragment
        }

        btnSelectImage.setOnClickListener {
            openGallery()
        }

        btnGenerateVideo.setOnClickListener {
            if (imageUri != null) {
                val prompt = etPrompt.text.toString().trim()
                val selectedVoice = when (radioGroupVoice.checkedRadioButtonId) {
                    R.id.radioMale -> "en-US-ChristopherMultilingualNeural"
                    R.id.radioFemale -> "en-US-CoraMultilingualNeural"
                    else -> ""
                }

                if (prompt.isEmpty() || selectedVoice.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a prompt and select a voice", Toast.LENGTH_SHORT).show()
                } else {
                    val imageFile = File(imageUri?.path ?: "")
                    if (imageFile.exists()) {
                        // Show the progress bar while uploading image and generating video
                        progressBar.visibility = View.VISIBLE
                        uploadImageToImgurAuthenticated(imageFile, imgurAccessToken) { imageUrl ->
                            sendGenerateVideoRequest(prompt, selectedVoice, imageUrl)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Image file does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)  // Display selected image in ImageView

            // Save image to internal storage
            imageUri?.let {
                val savedFile = saveImageToInternalStorage(it)
                if (savedFile != null) {
                    btnGenerateVideo.isEnabled = true
                    imageUri = Uri.fromFile(savedFile) // Update imageUri to point to saved file
                } else {
                    Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): File? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageToImgurAuthenticated(imageFile: File, accessToken: String, callback: (String) -> Unit) {
        val apiUrl = "https://api.imgur.com/3/upload"
        Log.d("ImgurUpload", "Uploading image to $apiUrl")

        // Create RequestBody instance from the image file
        val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

        // Wrap RequestBody into MultipartBody.Part
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, requestBody)
            .build()

        // Create the Request
        val request = Request.Builder()
            .url(apiUrl)
            .post(multipartBody)
            .addHeader("Authorization", "Bearer $accessToken")  // Using Bearer token for authenticated upload
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("ImgurUpload", "Failed to upload image: ${e.message}")
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to upload image to Imgur", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()

                if (response.isSuccessful) {
                    responseString?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val success = jsonObject.getBoolean("success")
                            if (success) {
                                val imageUrl = jsonObject.getJSONObject("data").getString("link")
                                Log.d("ImgurUpload", "Imgur Image URL: $imageUrl")
                                requireActivity().runOnUiThread {
                                    Toast.makeText(requireContext(), "Image uploaded to Imgur successfully", Toast.LENGTH_SHORT).show()
                                    callback(imageUrl)
                                }
                            } else {
                                val message = jsonObject.getString("message")
                                Log.e("ImgurUpload", "Error: $message")
                                requireActivity().runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("ImgurUpload", "Failed to parse JSON: $it")
                            requireActivity().runOnUiThread {
                                progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), "Unexpected response format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("ImgurUpload", "Upload failed with response code: ${response.code}, response body: $responseString")
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Upload failed with response code: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun sendGenerateVideoRequest(prompt: String, voiceId: String, sourceUrl: String) {
        val json = JSONObject().apply {
            put("prompt", prompt)
            put("voiceId", voiceId)
            put("sourceUrl", sourceUrl)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body: RequestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(baseUrl + "api/generateVideo") // Replace with your Node.js server endpoint
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("VideoGeneration", "Failed to generate video: ${e.message}")
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to generate video", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val responseString = response.body?.string()
                        Log.d("VideoGeneration", "Video generation response: $responseString")
                        responseString?.let {
                            try {
                                val jsonObject = JSONObject(it)
                                val success = jsonObject.getBoolean("success")
                                if (success) {
                                    val data = jsonObject.getJSONObject("data")
                                    val videoId = data.getString("id")
                                    Log.d("VideoGeneration", "Video ID: $videoId")
                                    requireActivity().runOnUiThread {
                                        Toast.makeText(requireContext(), "Video generated successfully", Toast.LENGTH_SHORT).show()
                                        sharedViewModel.videoId.value = videoId
                                        // Dismiss the dialog with a slight delay
                                        progressBar.visibility = View.GONE
                                        handler.postDelayed({ dismiss() }, 8000) // 3-second
                                    // delay before closing
                                    }
                                } else {
                                    val message = jsonObject.getString("message")
                                    Log.e("VideoGeneration", "Error: $message")
                                    requireActivity().runOnUiThread {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: JSONException) {
                                Log.e("VideoGeneration", "Failed to parse JSON: $it")
                                requireActivity().runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(requireContext(), "Unexpected response format", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Log.e("VideoGeneration", "Failed to generate video with response code: ${response.code}")
                        requireActivity().runOnUiThread {
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed to generate video", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}
