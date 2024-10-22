package com.example.memoreal_prototype.models

import org.w3c.dom.Text

data class User(
    val USERID:Int,
    val FIRST_NAME:String? = null,
    var LAST_NAME:String? = null,
    var MI:String? = null,
    var USERNAME:String,
    var CONTACT_NUMBER:String? = null,
    var EMAIL:String,
    var BIRTHDATE:String? = null,
    var PICTURE:String? = null,
    val HASHED_PASSWORD: String
)

data class Obituary(
    var obituaryID:Int,
    var userID:Int,
    var biography: Text,
    var obituaryName:String,
    var dateOfBirth:String,
    var dateOfDeath:String,
    var keyEvents:String,
    var picture:String? = null,
    var achievements:String? = null,
    var favoriteQuotes:String? = null,
    var creationDate: Long,
    var lastModified: Long
)

data class Payment(
    var paymentID:Int,
    var userID:Int,
    var obituaryID: Int,
    var amount: Double,
    var paymentDate: String,
    var paymentMethod: String,
    var paymentStatus: String
)
