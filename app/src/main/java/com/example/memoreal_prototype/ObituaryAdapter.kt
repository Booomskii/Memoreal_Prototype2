package com.example.memoreal_prototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.models.Obituary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ObituaryAdapter(
    private var originalObituaries: List<Obituary>,
    private val onDeleteClick: (Int) -> Unit = { },
    private val onItemClick: (Obituary) -> Unit // Add this callback to handle item click
) : RecyclerView.Adapter<ObituaryAdapter.ObituaryViewHolder>() {

    private var obituaries: List<Obituary> = originalObituaries

    class ObituaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val obituaryPhoto: ImageView = itemView.findViewById(R.id.obituaryPhoto)
        val obituaryName: TextView = itemView.findViewById(R.id.obituaryName)
        val dateLived: TextView = itemView.findViewById(R.id.dateLived)
        val funLocation: TextView = itemView.findViewById(R.id.funLocation)
        val createDate: TextView = itemView.findViewById(R.id.createDate)
        val itemContainer: View = itemView.findViewById(R.id.itemContainer) // Add this to reference the whole layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObituaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.obituary_items, parent, false)
        return ObituaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObituaryViewHolder, position: Int) {
        val obituary = obituaries[position]
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date1 = originalFormat.parse(obituary.DATEOFBIRTH)
        val date2 = originalFormat.parse(obituary.DATEOFDEATH)
        val date3 = originalFormat.parse(obituary.CREATIONDATE)
        val desiredFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
        val formattedBirthDate = desiredFormat.format(date1)
        val formattedDeathDate = desiredFormat.format(date2)
        val formattedCreationDate = desiredFormat.format(date3)

        holder.obituaryName.text = obituary.OBITUARYNAME
        holder.dateLived.text = "${formattedBirthDate} ~ ${formattedDeathDate}"
        holder.funLocation.text = obituary.FUNLOCATION
        holder.createDate.text = formattedCreationDate

        if (obituary.OBITUARYPHOTO.isNotEmpty()) {
            val bitmap = loadImageFromInternalStorage(obituary.OBITUARYPHOTO)
            if (bitmap != null) {
                holder.obituaryPhoto.setImageBitmap(bitmap)
            } else {
                Log.e("ObituaryAdapter", "Failed to load image from path: ${obituary.OBITUARYPHOTO}")
                holder.obituaryPhoto.setImageResource(R.drawable.baseline_person_24)
            }
        } else {
            holder.obituaryPhoto.setImageResource(R.drawable.baseline_person_24)
        }

        // Set item click listener
        holder.itemContainer.setOnClickListener {
            onItemClick(obituary)
        }
    }

    override fun getItemCount(): Int = obituaries.size

    fun updateObituaries(newObituaries: List<Obituary>) {
        originalObituaries = newObituaries
        obituaries = newObituaries
        notifyDataSetChanged()
    }

    // Utility function to get an obituary by its position
    fun getObituaryAt(position: Int): Obituary {
        return obituaries[position]
    }

    fun filter(query: String) {
        obituaries = if (query.isEmpty()) {
            originalObituaries
        } else {
            originalObituaries.filter { obituary ->
                obituary.OBITUARYNAME.contains(query, ignoreCase = true) ||
                        obituary.BIOGRAPHY.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    private fun loadImageFromInternalStorage(imagePath: String): Bitmap? {
        return try {
            val cleanPath = imagePath.replace("file://", "")
            val imgFile = File(cleanPath)
            if (imgFile.exists()) {
                BitmapFactory.decodeFile(imgFile.absolutePath)
            } else {
                Log.e("ObituaryAdapter", "Image file does not exist at path: $cleanPath")
                null
            }
        } catch (e: Exception) {
            Log.e("ObituaryAdapter", "Failed to load image: ${e.message}")
            null
        }
    }
}

