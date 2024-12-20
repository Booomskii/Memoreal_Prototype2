package com.example.memoreal_prototype

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

class CreateObituaryStep7 : Fragment() {

    val client = UserSession.client
    val baseUrl = UserSession.baseUrl
    val url = UserSession.d_idUrl
    val authorization = UserSession.authorization
    private lateinit var aiVideoContainerLayout : LinearLayout

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step7, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val backgroundSpinner = view.findViewById<Spinner>(R.id.spinnerBGTheme)
        val frameSpinner = view.findViewById<Spinner>(R.id.spinnerPicFrame)
        val bgMusicSpinner = view.findViewById<Spinner>(R.id.spinnerBGMusic)
        val vflowerSpinner = view.findViewById<Spinner>(R.id.spinnerVFlowers)
        val vcandleSpinner = view.findViewById<Spinner>(R.id.spinnerVCandles)
        val favQuoteET = view.findViewById<EditText>(R.id.etFavQuote)
        val uploadAI = view.findViewById<Button>(R.id.btnUploadAIItem)

        val frameItems = listOf(
            Pair(R.drawable.classic1_option, "Classic 1"),
            Pair(R.drawable.classic2_option, "Classic 2"),
            Pair(R.drawable.classic3_option, "Classic 3"),
            Pair(R.drawable.gold1_option, "Gold 1"),
            Pair(R.drawable.gold2_option, "Gold 2"),
            Pair(R.drawable.gold3_option, "Gold 3"),
            Pair(R.drawable.wood1_option, "Wood 1"),
            Pair(R.drawable.wood2_option, "Wood 2"),
            Pair(R.drawable.wood3_option, "Wood 3")
        )

        val flowerItems = listOf(
            Pair(R.drawable.rose, "Rose"),
            Pair(R.drawable.lily, "Lily"),
            Pair(R.drawable.carnation, "Carnation"),
            Pair(R.drawable.chrysanthemum, "Chrysanthemum"),
            Pair(R.drawable.iris, "Iris"),
            Pair(R.drawable.orchid, "Orchid"),
        )

        val candleItems = listOf(
            Pair(R.drawable.candle1, "Candle 1"),
            Pair(R.drawable.candle2, "Candle 2"),
            Pair(R.drawable.candle3, "Candle 3"),
            Pair(R.drawable.candle4, "Candle 4"),
            Pair(R.drawable.candle5, "Candle 5"),
            Pair(R.drawable.candle6, "Candle 6"),
            Pair(R.drawable.candle7, "Candle 7"),
            Pair(R.drawable.candle8, "Candle 8"),
        )

