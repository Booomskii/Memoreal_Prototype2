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
import androidx.fragment.app.activityViewModels

class CreateObituaryStep7 : Fragment() {

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

        setSpinnerTextColor(backgroundSpinner)
        setSpinnerTextColor(frameSpinner)
        setSpinnerTextColor(bgMusicSpinner)
        setSpinnerTextColor(vflowerSpinner)
        setSpinnerTextColor(vcandleSpinner)

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
