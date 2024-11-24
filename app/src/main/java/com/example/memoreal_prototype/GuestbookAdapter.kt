package com.example.memoreal_prototype.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoreal_prototype.R
import com.example.memoreal_prototype.models.Guestbook
import java.text.SimpleDateFormat
import java.util.Locale

class GuestbookAdapter(private val guestbookList: List<Guestbook>) :
    RecyclerView.Adapter<GuestbookAdapter.GuestbookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestbookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guestbook_entry, parent, false)
        return GuestbookViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuestbookViewHolder, position: Int) {
        val guestbookEntry = guestbookList[position]
        holder.guestNameTextView.text = guestbookEntry.GUESTNAME
        holder.messageTextView.text = guestbookEntry.MESSAGE

        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = originalFormat.parse(guestbookEntry.POSTINGDATE)
        val desiredFormat = SimpleDateFormat("MMM. dd, yyyy h:mm a", Locale.getDefault())
        val formattedDate = date?.let { desiredFormat.format(it) }

        holder.postingDateTextView.text = formattedDate

        // Load profile picture using Glide
        if (!guestbookEntry.PROFILEPICTURE.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(guestbookEntry.PROFILEPICTURE)
                .placeholder(R.drawable.baseline_person_24) // Placeholder image
                .error(R.drawable.baseline_person_24) // Error image if loading fails
                .circleCrop() // Crop the image in a circle if needed
                .into(holder.profileImageView)
        } else {
            // Use a default profile picture if none is provided
            holder.profileImageView.setImageResource(R.drawable.baseline_person_24)
        }
    }

    override fun getItemCount(): Int {
        return guestbookList.size
    }

    inner class GuestbookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val guestNameTextView: TextView = itemView.findViewById(R.id.guestNameTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val postingDateTextView: TextView = itemView.findViewById(R.id.postingDateTextView)
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
    }
}

