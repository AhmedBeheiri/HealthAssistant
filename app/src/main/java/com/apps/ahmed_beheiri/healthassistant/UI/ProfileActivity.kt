package com.apps.ahmed_beheiri.healthassistant.UI

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.AppBarLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.apps.ahmed_beheiri.healthassistant.Model.Contract
import com.apps.ahmed_beheiri.healthassistant.Model.User
import com.apps.ahmed_beheiri.healthassistant.Model.UserData
import com.apps.ahmed_beheiri.healthassistant.R
import com.apps.ahmed_beheiri.healthassistant.Utlies.MachineNetwork
import com.apps.ahmed_beheiri.healthassistant.Utlies.NotificationNetwork
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.onesignal.OneSignal
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    internal lateinit var bluetoothin: Handler
    internal val handlerstate = 0
    private lateinit var adapter: BluetoothAdapter
    private var socket: BluetoothSocket? = null
    private val stringBuilder = StringBuilder()
    private lateinit var connectedThread: ConnectedThread
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var address: String? = null

    lateinit var user: FirebaseUser
    lateinit var mAuth: FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var provider: String
    lateinit var storageRef: StorageReference
    lateinit var mgoogleSignInClient: GoogleSignInClient
    lateinit var data: UserData
    lateinit var myuser: User
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private var isHideToolbarView: Boolean = false
    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        ButterKnife.bind(this)

        myuser = User()
        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail().build()

        mgoogleSignInClient = GoogleSignIn.getClient(this, gso)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        user = mAuth.currentUser!!
        var databaseReference: DatabaseReference = database.getReference("users").child(user.uid)

        storageRef = storage.getReference("users")
        Log.d("Provider", user.providers!![0].toString())
        provider = user.providers!![0].toString()
        if (provider.equals("facebook.com") || provider.equals("google.com")) {
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(value: DataSnapshot?) {
                    var myuser: User? = value?.getValue(User::class.java)
                    Picasso.with(this@ProfileActivity).load(myuser!!.imageUri).placeholder(R.drawable.index).into(image)
                    if (value?.child("followers")?.exists()!!) {
                        initUi(myuser!!.username, myuser!!.followers.size)
                    } else {
                        initUi(myuser!!.username, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError?) {
                    Log.d("Database Error", "error retriving data : " + error?.message)
                }
            })
        } else {


            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(value: DataSnapshot?) {
                    var myuser: User? = value?.getValue(User::class.java)

                    loadimage()
                    if (value?.child("followers")?.exists()!!) {
                        initUi(myuser!!.username, myuser!!.followers.size)
                    } else {
                        initUi(myuser!!.username, 0)
                    }
                }

                override fun onCancelled(error: DatabaseError?) {
                    Log.d("Database Error", "error retriving data : " + error?.message)
                }
            })

        }

        setSupportActionBar(profiletoolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        //initUi()
        bluetoothin = object : Handler() {
            override fun handleMessage(msg: Message) {
                var heartbeat:String=""
                var temp:String=""
                if (msg.what == handlerstate) {                                     //if message is what we want
                    val readMessage = msg.obj as String
                    // msg.arg1 = bytes from connect thread
                    Log.i("message", msg.arg1.toString())
                    Log.i("message", msg.obj.toString())
                    stringBuilder.append(readMessage)                                      //keep appending to string until ~
                    val endOfLineIndex = stringBuilder.indexOf("~") // determine the end-of-line
                    val endoffirstsensor = stringBuilder.indexOf("!")
                    val endofsecondsensor = stringBuilder.indexOf("$")
                    val endofthirdsensor = stringBuilder.indexOf("&")
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        var dataInPrint = stringBuilder.substring(0, endOfLineIndex)    // extract string
                        //txtString.setText("Data Received = " + dataInPrint);
                        val dataLength = dataInPrint.length                          //get length of data received
                        // txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (stringBuilder[0] == '#')
                        //if it starts with # we know it is what we are looking for
                        {


                            var sensor0 = stringBuilder.substring(1, endoffirstsensor)             //get sensor value from string between indices 1-5
                            var sensor1 = stringBuilder.substring(endoffirstsensor + 1, endofsecondsensor) //same again...
                            var value = sensor1.toInt()
                            var sensor2 = stringBuilder.substring(endofsecondsensor + 1, endofthirdsensor)

                            if (stringBuilder[1] == '0') {
                                if (value in 50..100) {

                                    valuetextView.text = value.toString()+" %"
                                    status.text = "Unstable"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.sad))


                                } else {

                                    valuetextView.text = value.toString()+" %"
                                    status.text = "Stabel"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.happy))

                                }
                                temptextView.text = sensor2 + " C"

                            } else {
                                hearttextView.text = sensor0 + " BPM"
                                if (value in 50..100) {

                                    valuetextView.text = value.toString()+" %"
                                    status.text = "UnStable"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.sad))


                                } else {

                                    valuetextView.text = value.toString()+" %"
                                    status.text = "stable"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.happy))

                                }
                                temptextView.text = sensor2 + " C"

                                if (sensor0.toInt() in 49..59){
                                    heartbeat="0"

                                }else if(sensor0.toInt() in 60..85){
                                    heartbeat="1"

                                }else if(sensor0.toInt() >85 ){
                                    heartbeat= "-1"
                                }

                                if(sensor2.toFloat() in 20.0..37.5){
                                    temptextView.text="37"
                                    temp="1"
                                }else if(sensor2.toFloat() >37.5){
                                    temp="-1"
                                }
                                else if(sensor2.toFloat() <20){
                                    temp="0"
                                }
                                if(MachineNetwork.startTask(heartbeat,temp,MainActivity.BASE_URL,this@ProfileActivity)!= null){
                                    var call: Call<ResponseBody>?=MachineNetwork.startTask(heartbeat,temp,MainActivity.BASE_URL,this@ProfileActivity)
                                    call!!.enqueue(object:retrofit2.Callback<ResponseBody>{
                                        override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                                            t?.printStackTrace()
                                        }

                                        override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                                            var obj:JSONObject= JSONObject(response?.body()?.string())
                                            var value:Int=obj.getInt("Status")
                                            when(value){
                                                0->{
                                                    statustxt.text="Perfect"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_green_light))
                                                    exptxt.text="Normal Heartbeat and temperature"
                                                }
                                                1->{
                                                    statustxt.text="Danger"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                                                    exptxt.text="Heart beat and temperature are lower than average"
                                                    Emergencycall()
                                                }
                                                2-> {
                                                    statustxt.text = "Danger!"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                                                    exptxt.text = "Heart beat and temperature are above Average"
                                                    Emergencycall()
                                                }
                                                3->{
                                                    statustxt.text="Caution!"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                                                    exptxt.text="temperature is above the average"}
                                                4->{
                                                    statustxt.text="Caution!"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                                                    exptxt.text="temperature is below the average"}
                                                5->{
                                                    statustxt.text="Caution!"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                                                    exptxt.text="Heartbeat is above the Average"
                                                    Emergencycall()
                                                }
                                                6->{
                                                    statustxt.text="Caution!"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_orange_light))
                                                    exptxt.text="Heartbeat is below the average"
                                                    Emergencycall()
                                                }
                                                7->{
                                                    statustxt.text="Tricky!"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_red_light))
                                                    exptxt.text="Heartbeat is high and tempreature is below average"}
                                                8->{
                                                    statustxt.text="Tricky"
                                                    statustxt.setTextColor(resources.getColor(android.R.color.holo_red_light))
                                                    exptxt.text="heart beat is below average and temperature is high"}
                                            }
                                        }
                                    })
                                }else{

                                }

                                data = UserData(sensor0.toFloat(), sensor2.toFloat(), sensor1.toInt())
                                database.getReference("users").child(user.uid).child("data").setValue(data)
                                database.getReference("users").child(user.uid).addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(value: DataSnapshot?) {
                                        var followersnapshot: DataSnapshot = value!!.child("followers")
                                        if (followersnapshot.exists()) {
                                            var followerchildren: Iterable<DataSnapshot> = followersnapshot.children
                                            for (follower in followerchildren) {
                                                Log.d("followerkey", follower.key)
                                                database.getReference("users").child(follower.key).child("following").child(user.uid).child("data").setValue(data)
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError?) {
                                        Log.d("profDataBaseerror", error?.message)
                                    }
                                })
                            }


                        }
                        stringBuilder.delete(0, stringBuilder.length)                    //clear all string data
                        dataInPrint = " "
                    }
                }
            }
        }
        adapter = BluetoothAdapter.getDefaultAdapter()
        checkBTState()

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()

        OneSignal.sendTag("User_Id", user.email)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }


    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
        //creates secure outgoing connecetion with BT device using UUID
    }


    override fun onResume() {
        super.onResume()

        val intent = intent

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(Contract.EXTRA_DEVICE_ADDRESS)

        //create device and set the MAC address
        val device = adapter.getRemoteDevice(address)

        try {
            socket = createBluetoothSocket(device)
        } catch (e: IOException) {
            Toast.makeText(baseContext, "Socket creation failed", Toast.LENGTH_LONG).show()
        }

        // Establish the Bluetooth socket connection.
        try {
            socket?.connect()
        } catch (e: IOException) {
            bluetoothtextView.setText("Disconnected")
            bluetoothimageView.setImageDrawable(resources.getDrawable(R.drawable.ic_bluetooth_disabled_indigo_800_48dp))
            try {
                socket?.close()
            } catch (e2: IOException) {
                //insert code to deal with this
            }

        }

        connectedThread = ConnectedThread(socket)
        connectedThread.start()


    }


    override fun onPause() {
        super.onPause()
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            socket?.close()
        } catch (e2: IOException) {
            //insert code to deal with this
        }

    }


    private fun checkBTState() {

        if (adapter == null) {
            Toast.makeText(baseContext, "Device does not support bluetooth", Toast.LENGTH_LONG).show()
        } else {
            if (adapter.isEnabled()) {
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
        }

    }


    private inner class ConnectedThread//creation of the connect thread
    (socket: BluetoothSocket?) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                //Create I/O streams for connection
                tmpIn = socket?.inputStream
                tmpOut = socket?.outputStream
            } catch (e: IOException) {
                bluetoothtextView.setText("Dissconnected")
                bluetoothimageView.setImageDrawable(resources.getDrawable(R.drawable.ic_bluetooth_disabled_indigo_800_48dp))
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)            //read bytes from input buffer
                    val readMessage = String(buffer, 0, bytes)
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothin.obtainMessage(handlerstate, bytes, -1, readMessage).sendToTarget()
                } catch (e: IOException) {
                    break
                }

            }
        }
    }

    private fun initUi(username: String, Followers_number: Int) {
        usernametxt.text=username
        followernum.text="Followers : "+Followers_number
    }



    private fun loadimage() {
        var imageref: StorageReference = storageRef.child(user?.uid).child("image.jpg")
        imageref.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
            override fun onSuccess(uri: Uri?) {
                Picasso.with(this@ProfileActivity).load(uri).placeholder(resources.getDrawable(R.drawable.index)).into(image)
            }
        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(p0: Exception) {
                Log.d("loadImage", p0.localizedMessage)
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.logout -> {
                if (provider.equals("facebook.com")) {
                    LoginManager.getInstance().logOut()
                    Signout()


                } else if (provider.equals("google.com")) {
                    mgoogleSignInClient.signOut().addOnSuccessListener {
                        Log.d("Google Sign Out ", "Success")
                    }
                    Signout()


                } else {
                    Signout()
                }

                return true

            }
            R.id.following ->{
                val intent=Intent(this,FollowingActivity::class.java)
                startActivity(intent)
                return true
            }

            android.R.id.home->{
                onBackPressed()
            }
        }
        return true
    }


    fun Signout() {
        mAuth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun Emergencycall(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    100)

        }else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                var gecoder: Geocoder = Geocoder(this, Locale.getDefault())
                var adresses = gecoder.getFromLocation(location!!.latitude, location.longitude, 1)
                var adress=adresses.get(0).getAddressLine(0)
                val city = adresses.get(0).getLocality()
                val state = adresses.get(0).getAdminArea()
                val country = adresses.get(0).getCountryName()
                val total=adress+", "+city+", "+state+", "+country
                NotificationNetwork.sendNotification(getUserfollowers(),total)
                sendSMS(total)

            }

        }


    }

    private fun getUserfollowers():ArrayList<String>{
       var emails: ArrayList<String> = ArrayList()
        var reference=database.getReference("users").child(user.uid).child("followers").ref
        reference.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                Log.d("followersget","error retriveing followers")
            }

            override fun onDataChange(data: DataSnapshot?) {
                if (data!!.exists()) {
                    var followerschildren: Iterable<DataSnapshot> = data.children
                    for (follower: DataSnapshot in followerschildren) {
                        emails.add(follower.value.toString())
                    }
                }
            }
        })
        return emails
    }
    fun sendSMS(location:String){
    var intent:Intent =Intent(Intent.ACTION_VIEW, Uri.parse("sms:" +"+201116454525"))
        intent.putExtra("sms_body","this person you follow needs help ASAP he is located in"+location)
        startActivity(intent);
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            100 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Emergencycall()
                }
                return

            }
        }
    }

}
