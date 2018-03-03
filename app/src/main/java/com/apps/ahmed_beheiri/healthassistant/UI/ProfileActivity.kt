package com.apps.ahmed_beheiri.healthassistant.UI

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import com.apps.ahmed_beheiri.healthassistant.Model.Contract
import com.apps.ahmed_beheiri.healthassistant.Model.User
import com.apps.ahmed_beheiri.healthassistant.R
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.content_profile.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class ProfileActivity : AppCompatActivity(),AppBarLayout.OnOffsetChangedListener {
    @BindView(R.id.toolbar_header_view)
      protected lateinit var toolbarHeaderView: HeaderView

    @BindView(R.id.float_header_view)
     protected lateinit var floatHeaderView: HeaderView

    internal lateinit var bluetoothin: Handler
    internal val handlerstate = 0
    private lateinit var adapter: BluetoothAdapter
    private var socket: BluetoothSocket? = null
    private val stringBuilder = StringBuilder()
    private lateinit var connectedThread: ConnectedThread
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var address: String? = null

    lateinit var user:FirebaseUser
    lateinit var mAuth:FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var provider:String
    lateinit  var storageRef: StorageReference
    lateinit var mgoogleSignInClient: GoogleSignInClient


    private var isHideToolbarView:Boolean=false
    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        ButterKnife.bind(this)
        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail().build()

        mgoogleSignInClient=GoogleSignIn.getClient(this,gso)
        database= FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        mAuth= FirebaseAuth.getInstance()
        user= mAuth.currentUser!!
        var databaseReference:DatabaseReference=database.getReference("users").child(user.uid)
         storageRef =storage.getReference("users")
        Log.d("Provider", user.providers!![0].toString())
        provider=user.providers!![0].toString()
        if(provider.equals("facebook.com")||provider.equals("google.com")){
            databaseReference.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(value: DataSnapshot?) {
                    var myuser: User? =value?.getValue(User::class.java)
                    Picasso.with(this@ProfileActivity).load(myuser!!.imageuri).placeholder(R.drawable.index).into(image)
                    initUi(myuser!!.username,3)
                }

                override fun onCancelled(error: DatabaseError?) {
                    Log.d("Database Error","error retriving data : "+error?.message)
                }
            })
        }else{


            databaseReference.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(value: DataSnapshot?) {
                    var myuser: User? =value?.getValue(User::class.java)

                    loadimage()
                    initUi(myuser!!.username,3)
                }

                override fun onCancelled(error: DatabaseError?) {
                    Log.d("Database Error","error retriving data : "+error?.message)
                }
            })

        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        //initUi()

        bluetoothin = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == handlerstate) {                                     //if message is what we want
                    val readMessage = msg.obj as String
                    // msg.arg1 = bytes from connect thread
                    Log.i("message", msg.arg1.toString())
                    Log.i("message", msg.obj.toString())
                    stringBuilder.append(readMessage)                                      //keep appending to string until ~
                    val endOfLineIndex = stringBuilder.indexOf("~") // determine the end-of-line
                    val endoffirstsensor=stringBuilder.indexOf("!")
                    val endofsecondsensor=stringBuilder.indexOf("$")
                    val endofthirdsensor=stringBuilder.indexOf("&")
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        var dataInPrint = stringBuilder.substring(0, endOfLineIndex)    // extract string
                        //txtString.setText("Data Received = " + dataInPrint);
                        val dataLength = dataInPrint.length                          //get length of data received
                        // txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        if (stringBuilder[0] == '#')
                        //if it starts with # we know it is what we are looking for
                        {



                                var sensor0 = stringBuilder.substring(1, endoffirstsensor)             //get sensor value from string between indices 1-5
                                var sensor1 = stringBuilder.substring(endoffirstsensor+1, endofsecondsensor) //same again...
                                var value=sensor1.toInt()
                                var sensor2=stringBuilder.substring(endofsecondsensor+1,endofthirdsensor)

                            if(stringBuilder[1]=='0'){
                                if(value==0){

                                valuetextView.text="50% - 70%"
                                    status.text="Happy"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.happy))


                                }else{

                                    valuetextView.text="over 80%"
                                    status.text="angry"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.angry))

                                }
                                temptextView.text=sensor2 +" C"

                            }else {

                                //String sensor2 = recDataString.substring(11, 15);
                                //String sensor3 = recDataString.substring(16, 20);
                                hearttextView.text = sensor0 + " BPM"
                                if(value==0){

                                    valuetextView.text="50% - 70%"
                                    status.text="Happy"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.happy))


                                }else{

                                    valuetextView.text="over 80%"
                                    status.text="angry"
                                    faceimageView.setImageDrawable(resources.getDrawable(R.drawable.angry))

                                }
                                temptextView.text = sensor2 + " C"
                            }



                            //update the textviews with sensor values
                        }
                        stringBuilder.delete(0, stringBuilder.length)                    //clear all string data
                        // strIncom =" ";
                        dataInPrint = " "
                    }
                }
            }
        }
        adapter = BluetoothAdapter.getDefaultAdapter()
        checkBTState()






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

private fun initUi(username:String,Followers_number:Int) {
        appbar.addOnOffsetChangedListener(this)

        toolbarHeaderView.bindTo(username, "Followrs : "+Followers_number)
        floatHeaderView.bindTo(username, "Followrs : "+Followers_number)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        val maxScroll = appBarLayout.totalScrollRange
        val percentage = Math.abs(offset).toFloat() / maxScroll.toFloat()

        if (percentage == 1f && isHideToolbarView) {
            toolbarHeaderView?.visibility = View.VISIBLE
            isHideToolbarView = !isHideToolbarView

        } else if (percentage < 1f && !isHideToolbarView) {
            toolbarHeaderView?.visibility = View.GONE
            isHideToolbarView = !isHideToolbarView
        }
    }


    private fun loadimage() {
        var imageref: StorageReference =storageRef.child(user?.uid).child("image.jpg")
        imageref.downloadUrl.addOnSuccessListener(object :OnSuccessListener<Uri>{
            override fun onSuccess(uri: Uri?) {
                Picasso.with(this@ProfileActivity).load(uri).placeholder(resources.getDrawable(R.drawable.index)).into(image)
            }
        }).addOnFailureListener(object :OnFailureListener{
            override fun onFailure(p0: Exception) {
                Log.d("loadImage",p0.localizedMessage)
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater:MenuInflater=menuInflater
        inflater.inflate(R.menu.main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.logout->{
                if(provider.equals("facebook.com")){
                    LoginManager.getInstance().logOut()
                    Signout()


                }else if(provider.equals("google.com")){
                    mgoogleSignInClient.signOut().addOnSuccessListener {
                        Log.d("Google Sign Out ","Success")
                    }
                    Signout()


                }else{
                    Signout()
                }

                return true

            }
        }
        return true
    }



    fun Signout(){
        mAuth.signOut();
        val intent=Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

}
