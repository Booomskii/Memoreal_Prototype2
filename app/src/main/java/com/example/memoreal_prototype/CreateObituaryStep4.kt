package com.example.memoreal_prototype

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
import com.example.memoreal_prototype.MediaAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class CreateObituaryStep4 : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var recyclerView: RecyclerView
    private val mediaList = mutableListOf<Uri>()

    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            it.forEach { uri -> sharedViewModel.addMedia(uri) }
            mediaAdapter.notifyDataSetChanged() // Refresh adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_obituary_step4, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewMedia) // Make sure this ID matches your layout
        mediaAdapter = MediaAdapter(
            getMediaList = { sharedViewModel.mediaList }, // Provide a function to get the current media list
            onDeleteClick = { uri -> deleteMedia(uri) }
        )
        recyclerView.adapter = mediaAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val addFamily = view.findViewById<Button>(R.id.btnAddFamily)
        val addMedia = view.findViewById<ImageView>(R.id.btnAddMedia)
        val obitText = view.findViewById<EditText>(R.id.etObituaryText)
        val keyEvents = view.findViewById<EditText>(R.id.etKeyEvents)

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .commit()
        }

        addFamily.setOnClickListener {
            AddFamilyDialogFragment().show((activity as AppCompatActivity).supportFragmentManager, "openFamilyDialog")
        }

        addMedia.setOnClickListener {
            pickMediaLauncher.launch("image/* video/*")
        }

        nextButton.setOnClickListener {
            Log.d("FamilyMembers", "Family members: ${sharedViewModel.getFamilyMembers()}")
            val mediaUriStrings = sharedViewModel.mediaList.map { it.toString() }

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

            Log.d("Media List: ", "$mediaUriStrings")
            Log.d("Family Names: ", "$familyNames")
            Log.d("Family Relationships: ", "$familyRelationships")
            Log.d("Obituary Text: ", "${obitText.text}")
            Log.d("Key Events: ", "${keyEvents.text}")

            val createObituaryStep4 = CreateObituaryStep4()
            createObituaryStep4.arguments = bundle

            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep5())
                .addToBackStack("CreateObituaryStep4")
                .commit()
        }

        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep3())
                .commit()
        }

        return view
    }

    private fun deleteMedia(uri: Uri) {
        sharedViewModel.removeMedia(uri) // Remove from SharedViewModel's media list
        mediaAdapter.notifyDataSetChanged() // Refresh adapter to update RecyclerView
    }
}
