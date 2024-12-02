package com.example.memoreal_prototype

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.models.Obituary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class ObituaryAdapterHome(
    private var originalObituaries: List<Obituary>,
    private val onItemClick: (Obituary) -> Unit // Remove onDeleteClick since it's not needed in HomeFragment
) : RecyclerView.Adapter<ObituaryAdapterHome.ObituaryViewHolder>() {

    private var obituaries: List<Obituary> = originalObituaries

    // ViewHolder for home obituary items
    class ObituaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val obituaryPhoto: ImageView = itemView.findViewById(R.id.obituaryPhoto)
        val obituaryName: TextView = itemView.findViewById(R.id.obituaryName)
        val obituaryAge: TextView = itemView.findViewById(R.id.obituaryAge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObituaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_obituary_items, parent, false)
        return ObituaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObituaryViewHolder, position: Int) {
        val obituary = obituaries[position]

        // Safely parse dates for age calculation or display
        val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val desiredFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

        val birthDate = try { originalFormat.parse(obituary.DATEOFBIRTH) } catch (e: Exception) { null }
        val deathDate = try { originalFormat.parse(obituary.DATEOFDEATH) } catch (e: Exception) { null }

        val formattedBirthDate = birthDate?.let { desiredFormat.format(it) }
        val formattedDeathDate = deathDate?.let { desiredFormat.format(it) }

        // Set obituary name
        holder.obituaryName.text = obituary.OBITUARYNAME

        // Set obituary age or date lived
        holder.obituaryAge.text = if (formattedBirthDate != null && formattedDeathDate != null) {
            "$formattedBirthDate ~ $formattedDeathDate"
        } else {
            "Date unavailable"
        }

        // Load image from internal storage
        if (obituary.OBITUARYPHOTO.isNotEmpty()) {
            val bitmap = loadImageFromInternalStorage(obituary.OBITUARYPHOTO)
            if (bitmap != null) {
                holder.obituaryPhoto.setImageBitmap(bitmap)
            } else {
                Log.e("ObituaryAdapterHome", "Failed to load image from path: ${obituary.OBITUARYPHOTO}")
                holder.obituaryPhoto.setImageResource(R.drawable.baseline_person_24)
            }
        } else {
            holder.obituaryPhoto.setImageResource(R.drawable.baseline_person_24)
        }

        // Set item click listener
        holder.itemView.setOnClickListener {
            onItemClick(obituary)
        }
    }

    override fun getItemCount(): Int = obituaries.size

    fun updateObituaries(newObituaries: List<Obituary>) {
        originalObituaries = newObituaries
        obituaries = newObituaries
        notifyDataSetChanged()
    }

    private fun loadImageFromInternalStorage(imagePath: String): Bitmap? {
        return try {
            val cleanPath = imagePath.replace("file://", "")
            val imgFile = File(cleanPath)
            if (imgFile.exists()) {
                BitmapFactory.decodeFile(imgFile.absolutePath)
            } else {
                Log.e("ObituaryAdapterHome", "Image file does not exist at path: $cleanPath")
                null
            }
        } catch (e: Exception) {
            Log.e("ObituaryAdapterHome", "Failed to load image: ${e.message}")
            null
        }
    }
}
