package com.apps.ahmed_beheiri.healthassistant.Model


/**
 * Created by ahmed_beheiri on 23/02/18.
 */

 data class User(var email:String, var imageUri:String, var username:String, var following :HashMap<String,User>, var followers:HashMap<String,String>, var data:UserData, var code :Int) {

    constructor():this (email="",imageUri="", username = "",following = HashMap<String,User>(),data = UserData(),code = 0,followers = HashMap<String,String>())


}