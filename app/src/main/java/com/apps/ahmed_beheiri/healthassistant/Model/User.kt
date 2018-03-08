package com.apps.ahmed_beheiri.healthassistant.Model


/**
 * Created by ahmed_beheiri on 23/02/18.
 */

 data class User(var email:String, var imageuri:String, var username:String, var followers :HashMap<String,User>, var data:UserData, var code :Int) {

    constructor():this (email="",imageuri="", username = "",followers = HashMap<String,User>(),data = UserData(),code = 0)


}