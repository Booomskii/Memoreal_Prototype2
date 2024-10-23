package com.example.memoreal_prototype

import MediaAdapter
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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class CreateObituaryStep4 : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var recyclerView: RecyclerView

    // Launcher for picking multiple media files
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.let {
            it.forEach { uri ->
                sharedViewModel.addMedia(uri)
                Log.d("Media Added", uri.toString()) // Log each added media URI
            }
            mediaAdapter.notifyDataSetChanged() // Refresh adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step4, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMedia) // Ensure this ID matches your layout
        mediaAdapter = MediaAdapter(
            getMediaList = { sharedViewModel.mediaList }, // Function to get current media list
            onDeleteClick = { uri -> deleteMedia(uri) }, // Handle media deletion
            onMediaClick = { uri -> viewMedia(uri) } // Handle media click to view
        )
        recyclerView.adapter = mediaAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Toolbar and button initialization
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val addFamily = view.findViewById<Button>(R.id.btnAddFamily)
        val addMedia = view.findViewById<ImageView>(R.id.btnAddMedia)
        val obitText = view.findViewById<EditText>(R.id.etObituaryText)
        val keyEvents = view.findViewById<EditText>(R.id.etKeyEvents)

        Log.d("STEP 4 SF - Bundle:", this.arguments.toString())

        // Back button functionality
        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .commit()
        }

        // Add family dialog
        addFamily.setOnClickListener {
            AddFamilyDialogFragment().show((activity as AppCompatActivity).supportFragmentManager, "openFamilyDialog")
        }

        // Add media button functionality
        addMedia.setOnClickListener {
            pickMediaLauncher.launch("image/* video/*")
        }

        // Next button functionality
        nextButton.setOnClickListener {
            Log.d("FamilyMembers", "Family members: ${sharedViewModel.getFamilyMembers()}")
            val mediaUriStrings = sharedViewModel.mediaList.map { it.toString() }

            mediaUriStrings.forEachIndexed { index, uri ->
                Log.d("Media List Item $index:", uri) // Log each item with its index
            }

            // Convert family members (List<Pair<String, String>>) to two separate ArrayLists
            val familyNames = ArrayList<String>()
            val familyRelationships = ArrayList<String>()
            sharedViewModel.getFamilyMembers().forEach { pair ->
                familyNames.add(pair.first)
                familyRelationships.add(pair.second)
            }

            // Create the bundle and pass the media and family data
            val bundle = Bundle().apply {
                putStringArrayList("mediaList", ArrayList(mediaUriStrings)) // Pass media as String URIs
                putStringArrayList("familyNames", familyNames) // Pass family names
                putStringArrayList("familyRelationships", familyRelationships) // Pass family relationships
                putString("obituaryText", obitText.text.toString())
                putString("keyEvents", keyEvents.text.toString())
            }

            Log.d("Family Names: ", "$familyNames")
            Log.d("Family Relationships: ", "$familyRelationships")
            Log.d("Obituary Text: ", "${obitText.text}")
            Log.d("Key Events: ", "${keyEvents.text}")
            Log.d("Key Events: ", "${keyEvents.text}")

            val createObituaryStep5 = CreateObituaryStep5()
            val existingBundle = this.arguments
            existingBundle?.let { bundle.putAll(it) } // Merge existing arguments with the new bundle
            createObituaryStep5.arguments = bundle // Set arguments for the next fragment

            // Navigate to the next step
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, createObituaryStep5)
                .addToBackStack("CreateObituaryStep4") // Add to back stack for navigation
                .commit()
        }

        // Previous button functionality
        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep3())
                .commit()
        }

        return view // Return the inflated view
    }

    // Function to delete media from the list
    private fun deleteMedia(uri: Uri) {
        sharedViewModel.removeMedia(uri) // Remove from SharedViewModel's media list
        mediaAdapter.notifyDataSetChanged() // Refresh adapter to update RecyclerView
    }

    // Function to handle media viewing (for future implementation)
    private fun viewMedia(uri: Uri) {
        // Implement media viewing logic here
        // This could involve starting a new activity or fragment to display the media
        Toast.makeText(requireContext(), "Viewing media: $uri", Toast.LENGTH_SHORT).show()
    }
}