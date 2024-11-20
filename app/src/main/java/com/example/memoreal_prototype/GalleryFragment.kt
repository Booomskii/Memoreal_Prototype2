package com.example.memoreal_prototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.DialogFragment
import com.example.memoreal_prototype.models.GalleryMedia
import com.example.memoreal_prototype.models.Obituary
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

class GalleryFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private var fetchedObituary: Obituary? = null
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    private lateinit var photoContainerLayout: LinearLayout
    private lateinit var videoContainerLayout: LinearLayout
    private lateinit var aiVideoContainerLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        // Initialize container layouts
        photoContainerLayout = view.findViewById(R.id.photoContainerLayout)
        videoContainerLayout = view.findViewById(R.id.videoContainerLayout)
        aiVideoContainerLayout = view.findViewById(R.id.aiVideoContainerLayout)

        photoContainerLayout.orientation = LinearLayout.HORIZONTAL
        videoContainerLayout.orientation = LinearLayout.HORIZONTAL
        aiVideoContainerLayout.orientation = LinearLayout.HORIZONTAL

        sharedViewModel.obituaryId.observe(viewLifecycleOwner) { id ->
            if (id != null) {
                fetchObituaryById(id)
            }
        }

        return view
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
                        val obitCustId = obitCustIdArray.getInt(0)

                        fetchedObituary = Obituary(
                            OBITUARYID = jsonObject.getInt("OBITUARYID"),
                            USERID = jsonObject.getInt("USERID"),
                            GALLERYID = jsonObject.getInt("GALLERYID"),
                            OBITCUSTID = obitCustId,
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
                                // Update the UI with fetched data
                                fetchedObituary?.let {
                                    fetchGallery(it.GALLERYID)
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

    private fun fetchGallery(galleryId: Int) {
        val url = "$baseUrl" + "api/fetchGallery/$galleryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Gallery", "Failed to fetch gallery: ${e.message}")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to fetch gallery", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)

                        requireActivity().runOnUiThread {
                            // Clear container views to avoid duplicates
                            photoContainerLayout.removeAllViews()
                            videoContainerLayout.removeAllViews()
                            aiVideoContainerLayout.removeAllViews()

                            for (i in 0 until jsonArray.length()) {
                                val mediaJson = jsonArray.getJSONObject(i)
                                val galleryMedia = GalleryMedia(
                                    GALLERYMEDIAID = mediaJson.getInt("GALLERYMEDIAID"),
                                    GALLERYID = mediaJson.getInt("GALLERYID"),
                                    MEDIATYPE = mediaJson.getString("MEDIATYPE"),
                                    FILENAME = mediaJson.getString("FILENAME"),
                                    UPLOADDATE = mediaJson.getString("UPLOADDATE")
                                )

                                val filePath = galleryMedia.FILENAME

                                when (galleryMedia.MEDIATYPE) {
                                    "Image" -> {
                                        val bitmap = loadImageFromFilePath(filePath)
                                        val thumbnailBitmap = bitmap?.let { createThumbnail(it) }
                                        val imageView = ImageView(requireContext()).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                            thumbnailBitmap?.let { setImageBitmap(it) }
                                            setPadding(8, 8, 8, 8)
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                            setOnClickListener {
                                                showImageDialog(bitmap)
                                            }
                                        }
                                        photoContainerLayout.addView(imageView)
                                    }
                                    "Video" -> {
                                        // Placeholder for video
                                        val thumbnailView = ImageView(requireContext()).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                            setImageResource(R.drawable.baseline_video_file_24) // Placeholder image for video
                                            setPadding(8, 8, 8, 8)
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                        }
                                        videoContainerLayout.addView(thumbnailView)
                                    }
                                    "ai_video" -> {
                                        // Placeholder for AI video
                                        val aiThumbnailView = ImageView(requireContext()).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            )
                                            setImageResource(R.drawable.baseline_video_file_24) // Placeholder image for AI video
                                            setPadding(8, 8, 8, 8)
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                        }
                                        aiVideoContainerLayout.addView(aiThumbnailView)
                                    }
                                }
                            }
                        }
                    } ?: run {
                        Log.e("Fetch Gallery", "Response body is null.")
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch Gallery", "Error: ${response.code} - ${response.message}")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun loadImageFromFilePath(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else {
                Log.e("GalleryFragment", "File does not exist: $filePath")
                null
            }
        } catch (e: Exception) {
            Log.e("GalleryFragment", "Failed to load image from file path: ${e.message}")
            null
        }
    }

    private fun createThumbnail(bitmap: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, 250, 250, false)
    }

    private fun showImageDialog(bitmap: Bitmap?) {
        bitmap?.let {
            val dialogFragment = ImageDialogFragment.newInstance(it)
            dialogFragment.show(childFragmentManager, "ImageDialog")
        }
    }

    class ImageDialogFragment : DialogFragment() {

        companion object {
            private const val ARG_BITMAP = "bitmap"

            fun newInstance(bitmap: Bitmap): ImageDialogFragment {
                val fragment = ImageDialogFragment()
                val args = Bundle().apply {
                    putParcelable(ARG_BITMAP, bitmap)
                }
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.dialog_image, container, false)

            val imageView = view.findViewById<ImageView>(R.id.dialogImageView)
            val closeButton = view.findViewById<ImageButton>(R.id.closeButton)

            val bitmap = arguments?.getParcelable<Bitmap>(ARG_BITMAP)
            bitmap?.let {
                imageView.setImageBitmap(it)
            }

            // Handle close button click
            closeButton.setOnClickListener {
                dismiss() // This will close the dialog
            }

            return view
        }
    }
}
