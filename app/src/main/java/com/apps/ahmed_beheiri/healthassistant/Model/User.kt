package com.apps.ahmed_beheiri.healthassistant.Model

/**
 * Created by ahmed_beheiri on 23/02/18.
 */
data class User(var email:String,var imageuri:String,var username:String) {
    constructor(email:String, imageuri:String, username:String,followers :ArrayList<User>) : this(email,imageuri, username)

}