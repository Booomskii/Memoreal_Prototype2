package com.example.memoreal_prototype

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.memoreal_prototype.models.FamilyMembers
import com.example.memoreal_prototype.models.Obituary
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FamilyFragment : Fragment() {

    private val client = UserSession.client
    private val baseUrl = UserSession.baseUrl

    private var fetchedObituary: Obituary? = null
    private var fetchedFamily: FamilyMembers? = null
    private var memberTextView: TextView? = null
    private var relationshipTextView: TextView? = null
    private lateinit var familyContainerLayout: LinearLayout
    private val sharedViewModel: ObituarySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_family, container, false)
        memberTextView = view.findViewById(R.id.tvMemberName)
        relationshipTextView = view.findViewById(R.id.tvRelationship)

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
                                    fetchFamilyMembers(it.FAMILYID)
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

    private fun fetchFamilyMembers(membersId: Int) {
        val url = "$baseUrl" + "api/fetchFamily/$membersId"
        Log.d("API", "Requesting URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Fetch Family", "Failed to fetch family: ${e.message}")
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(context, "Failed to fetch family", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                familyContainerLayout = view?.findViewById(R.id.familyContainerLayout)!!
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val familyMembersArray = JSONArray(responseBody)

                        // To avoid adding duplicate views, clear the container first
                        requireActivity().runOnUiThread {
                            familyContainerLayout.removeAllViews()
                        }

                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                // Loop through the family members array and add each member to the UI
                                for (i in 0 until familyMembersArray.length()) {
                                    val familyMemberJson = familyMembersArray.getJSONObject(i)

                                    val memberName = familyMemberJson.getString("MEMBERNAME")
                                    val relationship = familyMemberJson.getString("RELATIONSHIP")

                                    // Inflate the layout for each family member
                                    val memberView = LayoutInflater.from(requireContext())
                                        .inflate(R.layout.family_member_item, familyContainerLayout, false)

                                    // Set the data
                                    val memberTextView = memberView.findViewById<TextView>(R.id.tvMemberName)
                                    val relationshipTextView = memberView.findViewById<TextView>(R.id.tvRelationship)

                                    memberTextView.text = memberName
                                    relationshipTextView.text = relationship

                                    // Add the created view to the container layout
                                    familyContainerLayout.addView(memberView)
                                }
                            }
                        }
                    } ?: run {
                        Log.e("Fetch Family", "Response body is null.")
                        if (isAdded) {
                            requireActivity().runOnUiThread {
                                Toast.makeText(context, "Error: Empty response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Log.e("Fetch Family", "Error: ${response.code} - ${response.message}")
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