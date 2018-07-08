package com.apps.ahmed_beheiri.healthassistant.Model

class MachineData(heartbeat:String,temperature:String) {
    lateinit var heartbeat:String
    lateinit var temperature:String
    init {
        this.heartbeat=heartbeat
        this.temperature=temperature
    }
}