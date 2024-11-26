package com.example.memoreal_prototype

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Step4SharedViewModel2 : ViewModel() {

    private val _familyMembers = MutableLiveData<MutableList<Pair<String, String>>>(mutableListOf())
    private val _mediaList = MutableLiveData<MutableList<Uri>>(mutableListOf())

    val familyMembers: LiveData<MutableList<Pair<String, String>>> = _familyMembers
    val mediaList: LiveData<MutableList<Uri>> = _mediaList

    // To track the current obituary ID
    var currentObituaryId: Int? = null

    // Flag to indicate whether the gallery has already been fetched for the current obituary
    private var galleryFetched: Boolean = false

    // To track the current family ID
    var currentFamilyId: Int? = null

    // Flag to indicate whether the family has already been fetched for the current familyId
    private var familyFetched: Boolean = false

    fun addMedia(uri: Uri) {
        _mediaList.value?.add(uri)
        _mediaList.value = _mediaList.value // Trigger observer update
    }

    fun removeMedia(uri: Uri) {
        _mediaList.value?.remove(uri)
        _mediaList.value = _mediaList.value // Trigger observer update
    }

    fun clearMedia() {
        _mediaList.value?.clear()
        _mediaList.value = _mediaList.value // Trigger observer update
    }

    fun addFamilyMember(name: String, relationship: String) {
        _familyMembers.value?.add(Pair(name, relationship))
        _familyMembers.value = _familyMembers.value // Trigger observer update
    }

    fun removeFamilyMember(index: Int) {
        _familyMembers.value?.removeAt(index)
        _familyMembers.value = _familyMembers.value // Trigger observer update
    }

    fun getFamilyMembers(): List<Pair<String, String>> {
        return _familyMembers.value ?: listOf()
    }

    // To set the current obituary ID and manage re-fetching the gallery
    fun setCurrentObituaryId(obituaryId: Int) {
        if (currentObituaryId != obituaryId) {
            currentObituaryId = obituaryId
            galleryFetched = false // Reset galleryFetched since this is a new obituary ID
            clearMedia() // Clear previous media since we're editing a new obituary
        }
    }

    // To check if the gallery has already been fetched for the current obituary
    fun isGalleryFetched(): Boolean {
        return galleryFetched
    }

    // To set the galleryFetched flag to true once the gallery is fetched
    fun setGalleryFetched(fetched: Boolean) {
        galleryFetched = fetched
    }

    // To set the current family ID and manage re-fetching the family data
    fun setCurrentFamilyId(familyId: Int) {
        if (currentFamilyId != familyId) {
            currentFamilyId = familyId
            familyFetched = false // Reset familyFetched since this is a new family ID
            clearFamilyMembers() // Clear previous family members since we're editing a new family
        }
    }

    // To check if the family data has already been fetched for the current family ID
    fun isFamilyFetched(): Boolean {
        return familyFetched
    }

    // To set the familyFetched flag to true once the family data is fetched
    fun setFamilyFetched(fetched: Boolean) {
        familyFetched = fetched
    }

    // Method to clear family members when switching to a new family
    private fun clearFamilyMembers() {
        _familyMembers.value?.clear()
        _familyMembers.value = _familyMembers.value // Trigger observer update
    }
}
