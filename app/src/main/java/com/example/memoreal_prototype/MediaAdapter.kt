package com.example.memoreal_prototype

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class MediaAdapter(
    private val getMediaList: () -> List<Uri>,
    private val onDeleteClick: (Uri) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(mediaUri: Uri) {
            imageView.setImageURI(mediaUri) // Display the media URI

            // Set up the delete button click listener
            deleteButton.setOnClickListener {
                onDeleteClick(mediaUri) // Call the delete function passed from the fragment
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false) // Use your media item layout
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getMediaList()[position]) // Get the media URI from the current list
    }

    override fun getItemCount(): Int = getMediaList().size // Return the current size
}
