package com.example.memoreal_prototype

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.memoreal_prototype.models.Obituary

class MyObituariesFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl
    private var userId = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var obituaryAdapter: ObituaryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_obituaries, container, false)
        setupToolbar(view)
        recyclerView = view.findViewById(R.id.recyclerView)

        // Initialize with an empty list
        setupRecyclerView(emptyList())

        fetchObituariesByUser()

        return view
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val backButton = toolbar.findViewById<ImageView>(R.id.backButton)

        backButton.visibility = View.GONE
    }

    private fun fetchObituariesByUser() {
        getUserId()
        val url = "$baseUrl"+"api/allObitByUser/"+"$userId"
        Log.d("API", "Requesting URL: $url")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Obituaries", "Failed to fetch obituaries: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to fetch obituaries", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val obituaries = parseObituaries(responseBody)
                        requireActivity().runOnUiThread {
                            obituaryAdapter.updateObituaries(obituaries)
                        }
                    } ?: run {
                        Log.e("Fetch Obituaries", "Response body is null.")
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("Fetch Obituaries", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun getUserId(){
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Initialize EncryptedSharedPreferences
        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "userSession",  // File name
            masterKey,      // Master key for encryption
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        userId = sharedPreferences.getInt("userId", -1)
    }

    private fun parseObituaries(json: String): List<Obituary> {
        val jsonArray = JSONArray(json)
        val obituaries = mutableListOf<Obituary>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            obituaries.add(Obituary(
                jsonObject.getInt("OBITUARYID"),
                jsonObject.getInt("USERID"),
                jsonObject.getInt("GALLERYID"),
                jsonObject.getInt("FAMILYID"),
                jsonObject.getInt("OBITCUSTID"),
                jsonObject.optString("BIOGRAPHY"),
                jsonObject.getString("OBITUARYNAME"),
                jsonObject.getString("OBITUARY_PHOTO"),
                jsonObject.getString("DATEOFBIRTH"),
                jsonObject.getString("DATEOFDEATH"),
                jsonObject.getString("KEYEVENTS"),
                jsonObject.getString("OBITUARYTEXT"),
                jsonObject.optString("FUN_DATETIME"),
                jsonObject.optString("FUN_LOCATION"),
                jsonObject.optString("ADTLINFO"),
                jsonObject.optString("FAVORITEQUOTE"),
                jsonObject.getBoolean("ENAGUESTBOOK"),
                jsonObject.getString("PRIVACY"),
                jsonObject.getString("CREATIONDATE"),
                jsonObject.getString("LASTMODIFIED")
            ))
        }
        return obituaries
    }

    private fun setupRecyclerView(obituaries: List<Obituary>) {
        obituaryAdapter = ObituaryAdapter(
            obituaries,
            onDeleteClick = { obituaryId ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Obituary")
                    .setMessage("Are you sure you want to delete this obituary?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteObituary(obituaryId)
                    }
                    .setNegativeButton("No", null)
                    .show()
            },
            onItemClick = { obituary ->
                openObituaryDetailFragment(obituary.OBITUARYID)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = obituaryAdapter

        // Attach ItemTouchHelper to handle swipe gestures
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun openObituaryDetailFragment(obituaryId: Int) {
        val obituaryFragment = ObituaryFragment()

        // Pass the obituary ID as an argument to the new fragment
        val bundle = Bundle().apply {
            putInt("obituaryId", obituaryId)
        }
        obituaryFragment.arguments = bundle

        // Replace the current fragment with ObituaryDetailFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, obituaryFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun editObituaryDetailFragment(obituaryId: Int) {
        val editObituaryStep3 = EditObituaryStep3()

        // Pass the obituary ID as an argument to the new fragment
        val bundle = Bundle().apply {
            putInt("obituaryId", obituaryId)
        }
        editObituaryStep3.arguments = bundle

        // Replace the current fragment with ObituaryDetailFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, editObituaryStep3)
            .addToBackStack(null)
            .commit()
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val obituary = obituaryAdapter.getObituaryAt(position)

            if (direction == ItemTouchHelper.LEFT) {
                // Show a confirmation dialog before deleting
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Obituary")
                    .setMessage("Are you sure you want to delete this obituary?")
                    .setPositiveButton("Yes") { _, _ ->
                        deleteObituary(obituary.OBITUARYID)
                    }
                    .setNegativeButton("No") { _, _ ->
                        obituaryAdapter.notifyItemChanged(position) // Cancel swipe
                    }
                    .show()
            } else if (direction == ItemTouchHelper.RIGHT) {
                // Handle Edit
                Toast.makeText(requireContext(), "Editing obituary ${obituary.OBITUARYNAME}", Toast.LENGTH_SHORT).show()
                editObituaryDetailFragment(obituary.OBITUARYID)
                obituaryAdapter.notifyItemChanged(position) // Reset swipe
            }
        }

        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                val itemView = viewHolder.itemView
                val paint = Paint()

                // Draw background
                if (dX > 0) {
                    // Swiping to the right (Edit)
                    paint.color = Color.parseColor("#388E3C") // Green color
                    canvas.drawRect(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.left + dX,
                        itemView.bottom.toFloat(),
                        paint
                    )

                    // Draw Edit icon and text
                    val editIcon = ContextCompat.getDrawable(requireContext(), R.drawable
                        .baseline_edit_24)
                    // Use your edit icon drawable
                    val iconMargin = (itemView.height - editIcon!!.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - editIcon.intrinsicHeight) / 2
                    val iconBottom = iconTop + editIcon.intrinsicHeight
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = iconLeft + editIcon.intrinsicWidth
                    editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    editIcon.draw(canvas)

                    paint.color = Color.WHITE
                    paint.textSize = 40f
                    paint.isAntiAlias = true
                    canvas.drawText(
                        "Edit",
                        itemView.left + iconMargin + editIcon.intrinsicWidth + 20f,
                        (itemView.top + itemView.height / 2 + 10).toFloat(),
                        paint
                    )
                } else if (dX < 0) {
                    // Swiping to the left (Delete)
                    paint.color = Color.parseColor("#D32F2F") // Red color
                    canvas.drawRect(
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat(),
                        paint
                    )

                    // Draw Delete icon and text
                    val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable
                        .baseline_delete_24) // Use your delete icon drawable
                    val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight
                    val iconRight = itemView.right - iconMargin
                    val iconLeft = iconRight - deleteIcon.intrinsicWidth
                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(canvas)

                    paint.color = Color.WHITE
                    paint.textSize = 40f
                    paint.isAntiAlias = true
                    canvas.drawText(
                        "Delete",
                        itemView.right - iconMargin - deleteIcon.intrinsicWidth - 120f,
                        (itemView.top + itemView.height / 2 + 10).toFloat(),
                        paint
                    )
                }

                // Fade the view out during the swipe
                val alpha = 1.0f - Math.abs(dX) / recyclerView.width
                itemView.alpha = alpha
                itemView.translationX = dX
            } else {
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    private fun deleteObituary(obituaryId: Int) {
        val url = "$baseUrl"+"api/deleteObituary/$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .method("DELETE", null)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Delete Obituary", "Failed to delete obituary: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to delete obituary", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Obituary deleted successfully", Toast.LENGTH_SHORT).show()
                        // Refresh the list of obituaries
                        fetchObituariesByUser()
                    }
                } else {
                    Log.e("Delete Obituary", "Error: ${response.code} - ${response.message}")
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
