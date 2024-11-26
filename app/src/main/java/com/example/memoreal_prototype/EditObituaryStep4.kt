package com.example.memoreal_prototype

import MediaAdapter2
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class EditObituaryStep4 : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()
    private val sharedViewModel2: Step4SharedViewModel2 by activityViewModels()
    private lateinit var mediaAdapter: MediaAdapter2
    private lateinit var recyclerView: RecyclerView
    private lateinit var obitTextET: EditText
    private lateinit var keyEventsET: EditText

    private var fetchedObituary: Obituary? = null
    private var galleryId = 0
    private var familyId = 0

    private val mediaThumbnails: MutableMap<Uri, Bitmap> = mutableMapOf()

    // Launcher for picking multiple media files
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            it.forEach { uri ->
                sharedViewModel2.addMedia(uri)
                Log.d("Media Added", uri.toString()) // Log each added media URI
            }
            mediaAdapter.notifyDataSetChanged() // Refresh adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_obituary_step4, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val addFamily = view.findViewById<Button>(R.id.btnAddFamily)
        obitTextET = view.findViewById(R.id.etObituaryText)
        keyEventsET = view.findViewById(R.id.etKeyEvents)
        val obituaryId = sharedViewModel.obituaryId.value
        Log.d("Image from Step 3:", sharedViewModel.image.value.toString())

        // Initialize RecyclerView and MediaAdapter first
        recyclerView = view.findViewById(R.id.recyclerViewMedia)
        mediaAdapter = MediaAdapter2(
            context = requireContext(),
            fragmentManager = childFragmentManager,
            getMediaList = { sharedViewModel2.mediaList.value ?: emptyList() },
            mediaThumbnails = mediaThumbnails,
            onDeleteClick = { uri -> deleteMedia(uri) },
            onMediaClick = { uri -> viewMedia(uri) }
        )
        recyclerView.adapter = mediaAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(3, 24, true) // Increase spacing from 16dp to 24dp
        )

        // Fetch obituary data only if obituaryId changes or not fetched before
        if (obituaryId != null && obituaryId != sharedViewModel2.currentObituaryId) {
            sharedViewModel2.currentObituaryId = obituaryId // Set current obituaryId
            sharedViewModel2.setGalleryFetched(false) // Reset galleryFetched flag
            fetchObituaryById(obituaryId)
        } else if (sharedViewModel2.isGalleryFetched()) {
            generateThumbnailsForMedia() // Generate thumbnails for already fetched media
        }

        // Observing obituary text and key events
        sharedViewModel.obituaryText.observe(viewLifecycleOwner) { obituaryText ->
            obitTextET.setText(obituaryText)
        }

        sharedViewModel.keyEvents.observe(viewLifecycleOwner) { keyEvents ->
            keyEventsET.setText(keyEvents)
        }

        // Back button
        backButton.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Return")
                .setMessage("Are you sure you want to go back? Changes made will not be saved!")
                .setPositiveButton("Yes") { _, _ ->
                    (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, MyObituariesFragment())
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                        .commit()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Add Family
        addFamily.setOnClickListener {
            AddFamilyDialogFragment2().show((activity as AppCompatActivity)
                .supportFragmentManager, "openFamilyDialog")
        }

        // Add Media
        val addMedia = view.findViewById<ImageView>(R.id.btnAddMedia)
        addMedia.setOnClickListener {
            pickMediaLauncher.launch("image/* video/*")
        }

        // Next button
        nextButton.setOnClickListener {
            saveCurrentData()
            sharedViewModel.mediaList.value?.forEachIndexed { index, uri ->
                Log.d("NextButtonMediaList", "Media List Item $index: $uri")
            }
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep5())
                .addToBackStack("EditObituaryStep4")
                .commit()
        }

        // Previous button
        prevButton.setOnClickListener {
            saveCurrentData()
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, EditObituaryStep3())
                .commit()
        }

        return view
    }

    private fun saveCurrentData() {
        Log.d("FamilyMembers", "Family members: ${sharedViewModel2.getFamilyMembers()}")
        val mediaUriStrings = sharedViewModel2.mediaList.value?.map { it.toString() } ?: emptyList()

        mediaUriStrings.forEachIndexed { index, uri ->
            Log.d("Media List Item $index:", uri)
        }

        val familyNames = ArrayList<String>()
        val familyRelationships = ArrayList<String>()
        sharedViewModel2.getFamilyMembers().forEach { pair ->
            familyNames.add(pair.first)
            familyRelationships.add(pair.second)
        }

        sharedViewModel.mediaList.value = ArrayList(mediaUriStrings)
        sharedViewModel.familyNames.value = familyNames
        sharedViewModel.familyRelationships.value = familyRelationships
        sharedViewModel.obituaryText.value = obitTextET.text.toString()
        sharedViewModel.keyEvents.value = keyEventsET.text.toString()
    }

    private fun fetchObituaryById(obituaryId: Int) {
        // Set the current obituary ID in the ViewModel
        sharedViewModel2.setCurrentObituaryId(obituaryId)

        // Clear the media list if this is a new obituary ID
        if (!sharedViewModel2.isGalleryFetched()) {
            sharedViewModel2.clearMedia()
            mediaAdapter.notifyDataSetChanged()
        }

        val url = "$baseUrl" + "api/fetchObit/$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleError("Failed to fetch obituary: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        Log.d("Fetch Obituary", "Response received: $responseBody")

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
                                fetchedObituary?.let {
                                    sharedViewModel.obituaryText.postValue(it.OBITUARYTEXT)
                                    sharedViewModel.keyEvents.postValue(it.KEYEVENTS)
                                    galleryId = it.GALLERYID
                                    familyId = it.FAMILYID
                                    sharedViewModel.familyId.value = familyId
                                    fetchGallery(galleryId)
                                }
                            }
                        }
                    } ?: run {
                        handleError("Error: Empty response from server")
                    }
                } else {
                    handleError("Error: ${response.message}")
                }
            }
        })
    }

    private fun fetchGallery(galleryId: Int) {
        if (sharedViewModel2.isGalleryFetched()) return

        val url = "$baseUrl" + "api/fetchGallery/$galleryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                handleError("Failed to fetch gallery: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)
                        val galleryMediaList = mutableListOf<GalleryMedia>()

                        for (i in 0 until jsonArray.length()) {
                            val mediaJson = jsonArray.getJSONObject(i)
                            val mediaType = mediaJson.getString("MEDIATYPE")

                            /*if (mediaType.equals("AI Video", ignoreCase = true)) continue*/

                            val galleryMedia = GalleryMedia(
                                GALLERYMEDIAID = mediaJson.getInt("GALLERYMEDIAID"),
                                GALLERYID = mediaJson.getInt("GALLERYID"),
                                MEDIATYPE = mediaType,
                                FILENAME = mediaJson.getString("FILENAME"),
                                UPLOADDATE = mediaJson.getString("UPLOADDATE")
                            )
                            galleryMediaList.add(galleryMedia)
                        }

                        requireActivity().runOnUiThread {
                            galleryMediaList.forEach { galleryMedia ->
                                val mediaUri = Uri.fromFile(File(galleryMedia.FILENAME))
                                sharedViewModel2.addMedia(mediaUri)
                            }

                            generateThumbnailsForMedia()
                            mediaAdapter.notifyDataSetChanged()
                            sharedViewModel2.setGalleryFetched(true)
                        }
                    } ?: run {
                        handleError("Error: Empty response from server")
                    }
                } else {
                    handleError("Error: ${response.message}")
                }
            }
        })
    }

    private fun generateThumbnailsForMedia() {
        sharedViewModel2.mediaList.value?.forEach { mediaUri ->
            val path = mediaUri.path ?: ""
            val fileExtension = File(path).extension.lowercase()

            when {
                fileExtension in listOf("mp4", "mkv", "avi") -> {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(requireContext(), mediaUri)
                        val bitmap: Bitmap? = retriever.getFrameAtTime(0)
                        bitmap?.let {
                            mediaThumbnails[mediaUri] = it
                            Log.d("Thumbnail", "Generated video thumbnail for: $mediaUri")
                        }
                    } catch (e: Exception) {
                        Log.e("GenerateThumbnail", "Error generating video thumbnail: ${e.message}")
                    } finally {
                        retriever.release()
                    }
                }
                fileExtension in listOf("jpg", "jpeg", "png", "gif") -> {
                    try {
                        val file = File(path)
                        if (file.exists()) {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            bitmap?.let {
                                mediaThumbnails[mediaUri] = it
                                Log.d("Thumbnail", "Generated image thumbnail for: $mediaUri")
                            }
                        } else {
                            Log.e("GenerateThumbnail", "File does not exist: $path")
                        }
                    } catch (e: Exception) {
                        Log.e("GenerateThumbnail", "Error generating image thumbnail: ${e.message}")
                    }
                }
                else -> Log.e("GenerateThumbnail", "Unsupported file extension for thumbnail generation: $fileExtension")
            }
        }
    }

    private fun deleteMedia(uri: Uri) {
        sharedViewModel2.removeMedia(uri)
        mediaAdapter.notifyDataSetChanged()
    }

    private fun viewMedia(uri: Uri) {
        val dialogFragment = MediaPreviewDialogFragment2.newInstance(uri)
        dialogFragment.show(childFragmentManager, "MediaPreviewDialog2")
    }

    private fun handleError(errorMessage: String) {
        Log.e("EditObituaryStep4", errorMessage)
        if (isAdded) {
            requireActivity().runOnUiThread {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }
}