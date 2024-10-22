package com.example.memoreal_prototype

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _familyMembers = MutableLiveData<MutableList<Pair<String, String>>>(mutableListOf())
    private val _mediaList = mutableListOf<Uri>()
    val familyMembers: LiveData<MutableList<Pair<String, String>>> = _familyMembers
    val mediaList: List<Uri> get() = _mediaList

    fun addMedia(uri: Uri) {
        _mediaList.add(uri)
    }

    fun removeMedia(uri: Uri) {
        _mediaList.remove(uri)
    }

    fun clearMedia() {
        _mediaList.clear()
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
}
