package com.example.memoreal_prototype

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView

class CreateObituaryStep7 : Fragment() {

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

        val sharedPref = requireActivity().getSharedPreferences("ObituaryPrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()


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

        val guestBookSwitchState = arguments?.getString("guestBookSwitchState")
        guestBookSwitchState?.let {
            Log.d("guestBookSwitchState", it.toString()) // Log the media URIs
        } ?: Log.d("guestBookSwitchState", "No guestBookSwitchState received")

        // Retrieve family names
        val privacyType = arguments?.getString("privacyType")
        privacyType?.let {
            Log.d("privacyType", it.toString()) // Log the family names
        } ?: Log.d("privacyType", "No privacyType received")

        val savedBackgroundTheme = sharedPref.getString("backgroundTheme", null)
        val savedPictureFrame = sharedPref.getInt("pictureFrame", -1)
        val savedBgMusic = sharedPref.getString("bgMusic", null)
        val savedVirtualFlower = sharedPref.getInt("virtualFlower", -1)
        val savedVirtualCandle = sharedPref.getInt("virtualCandle", -1)
        val savedFavQuote = sharedPref.getString("favQuote", "")

        Log.d("STEP 7 SF - Background Theme:", savedBackgroundTheme ?: "No background theme found")
        Log.d("STEP 7 SF - Picture Frame:", (savedPictureFrame ?: 0).toString())
        Log.d("STEP 7 SF - Background Music:", savedBgMusic ?: "No background music found")
        Log.d("STEP 7 SF - Virtual Flower:", (savedVirtualFlower ?: 0).toString())
        Log.d("STEP 7 SF - Virtual Candle:", (savedVirtualCandle ?: 0).toString())
        Log.d("STEP 7 SF - Favorite Quote:", savedFavQuote!!)
        Log.d("STEP 7 SF - Bundle:", this.arguments.toString())

        savedBackgroundTheme?.let {
            val position = adapter2.getPosition(it)
            backgroundSpinner.setSelection(position)
        }

        savedPictureFrame?.let {
            val position = frameItems.indexOfFirst { pair -> pair.first == it }
            if (position != -1) {
                frameSpinner.setSelection(position)
            }
        }

        savedBgMusic?.let {
            val position = adapter3.getPosition(it)
            bgMusicSpinner.setSelection(position)
        }

        savedVirtualFlower?.let {
            val position = flowerItems.indexOfFirst { pair -> pair.first == it }
            if (position != -1) {
                vflowerSpinner.setSelection(position)
            }
        }

        savedVirtualCandle?.let {
            val position = candleItems.indexOfFirst { pair -> pair.first == it }
            if (position != -1) {
                vcandleSpinner.setSelection(position)
            }
        }

        favQuoteET.setText(savedFavQuote)

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_out_left, R.anim.slide_out_right)
                .commit()
        }

        nextButton.setOnClickListener {
            val backgroundTheme = backgroundSpinner.selectedItem.toString()
            val pictureFrame = frameSpinner.selectedItem.toString()
            val bgMusic = bgMusicSpinner.selectedItem.toString()
            val virtualFlower = vflowerSpinner.selectedItem.toString()
            val virtualCandle = vcandleSpinner.selectedItem.toString()
            val favQuote = favQuoteET.text.toString()

            editor.putString("backgroundTheme", backgroundTheme)
            editor.putInt("pictureFrame", frameItems[frameSpinner.selectedItemPosition].first)
            editor.putString("bgMusic", bgMusic)
            editor.putInt("virtualFlower", flowerItems[vflowerSpinner.selectedItemPosition].first)
            editor.putInt("virtualCandle", candleItems[vcandleSpinner.selectedItemPosition].first)
            editor.putString("favQuote", favQuote)
            editor.apply()

            val bundle = Bundle().apply {
                putString("backgroundTheme", backgroundTheme)
                putString("pictureFrame", pictureFrame)
                putString("bgMusic", bgMusic)
                putString("virtualFlower", virtualFlower)
                putString("virtualCandle", virtualCandle)
                putString("favQuote", favQuote)
            }

            Log.d("STEP7", bundle.toString())

            val createObituaryStep8 = CreateObituaryStep8()
            val existingBundle = this.arguments
            existingBundle?.let { bundle.putAll(it) }
            createObituaryStep8.arguments = bundle

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, createObituaryStep8)
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

        backgroundSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }

        frameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }

        bgMusicSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }

        vflowerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }

        vcandleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed here
            }
        }

        return view
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
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout
                .spinner_flowers, parent, false)
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
            val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout
                .spinner_candles, parent, false)
            val imageView = itemView.findViewById<ImageView>(R.id.spinner_image)
            val textView = itemView.findViewById<TextView>(R.id.spinner_text)

            val item = getItem(position)
            imageView.setImageResource(item?.first ?: 0) // Set the image in dropdown
            textView.text = item?.second // Set the text in dropdown

            return itemView
        }
    }
}
