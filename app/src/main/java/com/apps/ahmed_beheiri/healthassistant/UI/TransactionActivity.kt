package com.apps.ahmed_beheiri.healthassistant.UI

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.apps.ahmed_beheiri.healthassistant.Model.Contract
import com.apps.ahmed_beheiri.healthassistant.R
import com.google.firebase.auth.FirebaseUser

class TransactionActivity : AppCompatActivity() {


    lateinit var user:FirebaseUser
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDeviceArrayAdapter: ArrayAdapter<String>
    private lateinit var userid:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        userid= intent.getStringExtra(Contract.EXTRA_USER_VALUE1)
        Log.d("UserValue",userid)
    }

    override fun onResume() {
        super.onResume()
        checkBTState(this)
        var buildersingle: AlertDialog.Builder= AlertDialog.Builder(this)

        buildersingle.setTitle("Choose Device to connect to (usualy HC-05) ..")

        pairedDeviceArrayAdapter = ArrayAdapter(this, R.layout.device_list)
        buildersingle.setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        buildersingle.setAdapter(pairedDeviceArrayAdapter, DialogInterface.OnClickListener { dialog, which ->
            var str:String=pairedDeviceArrayAdapter.getItem(which)
            val address = str.substring(str.length - 17)
            val intent = Intent(applicationContext, ProfileActivity::class.java)
            intent.putExtra(Contract.EXTRA_DEVICE_ADDRESS, address)
            intent.putExtra(Contract.EXTRA_USER_VALUE2,userid)
            startActivity(intent)


        })
        buildersingle.show()


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val paireddevices = bluetoothAdapter.bondedDevices
        if (paireddevices.size > 0) {
            for (device in paireddevices) {
                pairedDeviceArrayAdapter.add(device.name + "\n" + device.address)
            }
        } else {

            pairedDeviceArrayAdapter.add("No Device Found")
        }
    }


     fun checkBTState(context:Context) {
         // Check device has Bluetooth and that it is turned on
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() // CHECK THIS OUT THAT IT WORKS!!!
         if (bluetoothAdapter == null) {
             Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show()
         } else {
             if (bluetoothAdapter.isEnabled) {
                 Log.d("Enabled", "...Bluetooth ON...")
             } else {
                 //Prompt user to turn on Bluetooth
                 val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                 startActivityForResult(enableBtIntent, 1)

             }
         }
     }



}
