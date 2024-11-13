package com.example.memoreal_prototype

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

class UploadPhotoDialogFragment : DialogFragment() {

    private val PICK_IMAGE_REQUEST_CODE = 1001
    private var imageUri: Uri? = null
    private lateinit var imageView: ImageView
    private lateinit var btnUpload: Button

    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_photo_dialog, container, false)

        imageView = view.findViewById(R.id.imageView)
        val btnSelectImage = view.findViewById<Button>(R.id.btnSelectImage)
        btnUpload = view.findViewById(R.id.btnUploadImage)

        btnSelectImage.setOnClickListener {
            openGallery()
        }

        btnUpload.setOnClickListener {
            if (imageUri != null) {
                // Use the parent fragment's callback to handle the selected image URI
                val parentFragment = parentFragment as? UploadPhotoListener
                parentFragment?.onPhotoUploaded(imageUri!!)
                dismiss()  // Close the dialog
            } else {
                Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)  // Display selected image in ImageView
        }
    }

    interface UploadPhotoListener {
        fun onPhotoUploaded(uri: Uri)
    }
}