        /*Background Theme Spinner*/
        val adapter2 = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.background_spinner,
            android.R.layout.simple_spinner_item
        )

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        backgroundSpinner.adapter = adapter2

        /*Frame Spinner*/
        val adapter = ImageSpinnerAdapter(requireContext(), frameItems)
        frameSpinner.adapter = adapter

        /*Background Music Spinner*/
        val adapter3 = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.bg_music_spinner,
            android.R.layout.simple_spinner_item
        )

        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        bgMusicSpinner.adapter = adapter3

        /*Virtual Flower Spinner*/
        val adapter4 = ImageSpinnerAdapter2(requireContext(), flowerItems)
        vflowerSpinner.adapter = adapter4

        /*Virtual Candle Spinner*/
        val adapter5 = ImageSpinnerAdapter3(requireContext(), candleItems)
        vcandleSpinner.adapter = adapter5

        backgroundSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedTheme = backgroundSpinner.selectedItem.toString()
                sharedViewModel.backgroundTheme.value = selectedTheme
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        vcandleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = vcandleSpinner.selectedItem as Pair<Int, String>
                sharedViewModel.virtualCandle.value = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        vflowerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = vflowerSpinner.selectedItem as Pair<Int, String>
                sharedViewModel.virtualFlower.value = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        frameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = frameSpinner.selectedItem as Pair<Int, String>
                sharedViewModel.pictureFrame.value = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        bgMusicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedMusic = bgMusicSpinner.selectedItem.toString()
                sharedViewModel.bgMusic.value = selectedMusic
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Observe and set the selected values
        sharedViewModel.virtualCandle.observe(viewLifecycleOwner) { selectedPair ->
            selectedPair?.let {
                val position = adapter5.getPosition(it)
                vcandleSpinner.setSelection(position)
            }
        }

        sharedViewModel.virtualFlower.observe(viewLifecycleOwner) { selectedPair ->
            selectedPair?.let {
                val position = adapter4.getPosition(it)
                vflowerSpinner.setSelection(position)
            }
        }

        sharedViewModel.pictureFrame.observe(viewLifecycleOwner) { selectedPair ->
            selectedPair?.let {
                val position = adapter.getPosition(it)
                frameSpinner.setSelection(position)
            }
        }

        sharedViewModel.bgMusic.observe(viewLifecycleOwner) { bgMusic ->
            bgMusic?.let {
                val position = adapter3.getPosition(it)
                if (position >= 0) {
                    bgMusicSpinner.setSelection(position)
                }
            }
        }

        // Observe the selected background theme and set the spinner's selection based on it
        sharedViewModel.backgroundTheme.observe(viewLifecycleOwner) { selectedTheme ->
            val position = adapter2.getPosition(selectedTheme)  // Find position of theme in adapter
            if (position >= 0) {
                backgroundSpinner.setSelection(position)
            }
        }

        sharedViewModel.favQuote.observe(viewLifecycleOwner) { favQuote ->
            favQuoteET.setText(favQuote)
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        uploadAI.setOnClickListener {
            // Open dialog to upload photo
            val uploadPhotoDialog = UploadPhotoDialogFragment()
            uploadPhotoDialog.show(parentFragmentManager, "UploadPhotoDialog")
        }

        nextButton.setOnClickListener {
            val backgroundTheme = backgroundSpinner.selectedItem.toString()
            val pictureFrame = frameSpinner.selectedItem as? Pair<Int, String>
            val bgMusic = bgMusicSpinner.selectedItem.toString()
            val virtualFlower = vflowerSpinner.selectedItem as? Pair<Int, String>
            val virtualCandle = vcandleSpinner.selectedItem as? Pair<Int, String>
            val favQuote = favQuoteET.text.toString()

            sharedViewModel.backgroundTheme.value = backgroundTheme
            sharedViewModel.pictureFrame.value = pictureFrame
            sharedViewModel.bgMusic.value = bgMusic
            sharedViewModel.virtualFlower.value = virtualFlower
            sharedViewModel.virtualCandle.value = virtualCandle
            sharedViewModel.favQuote.value = favQuote

            Log.d("STEP 7:", sharedViewModel.backgroundTheme.value!!)
            Log.d("STEP 7:", sharedViewModel.pictureFrame.value!!.toString())
            Log.d("STEP 7:", sharedViewModel.bgMusic.value!!)
            Log.d("STEP 7:", sharedViewModel.virtualFlower.value!!.toString())
            Log.d("STEP 7:", sharedViewModel.virtualCandle.value!!.toString())
            Log.d("STEP 7:", sharedViewModel.favQuote.value!!)

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep8())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .addToBackStack("CreateObituaryStep7")
                .commit()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep6())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        setSpinnerTextColor(bgMusicSpinner)
        setSpinnerTextColor(vflowerSpinner)
        setSpinnerTextColor(vcandleSpinner)
        setSpinnerTextColor(backgroundSpinner)
        setSpinnerTextColor(frameSpinner)

        sharedViewModel.videoIds.observe(viewLifecycleOwner) { videoIds ->
            Log.d("VideoRetrieve", "Observer triggered for videoIds: $videoIds")

            videoIds?.forEach { videoId ->
                val isRetrieved = sharedViewModel.isVideoRetrievedMap.value?.get(videoId) ?: false
                Log.d("VideoRetrieve", "Checking if video is retrieved for videoId: $videoId, isRetrieved = $isRetrieved")

                if (isRetrieved) {
                    // Add the thumbnail if video is already retrieved
                    val videoUrl = sharedViewModel.retrievedVideos.value?.find { it.contains(videoId) }
                    if (videoUrl != null) {
                        Log.d("VideoRetrieve", "Adding video thumbnail for already retrieved videoId: $videoId")
                        addVideoThumbnail(videoUrl)
                    } else {
                        Log.e("VideoRetrieve", "No video URL found for retrieved videoId: $videoId")
                    }
                } else {
                    // Retrieve video if not retrieved
                    Log.d("VideoRetrieve", "Starting video retrieval for videoId: $videoId")
                    retrieveGeneratedVideo(videoId)
                }
            }
        }

        return view
    }

    private fun setSpinnerTextColor(spinner: Spinner) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }
    }

    private fun retrieveGeneratedVideo(videoId: String) {
        // Check if the video is already retrieved
        if (sharedViewModel.isVideoRetrievedMap.value?.get(videoId) == true) {
            Log.d("VideoRetrieve", "Video already retrieved, skipping for videoId: $videoId")
            return
        }

        val apiUrl = "$baseUrl" + "api/retrieveVideo/$videoId"

        lifecycleScope.launch(Dispatchers.IO) {
            var videoReady = false
            var resultUrl: String? = null

            while (!videoReady) {
                try {
                    // Make the request to check video status
                    val request = Request.Builder()
                        .url(apiUrl)
                        .get()
                        .build()

                    val response = client.newCall(request).execute() // Execute the request synchronously
                    if (response.isSuccessful) {
                        val responseString = response.body?.string()
                        responseString?.let {
                            val jsonObject = JSONObject(it)

                            if (jsonObject.getBoolean("success")) {
                                val data = jsonObject.getJSONObject("data")
                                val status = data.optString("status", "unknown")

                                when {
                                    status == "done" && data.has("result_url") -> {
                                        // Video rendering is complete
                                        resultUrl = data.getString("result_url")
                                        videoReady = true
                                    }
                                    status == "pending" || data.has("pending_url") -> {
                                        // Video is still processing, continue polling
                                        Log.d("VideoRetrieve", "Video $videoId still pending, polling again in 5 seconds...")
                                        delay(5000) // Wait for 5 seconds before the next poll
                                    }
                                    else -> {
                                        // Handle unexpected status
                                        Log.e("VideoRetrieve", "Unexpected status or response for videoId: $videoId")
                                        delay(5000) // Retry after a delay
                                    }
                                }
                            } else {
                                // Log unsuccessful responses and retry after delay
                                Log.e("VideoRetrieve", "Failed to retrieve video $videoId: ${jsonObject.optString("message", "Unknown error")}")
                                delay(5000) // Retry after a delay
                            }
                        }
                    } else {
                        // Log unsuccessful responses and retry after delay
                        Log.e("VideoRetrieve", "Retrieve failed for video $videoId with response code: ${response.code}")
                        delay(5000) // Retry after a delay
                    }
                } catch (e: IOException) {
                    Log.e("VideoRetrieve", "Failed to retrieve video $videoId: ${e.message}")
                    delay(5000) // Retry after a delay in case of network failure
                }
            }

            // Handle the result URL on the main thread after video is ready
            resultUrl?.let {
                withContext(Dispatchers.Main) {
                    addVideoThumbnail(it)
                    downloadAndSaveVideo(it)
                    Toast.makeText(requireContext(), "Video retrieved successfully for videoId: $videoId", Toast.LENGTH_SHORT).show()

                    // Set `isVideoRetrieved` to true for this videoId in the SharedViewModel
                    sharedViewModel.isVideoRetrievedMap.value?.put(videoId, true)
                    sharedViewModel.isVideoRetrievedMap.postValue(sharedViewModel.isVideoRetrievedMap.value)

                    // Add the retrieved video URL to the list
                    sharedViewModel.retrievedVideos.value?.add(it)
                    sharedViewModel.retrievedVideos.postValue(sharedViewModel.retrievedVideos.value)
                }
            }
        }
    }

    private fun addVideoThumbnail(resultUrl: String) {
        val aiVideoContainerLayout = view?.findViewById<GridLayout>(R.id.aiVideoContainerLayout) ?: return

        // Check if a thumbnail for this video URL already exists
        val existingThumbnail = aiVideoContainerLayout.children.find {
            it.tag == resultUrl
        }
        if (existingThumbnail != null) {
            Log.d("VideoThumbnail", "Thumbnail for this video already exists.")
            return
        }

        // Inflate the custom thumbnail layout
        val thumbnailLayout = LayoutInflater.from(requireContext())
            .inflate(R.layout.layout_video_thumbnail, aiVideoContainerLayout, false)

        // Set the tag for easy identification
        thumbnailLayout.tag = resultUrl

        // Set fixed dimensions for the thumbnail layout in GridLayout
        val layoutParams = GridLayout.LayoutParams().apply {
            width = 300  // Set width to 400px
            height = 300  // Set height to 400px (cube shape)
            setMargins(16, 16, 16, 16)  // Add some space around each item for better spacing
        }
        thumbnailLayout.layoutParams = layoutParams

        val thumbnailImageView = thumbnailLayout.findViewById<ImageView>(R.id.thumbnail_image)
        val playIconImageView = thumbnailLayout.findViewById<ImageView>(R.id.play_icon)
        val progressBar = thumbnailLayout.findViewById<ProgressBar>(R.id.progress_bar)

        // Ensure the progress bar is visible initially while loading the thumbnail
        progressBar.visibility = View.VISIBLE
        thumbnailImageView.visibility = View.INVISIBLE
        playIconImageView.visibility = View.INVISIBLE

        // Load the thumbnail using Glide
        Glide.with(this)
            .load(resultUrl) // Load thumbnail from the video URL
            .placeholder(R.drawable.baseline_photo_24) // Replace with your placeholder image
            .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Hide the progress bar if the image failed to load
                    progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Hide the progress bar and show the ImageView and play icon when the thumbnail is ready
                    progressBar.visibility = View.GONE
                    thumbnailImageView.visibility = View.VISIBLE
                    playIconImageView.visibility = View.VISIBLE
                    return false
                }
            })
            .into(thumbnailImageView)

        // Add the thumbnail layout to the GridLayout container
        aiVideoContainerLayout.addView(thumbnailLayout)

        // Set OnClickListener for the thumbnail to preview the video
        thumbnailLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resultUrl))
            intent.setDataAndType(Uri.parse(resultUrl), "video/*") // Set the MIME type to video
            startActivity(intent)
        }
    }

    private fun downloadAndSaveVideo(videoUrl: String) {
        // Check if the video is already downloaded
        if (sharedViewModel.downloadedVideos.value?.contains(videoUrl) == true) {
            Log.d("VideoDownload", "Video already downloaded: $videoUrl")
            return
        }

        // Proceed with the download if the video is not already downloaded
        val request = Request.Builder().url(videoUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("VideoDownload", "Failed to download video: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to download video", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { body ->
                        try {
                            // Include "ai_video" in the file name
                            val file = File(requireContext().filesDir, "ai_video_${System.currentTimeMillis()}.mp4")
                            val outputStream = file.outputStream()
                            outputStream.use { body.byteStream().copyTo(it) }

                            val videoPath = file.absolutePath
                            requireActivity().runOnUiThread {
                                val currentList = sharedViewModel.aiVideoUrl.value ?: ArrayList()
                                if (!currentList.contains(videoPath)) {
                                    currentList.add(videoPath)
                                    sharedViewModel.aiVideoUrl.value = currentList
                                    Log.d("AI Video Urls:", currentList.toString())
                                    Toast.makeText(requireContext(), "Video saved successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.d("AI Video Urls:", "Duplicate video path not added: $videoPath")
                                }

                                // Add the video URL to the downloaded videos list
                                sharedViewModel.downloadedVideos.value?.add(videoUrl)
                                sharedViewModel.downloadedVideos.postValue(sharedViewModel.downloadedVideos.value)
                            }
                        } catch (e: IOException) {
                            Log.e("VideoDownload", "Failed to save video: ${e.message}")
                            requireActivity().runOnUiThread {
                                Toast.makeText(requireContext(), "Failed to save video", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("VideoDownload", "Download failed with response code: ${response.code}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Download failed with response code: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private class ImageSpinnerAdapter(
        context: Context,
        private val items: List<Pair<Int, String>> // Pair of image resource and text
    ) : ArrayAdapter<Pair<Int, String>>(context, 0, items) {

        // This method is called to create the view that will be shown when the spinner is not clicked (i.e. the selected item view)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
            val selectedItem = getItem(position)

            val textOnlyView = textView.findViewById<TextView>(android.R.id.text1)
            textOnlyView.text = selectedItem?.second // Show only the text

            return textView
        }

        // This method is called to create each view in the dropdown (when the spinner is expanded)
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_picture_frame, parent, false)
            val imageView = itemView.findViewById<ImageView>(R.id.spinner_image)
            val textView = itemView.findViewById<TextView>(R.id.spinner_text)

            val item = getItem(position)
            imageView.setImageResource(item?.first ?: 0) // Set the image in dropdown
            textView.text = item?.second // Set the text in dropdown

            return itemView
        }
    }

    private class ImageSpinnerAdapter2(
        context: Context,
        private val items: List<Pair<Int, String>> // Pair of image resource and text
    ) : ArrayAdapter<Pair<Int, String>>(context, 0, items) {

        // This method is called to create the view that will be shown when the spinner is not clicked (i.e. the selected item view)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
            val selectedItem = getItem(position)

            val textOnlyView = textView.findViewById<TextView>(android.R.id.text1)
            textOnlyView.text = selectedItem?.second // Show only the text

            return textView
        }

        // This method is called to create each view in the dropdown (when the spinner is expanded)
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_flowers, parent, false)
            val imageView = itemView.findViewById<ImageView>(R.id.spinner_image)
            val textView = itemView.findViewById<TextView>(R.id.spinner_text)

            val item = getItem(position)
            imageView.setImageResource(item?.first ?: 0) // Set the image in dropdown
            textView.text = item?.second // Set the text in dropdown

            return itemView
        }
    }

    private class ImageSpinnerAdapter3(
        context: Context,
        private val items: List<Pair<Int, String>> // Pair of image resource and text
    ) : ArrayAdapter<Pair<Int, String>>(context, 0, items) {

        // This method is called to create the view that will be shown when the spinner is not clicked (i.e. the selected item view)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false)
            val selectedItem = getItem(position)

            val textOnlyView = textView.findViewById<TextView>(android.R.id.text1)
            textOnlyView.text = selectedItem?.second // Show only the text

            return textView
        }

        // This method is called to create each view in the dropdown (when the spinner is expanded)
        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_candles, parent, false)
            val imageView = itemView.findViewById<ImageView>(R.id.spinner_image)
            val textView = itemView.findViewById<TextView>(R.id.spinner_text)

            val item = getItem(position)
            imageView.setImageResource(item?.first ?: 0) // Set the image in dropdown
            textView.text = item?.second // Set the text in dropdown

            return itemView
        }
    }
}

