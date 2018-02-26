package com.apps.ahmed_beheiri.healthassistant.Model

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by ahmed_beheiri on 23/02/18.
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class User( var email:String, var imageuri:String, var username:String): Parcelable {

    constructor(email:String, imageuri:String, username:String,followers :ArrayList<User>) : this(email,imageuri,username)
    constructor():this(email="",imageuri="", username = "")


}