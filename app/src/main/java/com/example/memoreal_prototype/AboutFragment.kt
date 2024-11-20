package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import com.example.memoreal_prototype.models.Obituary
import com.example.memoreal_prototype.models.Obituary_Customization
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class AboutFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private var fetchedObituary: Obituary? = null
    private var biographyTextView: TextView? = null
    private var lifeEventsTextView: TextView? = null
    private var quoteTextView: TextView? = null
    private var funDateTimeTextView: TextView? = null
    private var funLocationTextView: TextView? = null
    private var funInfoTextView: TextView? = null
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        biographyTextView = view.findViewById(R.id.tvBio)
        lifeEventsTextView = view.findViewById(R.id.tvLifeEvents)
        quoteTextView = view.findViewById(R.id.tvQuote)
        funDateTimeTextView = view.findViewById(R.id.tvFunDateTime)
        funLocationTextView = view.findViewById(R.id.tvFunLocation)
        funInfoTextView = view.findViewById(R.id.tvFunInfo)

        sharedViewModel.obituaryId.observe(viewLifecycleOwner) { id ->
            if (id != null) {
                fetchObituaryById(id)
            }
        }

        return view
    }

    private fun fetchObituaryById(obituaryId: Int) {
        val url = "$baseUrl" + "api/fetchObit/$obituaryId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Obituary", "Failed to fetch obituary: ${e.message}")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to fetch obituary", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonObject = JSONObject(responseBody)
                        val obitCustIdArray = jsonObject.getJSONArray("OBITCUSTID")
                        val obitCustId = obitCustIdArray.getInt(0)

                        fetchedObituary = Obituary(
                            OBITUARYID = jsonObject.getInt("OBITUARYID"),
                            USERID = jsonObject.getInt("USERID"),
                            GALLERYID = jsonObject.getInt("GALLERYID"),
                            OBITCUSTID = obitCustId,
                            FAMILYID = jsonObject.getInt("FAMILYID"),
                            BIOGRAPHY = jsonObject.optString("BIOGRAPHY"),
                            OBITUARYNAME = jsonObject.getString("OBITUARYNAME"),
                            OBITUARYPHOTO = jsonObject.getString("OBITUARY_PHOTO"),
                            DATEOFBIRTH = jsonObject.getString("DATEOFBIRTH"),
                            DATEOFDEATH = jsonObject.getString("DATEOFDEATH"),
                            OBITUARYTEXT = jsonObject.optString("OBITUARYTEXT"),
                            KEYEVENTS = jsonObject.optString("KEYEVENTS"),
                            FUNDATETIME = jsonObject.optString("FUN_DATETIME"),
                            FUNLOCATION = jsonObject.optString("FUN_LOCATION"),
                            ADTLINFO = jsonObject.optString("ADTLINFO"),
                            PRIVACY = jsonObject.getString("PRIVACY"),
                            ENAGUESTBOOK = jsonObject.getBoolean("ENAGUESTBOOK"),
                            FAVORITEQUOTE = jsonObject.optString("FAVORITEQUOTE"),
                            CREATIONDATE = jsonObject.getString("CREATIONDATE"),
                            LASTMODIFIED = jsonObject.getString("LASTMODIFIED")
                        )

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                // Update the UI with fetched data
                                fetchedObituary?.let {
                                    val originalFormat = SimpleDateFormat("yyyy-dd-MM'T'HH:mm:ss" +
                                            ".SSS'Z'", Locale.getDefault())
                                    val date = originalFormat.parse(it.FUNDATETIME!!)
                                    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                                    val timeFormat = SimpleDateFormat("ha", Locale.getDefault())
                                    val formattedDate = date?.let { dateFormat.format(it) }
                                    val formattedDate2 = date?.let { timeFormat.format(it) }
                                    biographyTextView?.text = it.BIOGRAPHY
                                    lifeEventsTextView?.text = it.KEYEVENTS
                                    quoteTextView?.text = it.FAVORITEQUOTE
                                    funDateTimeTextView?.text = formattedDate + " " + formattedDate2
                                    funLocationTextView?.text = it.FUNLOCATION
                                    funInfoTextView?.text = it.ADTLINFO
                                }
                            }
                        }
                    } ?: run {
                        Log.e("Fetch Obituary", "Response body is null.")
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch Obituary", "Error: ${response.code} - ${response.message}")
                    if (isAdded) {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
