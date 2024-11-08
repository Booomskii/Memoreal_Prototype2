package com.example.memoreal_prototype

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ObituarySharedViewModel: ViewModel() {
    /*Create Obituary Step 1*/
    val memorialCreationFee = MutableLiveData<Boolean?>()
    val selectedPackage = MutableLiveData<String?>()
    val selectedButtonId = MutableLiveData<Int?>()
    /*Create Obituary Step 2*/
    /*Create Obituary Step 3*/
    val fullName = MutableLiveData<String?>()
    val dateBirth = MutableLiveData<String?>()
    val datePassing = MutableLiveData<String?>()
    val biography = MutableLiveData<String?>()
    val image = MutableLiveData<String?>()
    /*Create Obituary Step 4*/
    val mediaList = MutableLiveData<ArrayList<String>>(ArrayList()) // Initialized to an empty ArrayList
    val familyNames = MutableLiveData<ArrayList<String>>(ArrayList()) // Initialized to an empty ArrayList
    val familyRelationships = MutableLiveData<ArrayList<String>>(ArrayList()) // Initialized to an empty ArrayList
    val obituaryText = MutableLiveData<String?>()
    val keyEvents = MutableLiveData<String?>()
    /*Create Obituary Step 5*/
    val funeralDateTime = MutableLiveData<String?>()
    val funeralLocation = MutableLiveData<String?>()
    val funeralAdtlInfo = MutableLiveData<String?>()
    /*Create Obituary Step 6*/
    val guestBook = MutableLiveData<Boolean?>()
    val privacy = MutableLiveData<String?>()
    /*Create Obituary Step 7*/
    val backgroundTheme = MutableLiveData<String?>()
    val pictureFrame = MutableLiveData<Pair<Int, String>?>()
    val bgMusic = MutableLiveData<String?>()
    val virtualCandle = MutableLiveData<Pair<Int, String>?>()
    val virtualFlower = MutableLiveData<Pair<Int, String>?>()
    val favQuote = MutableLiveData<String?>()
    /*Create Obituary Step 8*/

    fun clearData() {
        memorialCreationFee.value = null
        selectedPackage.value = null
        selectedButtonId.value = null
        fullName.value = null
        dateBirth.value = null
        datePassing.value = null
        biography.value = null
        image.value = null
        mediaList.value = ArrayList() // Reset to empty ArrayList
        familyNames.value = ArrayList() // Reset to empty ArrayList
        familyRelationships.value = ArrayList() // Reset to empty ArrayList
        obituaryText.value = null
        keyEvents.value = null
        funeralDateTime.value = null
        funeralLocation.value = null
        funeralAdtlInfo.value = null
        guestBook.value = null
        privacy.value = null
        backgroundTheme.value = null
        pictureFrame.value = null
        bgMusic.value = null
        virtualCandle.value = null
        virtualFlower.value = null
        favQuote.value = null
    }
}