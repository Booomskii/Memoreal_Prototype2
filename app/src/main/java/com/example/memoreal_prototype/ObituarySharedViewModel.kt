package com.example.memoreal_prototype

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ObituarySharedViewModel: ViewModel() {
    /*Create Obituary Step 1*/
    val memorialCreationFee = MutableLiveData<Boolean>()
    val selectedPackage = MutableLiveData<String>()
    val selectedButtonId = MutableLiveData<Int>()
    /*Create Obituary Step 2*/
    /*Create Obituary Step 3*/
    val fullName = MutableLiveData<String>()
    val dateBirth = MutableLiveData<String>()
    val datePassing = MutableLiveData<String>()
    val biography = MutableLiveData<String>()
    val image = MutableLiveData<String>()
    /*Create Obituary Step 4*/
    val mediaList = MutableLiveData<ArrayList<String>>(ArrayList()) // Initialized to an empty ArrayList
    val familyNames = MutableLiveData<ArrayList<String>>(ArrayList()) // Initialized to an empty ArrayList
    val familyRelationships = MutableLiveData<ArrayList<String>>(ArrayList()) // Initialized to an empty ArrayList
    val obituaryText = MutableLiveData<String>()
    val keyEvents = MutableLiveData<String>()
    /*Create Obituary Step 5*/
    val funeralDateTime = MutableLiveData<String>()
    val funeralLocation = MutableLiveData<String>()
    val funeralAdtlInfo = MutableLiveData<String>()
    /*Create Obituary Step 6*/
    val guestBook = MutableLiveData<Boolean>()
    val privacy = MutableLiveData<String>()
    /*Create Obituary Step 7*/
    /*Create Obituary Step 8*/
}