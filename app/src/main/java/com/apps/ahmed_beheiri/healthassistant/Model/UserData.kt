package com.apps.ahmed_beheiri.healthassistant.Model

/**
 * Created by ahmed_beheiri on 05/03/18.
 */
data class UserData(var heartrate:Float,var temp:Float,var status:Int) {
    constructor():this(heartrate = 0f, temp= 0f, status = 0){}
}