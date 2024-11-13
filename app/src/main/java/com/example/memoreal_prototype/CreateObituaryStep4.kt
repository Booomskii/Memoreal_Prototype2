package com.example.memoreal_prototype

import MediaAdapter
import android.graphics.Rect
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateObituaryStep4 : Fragment() {

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()
    private val sharedViewModel2: Step4SharedViewModel by activityViewModels()
    private lateinit var mediaAdapter: MediaAdapter
    private lateinit var recyclerView: RecyclerView

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
        val view = inflater.inflate(R.layout.fragment_create_obituary_step4, container, false)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)
        val nextButton = view.findViewById<Button>(R.id.btnNext)
        val prevButton = view.findViewById<Button>(R.id.btnPrev)
        val addFamily = view.findViewById<Button>(R.id.btnAddFamily)
        val obitTextET = view.findViewById<EditText>(R.id.etObituaryText)
        val keyEventsET = view.findViewById<EditText>(R.id.etKeyEvents)

        sharedViewModel.obituaryText.observe(viewLifecycleOwner) { obituaryText ->
            obitTextET.setText(obituaryText)
        }

        sharedViewModel.keyEvents.observe(viewLifecycleOwner) { keyEvents ->
            keyEventsET.setText(keyEvents)
        }

        backButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, HomeFragment())
                .commit()
        }

        addFamily.setOnClickListener {
            AddFamilyDialogFragment().show((activity as AppCompatActivity).supportFragmentManager, "openFamilyDialog")
        }

        recyclerView = view.findViewById(R.id.recyclerViewMedia)
        mediaAdapter = MediaAdapter(
            context = requireContext(),
            fragmentManager = childFragmentManager, // Pass fragment manager to support dialog display
            getMediaList = { sharedViewModel2.mediaList },
            onDeleteClick = { uri -> deleteMedia(uri) },
            onMediaClick = { uri -> viewMedia(uri) } // Pass the function for media click
        )
        recyclerView.adapter = mediaAdapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.addItemDecoration(
            GridSpacingItemDecoration(3, 24, true) // Increase spacing from 16dp to 24dp
        )

        val addMedia = view.findViewById<ImageView>(R.id.btnAddMedia)
        addMedia.setOnClickListener {
            pickMediaLauncher.launch("image/* video/*")
        }

        nextButton.setOnClickListener {
            Log.d("FamilyMembers", "Family members: ${sharedViewModel2.getFamilyMembers()}")
            val mediaUriStrings = sharedViewModel2.mediaList.map { it.toString() }

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

            // Navigate to the next step
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep5())
                .addToBackStack("CreateObituaryStep4") // Add to back stack for navigation
                .commit()
        }

        // Previous button functionality
        prevButton.setOnClickListener {
            (activity as HomePageActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, CreateObituaryStep3())
                .commit()
        }

        return view
    }

    // Function to delete media from the list
    private fun deleteMedia(uri: Uri) {
        sharedViewModel2.removeMedia(uri)
        mediaAdapter.notifyDataSetChanged()
    }

    // Function to handle media viewing (for future implementation)
    private fun viewMedia(uri: Uri) {
        val dialogFragment = MediaPreviewDialogFragment.newInstance(uri)
        dialogFragment.show(childFragmentManager, "MediaPreviewDialog") // Use childFragmentManager for fragments inside other fragments
    }

    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view) // item position
            val column = position % spanCount // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) { // top edge
                    outRect.top = spacing
                }
                outRect.bottom = spacing // item bottom
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing // item top
                }
            }
        }
    }
}
