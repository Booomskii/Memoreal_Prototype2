package com.example.memoreal_prototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import com.example.memoreal_prototype.models.Obituary
import com.example.memoreal_prototype.models.Obituary_Customization
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ObituaryFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl
    private var userId = 0

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var buttons: List<Button>
    private var isPlaying = false
    private var fetchedObituary: Obituary? = null
    private var fetchedObitCust: Obituary_Customization? = null
    private var music = ""
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_obituary, container, false)
        setupToolbar(view)
        val playPauseButton = view.findViewById<ImageButton>(R.id.btnPlayPause)
        val stopButton = view.findViewById<ImageButton>(R.id.btnStop)
        val obituaryId = arguments?.getInt("obituaryId")
        val aboutButton = view.findViewById<Button>(R.id.btnAbout)
        val familyButton = view.findViewById<Button>(R.id.btnFamily)
        val galleryButton = view.findViewById<Button>(R.id.btnGallery)
        val guestbookButton = view.findViewById<Button>(R.id.btnGuestbook)

        // Initialize the buttons list
        buttons = listOf(aboutButton, familyButton, galleryButton, guestbookButton)

        // Set default fragment and highlight the About button initially
        replaceFragment(AboutFragment())
        highlightSelectedButton(aboutButton)

        aboutButton.setOnClickListener {
            replaceFragment(AboutFragment())
            highlightSelectedButton(aboutButton)
        }

        familyButton.setOnClickListener {
            replaceFragment(FamilyFragment())
            highlightSelectedButton(familyButton)
        }

        galleryButton.setOnClickListener {
            replaceFragment(GalleryFragment())
            highlightSelectedButton(galleryButton)
        }

        guestbookButton.setOnClickListener {
            replaceFragment(GuestbookFragment())
            highlightSelectedButton(guestbookButton)
        }

        // Optionally set the initial selected state, e.g., select "About" button by default
        aboutButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.memo_orange))
        familyButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color
            .black))
        galleryButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        guestbookButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))


        if (obituaryId != null) {
            fetchObituaryById(obituaryId)
        }

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

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)

        // Find the TextView inside the included toolbar
        val toolbarTitle = toolbar.findViewById<TextView>(R.id.createObituaryTitle)

        // Update the TextView's text
        toolbarTitle.text = getString(R.string.obit)

        backButton.setOnClickListener {
            activity?.let {
                it.supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, ExploreFragment())
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                    .commit()
            }
        }
    }

    private fun highlightSelectedButton(selectedButton: Button) {
        // Iterate over all buttons and update their appearance
        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.memo_orange)) // Highlight color
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            } else {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black)) // Default color
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
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

                        fetchedObitCust = Obituary_Customization(
                            OBITCUSTID = obitCustId,
                            BGTHEME = jsonObject.getString("BGTHEME"),
                            PICFRAME = jsonObject.getString("PICFRAME"),
                            BGMUSIC = jsonObject.getString("BGMUSIC"),
                            VFLOWER = jsonObject.getString("VFLOWER"),
                            VCANDLE = jsonObject.getString("VCANDLE")
                        )

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                // Update the obitImage UI here
                                fetchedObituary?.let {
                                    sharedViewModel.obituaryId.value = it.OBITUARYID
                                    val obitImage = view?.findViewById<ImageView>(R.id.obituary_image)
                                    val obitName = view?.findViewById<TextView>(R.id.tvObitName)
                                    val dateBirth = view?.findViewById<TextView>(R.id.tvDateBirth)
                                    val dateDeath = view?.findViewById<TextView>(R.id.tvDateDeath)
                                    val age = view?.findViewById<TextView>(R.id.tvAge)

                                    if (it.OBITUARYPHOTO.isNotEmpty()) {
                                        val bitmap = loadImageFromInternalStorage(it.OBITUARYPHOTO)
                                        if (bitmap != null) {
                                            obitImage?.setImageBitmap(bitmap)
                                        } else {
                                            Log.e("ProfileFragment", "Failed to load image from path: ${it.OBITUARYPHOTO}")
                                            obitImage?.setImageResource(R.drawable.baseline_person_24)
                                        }
                                    } else {
                                        obitImage?.setImageResource(R.drawable.baseline_person_24)
                                    }

                                    val originalFormat = SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss" +
                                            ".SSS'Z'", Locale.getDefault())
                                    val date = originalFormat.parse(it.DATEOFBIRTH)
                                    val date2 = originalFormat.parse(it.DATEOFDEATH)

                                    // Change the desired format to "MMM. dd, yyyy" to get "Nov. 19, 2024"
                                    val desiredFormat = SimpleDateFormat("MMM. dd, yyyy", Locale.getDefault())

                                    val formattedDate = date?.let { desiredFormat.format(it) }
                                    val formattedDate2 = date2?.let { desiredFormat.format(it) }

                                    obitName?.text = it.OBITUARYNAME
                                    dateBirth?.text = formattedDate ?: ""
                                    dateDeath?.text = formattedDate2 ?: ""
                                    if (date != null && date2 != null) {
                                        val birthCalendar = Calendar.getInstance().apply { time =
                                            date }
                                        val deathCalendar = Calendar.getInstance().apply { time =
                                            date2 }

                                        var ageValue = deathCalendar.get(Calendar.YEAR) -
                                                birthCalendar.get(Calendar.YEAR)
                                        if (deathCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                                            ageValue--
                                        }

                                        val age = view?.findViewById<TextView>(R.id.tvAge)
                                        age?.text = "$ageValue years old"
                                    }
                                }

                                // Update vflower and vcandle UI here
                                fetchedObitCust?.let {
                                    val vflower = view?.findViewById<ImageView>(R.id.imgFlower)
                                    val vcandle = view?.findViewById<ImageView>(R.id.imgCandle)

                                    vflower?.setImageResource(R.drawable.default_flower_icon)
                                    vcandle?.setImageResource(R.drawable.default_candle_icon)

                                    val obitImage = view?.findViewById<ImageView>(R.id
                                        .obituary_image)
                                    val musicLabel = view?.findViewById<TextView>(R.id.musicName)
                                    musicLabel?.isSelected = true
                                    musicLabel?.text = it.BGMUSIC
                                    music = it.BGMUSIC

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
                                    val backgroundTheme = it.BGTHEME
                                    Log.d("OBITUARY PAGE", it.BGTHEME)
                                    val backgroundResource = backgroundThemeMap[backgroundTheme]
                                    if (isAdded && view != null) {
                                        val mainLayout = view?.findViewById<NestedScrollView>(R.id.nsvObituary)
                                        backgroundResource?.let {
                                            mainLayout?.setBackgroundResource(it)
                                        } ?: Log.e("BACKGROUND THEME", "Background resource is null for value: $backgroundTheme")
                                    }

                                    val flowerMap = mapOf(
                                        "Rose" to R.drawable.rose,
                                        "Lily" to R.drawable.lily,
                                        "Carnation" to R.drawable.carnation,
                                        "Chrysanthemum" to R.drawable.chrysanthemum,
                                        "Iris" to R.drawable.iris,
                                        "Orchid" to R.drawable.orchid
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

                                    if (obitImage != null) {
                                        setFrameForeground(it.PICFRAME, obitImage,
                                            pictureFrameMap, R.drawable.classic1)
                                    }
                                    loadImage(it.VFLOWER, vflower!!, flowerMap, R.drawable.default_flower_icon)
                                    loadImage(it.VCANDLE, vcandle!!, candleMap, R.drawable.default_candle_icon)

                                    setupMediaPlayer()
                                    startMusic()
                                    if (isPlaying) {
                                        startMusic()
                                        val playPauseButton = view?.findViewById<ImageButton>(R
                                            .id.btnPlayPause)
                                        playPauseButton?.setImageResource(R.drawable
                                            .baseline_pause_circle_24) // Set to pause icon
                                    }
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
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        val musicMap = mapOf(
            "Amazing Grace Instrumental" to R.raw.amazing_grace_instrumental,
            "Emotional Soaking Prayer" to R.raw.christian_instrumental_piano_worship_calm_emotional_soaking_prayer,
            "Living on the Prayer" to R.raw.living_on_the_prayer,
            "Mindfulness Meditation" to R.raw.mindfulness_meditation,
            "Peaceful Prayer Ambience" to R.raw.peaceful_prayer_meditation_piano_ambient_music,
            "Silent Prayer" to R.raw.silent_prayer_instrumental
        )
        val selectedMusic = music
        val musicResId = musicMap[selectedMusic] ?: R.raw.amazing_grace_instrumental // Replace with default if not found

        mediaPlayer = MediaPlayer.create(requireContext(), musicResId)
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            view?.findViewById<ImageButton>(R.id.btnPlayPause)?.setImageResource(R.drawable.baseline_play_circle_24)
        }
    }

    private fun loadImageFromInternalStorage(imagePath: String): Bitmap? {
        return try {
            val cleanPath = imagePath.replace("file://", "")
            val imgFile = File(cleanPath)

            if (imgFile.exists()) {
                BitmapFactory.decodeFile(imgFile.absolutePath)
            } else {
                Log.e("ProfileFragment", "Image file does not exist at path: $cleanPath")
                null
            }
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Failed to load image: ${e.message}")
            null
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

    private fun stopMusic() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            isPlaying = false
            mediaPlayer.reset()
            setupMediaPlayer() // Reinitialize after resetting
        }
    }

    private fun loadImage(resourceName: String?, imageView: ImageView, resourceMap: Map<String, Int>, defaultResource: Int) {
        val resourceId = resourceMap[resourceName] ?: defaultResource
        imageView.setImageResource(resourceId)
    }

    // Additional method to change the frame foreground programmatically
    private fun setFrameForeground(resourceName: String?, imageView: ImageView, frameMap: Map<String, Int>, defaultFrame: Int) {
        val frameResourceId = frameMap[resourceName] ?: defaultFrame
        imageView.foreground = resources.getDrawable(frameResourceId, requireContext().theme)
    }

    override fun onPause() {
        super.onPause()
        pauseMusic() // Pause the music when the app goes to the background
    }

    override fun onResume() {
        super.onResume()
        // Check if mediaPlayer is initialized and if it was playing before going into the background
        if (this::mediaPlayer.isInitialized && !isPlaying) {
            startMusic()
            val playPauseButton = view?.findViewById<ImageButton>(R.id.btnPlayPause)
            playPauseButton?.setImageResource(R.drawable.baseline_pause_circle_24)
        }
    }

    override fun onStop() {
        super.onStop()
        stopMusic() // Stop the music when the app is stopped
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Release MediaPlayer resources when the fragment view is destroyed
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
