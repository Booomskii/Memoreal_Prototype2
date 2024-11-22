package com.example.memoreal_prototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
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

    private lateinit var photoContainerLayout: GridLayout
    private lateinit var videoContainerLayout: GridLayout
    private lateinit var aiVideoContainerLayout: GridLayout

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

        photoContainerLayout.columnCount = 4
        videoContainerLayout.columnCount = 4
        aiVideoContainerLayout.columnCount = 4

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

                                // Adjusted layout parameters to make thumbnails slightly bigger
                                val layoutParams = GridLayout.LayoutParams().apply {
                                    width = resources.displayMetrics.widthPixels / 5 - 24  // Adjust the width for 5 items per row
                                    height = width  // Keep items square
                                    setMargins(12, 12, 12, 12)  // Adjust margins for better spacing
                                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1) // Set the row span to 1
                                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1) // Set the column span to 1
                                }

                                when (galleryMedia.MEDIATYPE) {
                                    "Image" -> {
                                        val bitmap = loadImageFromFilePath(filePath)
                                        val thumbnailBitmap = bitmap?.let { createThumbnail(it) }
                                        val imageView = ImageView(requireContext()).apply {
                                            this.layoutParams = layoutParams
                                            thumbnailBitmap?.let { setImageBitmap(it) }
                                            adjustViewBounds = true
                                            scaleType = ImageView.ScaleType.CENTER_CROP  // Ensure thumbnails are cropped to center
                                            setOnClickListener {
                                                showImageDialog(bitmap)
                                            }
                                        }
                                        photoContainerLayout.addView(imageView)
                                    }
                                    "Video" -> {
                                        val videoThumbnail = createVideoThumbnail(filePath)
                                        val thumbnailView = ImageView(requireContext()).apply {
                                            this.layoutParams = layoutParams
                                            videoThumbnail?.let { setImageBitmap(it) }
                                            adjustViewBounds = true
                                            scaleType = ImageView.ScaleType.CENTER_CROP  // Ensure thumbnails are cropped to center
                                            setOnClickListener {
                                                showVideoDialog(filePath)
                                            }
                                        }
                                        videoContainerLayout.addView(thumbnailView)
                                    }
                                    "AI Video" -> {
                                        val aiVideoThumbnail = createVideoThumbnail(filePath)
                                        val aiThumbnailView = ImageView(requireContext()).apply {
                                            this.layoutParams = layoutParams
                                            aiVideoThumbnail?.let { setImageBitmap(it) }
                                            adjustViewBounds = true
                                            scaleType = ImageView.ScaleType.CENTER_CROP  // Ensure thumbnails are cropped to center
                                            setOnClickListener {
                                                showVideoDialog(filePath)
                                            }
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

    private fun createVideoThumbnail(videoPath: String): Bitmap? {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST) // Retrieves frame at 1 second mark
            retriever.release()
            bitmap?.let {
                Bitmap.createScaledBitmap(it, 250, 250, false) // Create a smaller thumbnail
            }
        } catch (e: Exception) {
            Log.e("VideoThumbnail", "Failed to create thumbnail: ${e.message}")
            null
        }
    }

    private fun showVideoDialog(videoPath: String?) {
        videoPath?.let {
            val dialogFragment = VideoDialogFragment.newInstance(it)
            dialogFragment.show(childFragmentManager, "VideoDialog")
        }
    }

    class VideoDialogFragment : DialogFragment() {

        companion object {
            private const val ARG_VIDEO_PATH = "video_path"

            fun newInstance(videoPath: String): VideoDialogFragment {
                val fragment = VideoDialogFragment()
                val args = Bundle().apply {
                    putString(ARG_VIDEO_PATH, videoPath)
                }
                fragment.arguments = args
                return fragment
            }
        }

        private lateinit var videoView: VideoView
        private lateinit var playPauseButton: ImageButton
        private lateinit var stopButton: ImageButton
        private lateinit var seekBar: SeekBar
        private var isPlaying = false
        private var updateSeekBarRunnable: Runnable? = null

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.dialog_video, container, false)

            videoView = view.findViewById(R.id.dialogVideoView)
            playPauseButton = view.findViewById(R.id.playPauseButton)
            stopButton = view.findViewById(R.id.stopButton)
            seekBar = view.findViewById(R.id.seekBar)
            val closeButton = view.findViewById<ImageButton>(R.id.closeButton)

            val videoPath = arguments?.getString(ARG_VIDEO_PATH)
            videoPath?.let {
                videoView.setVideoPath(it)

                videoView.setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false // Video won't loop by default
                    seekBar.max = mediaPlayer.duration
                    videoView.start()
                    isPlaying = true
                    playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)

                    // Update the SeekBar
                    startUpdatingSeekBar()
                }

                // Play/Pause button functionality
                playPauseButton.setOnClickListener {
                    if (isPlaying) {
                        videoView.pause()
                        playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                    } else {
                        videoView.start()
                        playPauseButton.setImageResource(R.drawable.baseline_pause_circle_24)
                        startUpdatingSeekBar()
                    }
                    isPlaying = !isPlaying
                }

                // Stop button functionality
                stopButton.setOnClickListener {
                    // Stop video playback but keep it ready for restarting
                    videoView.pause()
                    videoView.seekTo(0) // Reset to start
                    seekBar.progress = 0
                    isPlaying = false
                    playPauseButton.setImageResource(R.drawable.baseline_play_circle_24)
                }

                // SeekBar change listener
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            videoView.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {
                        // Pause video while seeking, but keep the current state
                        if (isPlaying) {
                            videoView.pause()
                        }
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        // Resume video if it was playing before seeking
                        if (isPlaying) {
                            videoView.start()
                            startUpdatingSeekBar() // Restart updating seek bar after seeking
                        }
                    }
                })
            }

            // Handle close button click
            closeButton.setOnClickListener {
                dismiss() // This will close the dialog
            }

            return view
        }

        private fun startUpdatingSeekBar() {
            // Remove any pending callbacks to avoid duplication
            updateSeekBarRunnable?.let { seekBar.removeCallbacks(it) }

            // Update SeekBar
            updateSeekBarRunnable = object : Runnable {
                override fun run() {
                    if (videoView.isPlaying) {
                        seekBar.progress = videoView.currentPosition
                        seekBar.postDelayed(this, 500)
                    }
                }
            }
            seekBar.postDelayed(updateSeekBarRunnable!!, 500)
        }

        override fun onDestroyView() {
            super.onDestroyView()
            // Remove any pending callbacks to avoid memory leaks
            updateSeekBarRunnable?.let { seekBar.removeCallbacks(it) }
        }
    }
}
