package com.example.memoreal_prototype

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.models.Obituary

class ObituaryAdapter(
    private var obituaries: List<Obituary>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ObituaryAdapter.ObituaryViewHolder>() {

    class ObituaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val obituaryPhoto: ImageView = itemView.findViewById(R.id.obituaryPhoto)
        val obituaryName: TextView = itemView.findViewById(R.id.obituaryName)
        val biography: TextView = itemView.findViewById(R.id.biography)
        val dateBirth: TextView = itemView.findViewById(R.id.dateBirth)
        val dateDeath: TextView = itemView.findViewById(R.id.dateDeath)
        val keyEvents: TextView = itemView.findViewById(R.id.keyEvents)
        val obitText: TextView = itemView.findViewById(R.id.obitText)
        val funDateTime: TextView = itemView.findViewById(R.id.funDateTime)
        val funLocation: TextView = itemView.findViewById(R.id.funLocation)
        val adtlInfo: TextView = itemView.findViewById(R.id.adtlInfo)
        val favQuote: TextView = itemView.findViewById(R.id.favQuote)
        val privacy: TextView = itemView.findViewById(R.id.privacy)
        val guestBook: TextView = itemView.findViewById(R.id.guestBook)
        val createDate: TextView = itemView.findViewById(R.id.createDate)
        val lastModified: TextView = itemView.findViewById(R.id.lastModified)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObituaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.obituary_items, parent, false)
        return ObituaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObituaryViewHolder, position: Int) {
        val obituary = obituaries[position]
        holder.obituaryName.text = obituary.OBITUARYNAME
        holder.biography.text = obituary.BIOGRAPHY
        holder.dateBirth.text = obituary.DATEOFBIRTH
        holder.dateDeath.text = obituary.DATEOFDEATH
        holder.keyEvents.text = obituary.KEYEVENTS
        holder.obitText.text = obituary.OBITUARYTEXT
        holder.funDateTime.text = obituary.FUNDATETIME
        holder.funLocation.text = obituary.FUNLOCATION
        holder.adtlInfo.text = obituary.ADTLINFO
        holder.favQuote.text = obituary.FAVORITEQUOTE
        holder.privacy.text = obituary.PRIVACY
        holder.guestBook.text = obituary.ENAGUESTBOOK.toString()
        holder.createDate.text = obituary.CREATIONDATE
        holder.lastModified.text = obituary.LASTMODIFIED

        holder.deleteButton.setOnClickListener {
            onDeleteClick(obituary.OBITUARYID)
        }
    }

    override fun getItemCount(): Int = obituaries.size

    fun updateObituaries(newObituaries: List<Obituary>) {
        obituaries = newObituaries
        notifyDataSetChanged()
    }
}