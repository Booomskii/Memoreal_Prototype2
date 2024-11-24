package com.example.memoreal_prototype.models

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
    var OBITUARYID:Int,
    var USERID:Int,
    var GALLERYID:Int,
    var OBITCUSTID:Int,
    var FAMILYID:Int,
    var BIOGRAPHY: String,
    var OBITUARYNAME:String,
    var OBITUARYPHOTO:String,
    var DATEOFBIRTH:String,
    var DATEOFDEATH:String,
    var OBITUARYTEXT:String? = null,
    var KEYEVENTS:String? = null,
    var FUNDATETIME:String? = null,
    var FUNLOCATION:String? = null,
    var ADTLINFO:String? = null,
    var PRIVACY:String,
    var ENAGUESTBOOK:Boolean,
    var FAVORITEQUOTE:String? = null,
    var CREATIONDATE: String,
    var LASTMODIFIED: String
)

data class Obituary_Customization(
    var OBITCUSTID:Int,
    var BGTHEME:String,
    var PICFRAME:String,
    var BGMUSIC:String,
    var VFLOWER:String,
    var VCANDLE:String,
)

data class Family(
    var FAMILYID:Int,
    var DATECREATED:String
)

data class FamilyMembers(
    var MEMBERSID:Int,
    var FAMILYID:Int,
    var MEMBERNAME:String? = null,
    var RELATIONSHIP:String? = null
)

data class Gallery(
    var GALLERYID:Int,
    var GALLERYMEDIAID:Int,
    var CREATIONDATE: String,
    var MODIFIEDDATE: String
)

data class GalleryMedia(
    var GALLERYMEDIAID:Int,
    var GALLERYID:Int,
    var MEDIATYPE:String,
    var FILENAME: String,
    var UPLOADDATE: String
)

data class Payment(
    var PAYMENTID:Int,
    var USERID:Int,
    var OBITUARYID: Int,
    var AMOUNT: Double,
    var PAYMENTDATE: String,
    var PAYMENTMETHOD: String,
    var PAYMENTSTATUS: String
)

data class Guestbook(
    var GUESTBOOKID:Int,
    var USERID:Int? = null,
    var OBITUARYID:Int,
    var GUESTNAME:String,
    var MESSAGE:String? = null,
    var POSTINGDATE:String,
    var PROFILEPICTURE:String? = null,
    var FULLNAME:String
)

data class Tribute(
    var TRIBUTEID:Int,
    var USERID:Int? = null,
    var OBITUARYID:Int,
    var OFFEREDFLOWER:String? = null,
    var LIGHTEDCANDLE:String? = null,
    var OFFERINGDATE:String
)