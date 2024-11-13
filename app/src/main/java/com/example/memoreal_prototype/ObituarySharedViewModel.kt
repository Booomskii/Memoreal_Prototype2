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
    val imageUri = MutableLiveData<String?>()
    /*Create Obituary Step 8*/

    fun clearData() {
        // Using postValue to ensure safe thread handling
        memorialCreationFee.postValue(null)
        selectedPackage.postValue(null)
        selectedButtonId.postValue(null)

        fullName.postValue(null)
        dateBirth.postValue(null)
        datePassing.postValue(null)
        biography.postValue(null)
        image.postValue(null)

        mediaList.postValue(ArrayList()) // Reset to empty ArrayList
        familyNames.postValue(ArrayList()) // Reset to empty ArrayList
        familyRelationships.postValue(ArrayList()) // Reset to empty ArrayList

        obituaryText.postValue(null)
        keyEvents.postValue(null)

        funeralDateTime.postValue(null)
        funeralLocation.postValue(null)
        funeralAdtlInfo.postValue(null)

        guestBook.postValue(null)
        privacy.postValue(null)

        backgroundTheme.postValue(null)
        pictureFrame.postValue(null)
        bgMusic.postValue(null)
        virtualCandle.postValue(null)
        virtualFlower.postValue(null)
        favQuote.postValue(null)
    }
}