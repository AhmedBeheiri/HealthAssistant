package com.apps.ahmed_beheiri.healthassistant.UI

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.apps.ahmed_beheiri.healthassistant.R
import com.truizlop.fabreveallayout.FABRevealLayout
import com.truizlop.fabreveallayout.OnRevealChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_login.*
import kotlinx.android.synthetic.main.content_signup.*
import android.widget.Toast
import com.apps.ahmed_beheiri.healthassistant.Model.Contract
import com.apps.ahmed_beheiri.healthassistant.Model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import android.provider.MediaStore
import com.apps.ahmed_beheiri.healthassistant.Model.UserData
import com.facebook.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.lang.Exception
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class MainActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth
    lateinit var databaseref:DatabaseReference
    lateinit var database:FirebaseDatabase
    lateinit var storage: FirebaseStorage
    lateinit var mGooglesignInClient:GoogleSignInClient
    private val RC_SIGN_Google=9001
    lateinit var mCallbackManager: CallbackManager
    var following:LinkedHashMap<String,User> = LinkedHashMap()
    var followers:LinkedHashMap<String,String> = LinkedHashMap()
    lateinit var datauser:UserData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(getApplicationContext())
        AppEventsLogger.activateApp(this)
        configureFabreveal(fab_reveal_layout)
        mAuth = FirebaseAuth.getInstance()
        datauser= UserData(1.0f,1.0f,1)

        mCallbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result?.getAccessToken()!!)
            }

            override fun onError(error: FacebookException?) {
                Log.d("FacebookLogin", "Error " + error?.message)
                updateUI(null)

            }

            override fun onCancel() {
                Log.d("FacebookLogin", "canceled ")
                updateUI(null)

            }

        })

        facebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))

        }

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestProfile()
                .requestEmail().build()

        @JvmStatic
        mGooglesignInClient = GoogleSignIn.getClient(this, gso)

        google.setOnClickListener { SignInwithGoogle() }
        cancel.setOnClickListener { prepareBackTransation(fab_reveal_layout) }
        signInButton.setOnClickListener {
            if (emaillogin.text.isEmpty() || passlogin.text.isEmpty()) {
                Toast.makeText(this@MainActivity, "please Enter your e-mail and Password", Toast.LENGTH_LONG).show()
            } else {
                signInEmail(emaillogin.text.toString(), passlogin.text.toString())
            }
        }

        pickimage.setOnClickListener {
            uploadImage()
        }
        signupbtn.setOnClickListener {
            if (signupemail.text.isEmpty() || passsignup.text.isEmpty() || imageuri.text.equals("Pick an Image")) {
                Toast.makeText(this@MainActivity, "Please enter E-mail and password and pick an image to signup", Toast.LENGTH_LONG).show()

            } else {
                signUp(signupemail.text.toString().trim(), passsignup.text.toString().trim(), imageuri.text.toString().trim())
            }
        }
       /* try {
            val info = packageManager.getPackageInfo(
                    "com.apps.ahmed_beheiri.healthassistant",
                    PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }*/
    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null) {
            progressbar.visibility=View.VISIBLE
            val currentuser = mAuth.currentUser
            databaseref = database.getReference("users").child(currentuser?.uid)
                databaseref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value!=null) {

                            updateUI(currentuser?.uid)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("Ch3", "Failed to read value.", error.toException())
                    }
                })
            }
    }

    private fun configureFabreveal(fabRevealLayout: FABRevealLayout){
        fabRevealLayout.setOnRevealChangeListener(object : OnRevealChangeListener {
            override fun onMainViewAppeared(fabRevealLayout: FABRevealLayout, mainView: View) {
                Log.d("cancelbutton","gone")
                cancel.setVisibility(View.GONE)
            }

            override fun onSecondaryViewAppeared(fabRevealLayout: FABRevealLayout, secondaryView: View) {
                Log.d("cancelbutton","show")
                cancel.setVisibility(View.VISIBLE)
            }
        })
    }

    private fun prepareBackTransation(fabRevealLayout: FABRevealLayout){
        Handler().postDelayed({ fabRevealLayout.revealMainView() }, 2000)
    }

    private fun signInEmail(email:String,pass:String){

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    progressbar.visibility=View.VISIBLE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.currentUser
                        databaseref=database.getReference("users").child(user?.uid)
                        databaseref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(data: DataSnapshot?) {
                              //  var value=data?.value as String
                                updateUI(user?.uid)

                            }

                            override fun onCancelled(error: DatabaseError?) {

                                Toast.makeText(this@MainActivity,"wrong username and password",Toast.LENGTH_LONG).show()
                                Log.e("database error",error?.message)

                            }})


                    } else {
                        // If sign in fails, display a message to the user.

                        Toast.makeText(this@MainActivity, "Wrong User Name or Password.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                }
    }


    private fun signUp(email: String,pass: String,imageurii:String){
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    progressbar.visibility=View.VISIBLE
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information

                        var user = mAuth.currentUser
                        databaseref=database.getReference("users")

                        var data:Uri= Uri.parse(imageurii)
                        uploadImagetoserver(data,user?.uid)
                        var myuser=User(email,imageuri.text.toString(),gettingUsernamefromEmail(email.trim()),following,followers,datauser,getcode())
                        databaseref.child(user?.uid).setValue(myuser)

                        updateUI(user?.uid)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@MainActivity, "signup failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }

    }


    private fun updateUI(userid:String?){
        if (userid!=null) {
            progressbar.visibility=View.GONE

            var i: Intent = Intent(this@MainActivity, TransactionActivity::class.java)
            i.putExtra(Contract.EXTRA_USER_VALUE1,userid)
            startActivity(i)
        }

    }

    private fun ReadUser(UID:String):User?{
        var user:User?=null
        databaseref=database.getReference(UID)
        databaseref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user= dataSnapshot.value as User
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ch3", "Failed to read value.", error.toException())
            }
        })

        return user

    }


    private fun uploadImage(){
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){

            1 ->{
                if (resultCode== Activity.RESULT_OK){
                    val selectedImage = data?.data
                    imageuri.text = selectedImage.toString()

                }
            }

            RC_SIGN_Google ->{
                var task:Task<GoogleSignInAccount> =GoogleSignIn.getSignedInAccountFromIntent(data)
                try{

                    var account:GoogleSignInAccount=task.getResult(ApiException::class.java)
                    firebaseAuthWithFireBase(account)

                }catch (e: ApiException){
                    e.printStackTrace()
                    Log.d("Googlesignin","Sign in Failed : "+e)
                    updateUI(null)
                }
            }
        }
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }


    fun uploadImagetoserver(data: Uri, uid: String?){
        var imageUri: Uri = data
        val bitmap:Bitmap=MediaStore.Images.Media.getBitmap(this.contentResolver,imageUri)
        var stream:ByteArrayOutputStream= ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
        var imagedata: ByteArray = stream.toByteArray()
        var storageRef:StorageReference=storage.getReference("users")
        var imageref:StorageReference=storageRef.child(uid!!).child("image.jpg")
        var uploadTask:UploadTask=imageref.putBytes(imagedata)
        uploadTask.addOnSuccessListener { taskSnapshot:UploadTask.TaskSnapshot ->var url=taskSnapshot.downloadUrl.toString()
            imageuri.text=url
        Log.d("downloadurl",url) }
        uploadTask.addOnFailureListener{exception: Exception ->Log.d("upload error",exception.localizedMessage)  }

    }


    fun SignInwithGoogle(){
        var i= mGooglesignInClient.signInIntent
        startActivityForResult(i,RC_SIGN_Google)
    }


    private fun firebaseAuthWithFireBase(account: GoogleSignInAccount){
        Log.d("GooglesignIn", "firebaseAuthWithGoogle:" + account.getId())
        progressbar.visibility=View.VISIBLE
        var credentials:AuthCredential=GoogleAuthProvider.getCredential(account.idToken,null)
        mAuth.signInWithCredential(credentials)
                .addOnCompleteListener(this, object : OnCompleteListener<AuthResult> {
                   override fun onComplete(task: Task<AuthResult>) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("GoogleSigIn", "signInWithCredential:success")
                            val user = mAuth.getCurrentUser()
                            databaseref=database.getReference("users")
                            databaseref.addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0?.hasChild(user?.uid)!!){
                                        updateUI(user?.uid)
                                    }else{
                                        var myuser=User(user?.email!!,user.photoUrl.toString(),user.displayName!!,following,followers,datauser,getcode())
                                        databaseref.child(user?.uid).setValue(myuser)
                                        updateUI(user?.uid)
                                    }
                                }

                                override fun onCancelled(p0: DatabaseError?) {

                                }

                            })
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException())
                            Toast.makeText(this@MainActivity, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }

                    }
                })
    }



    fun handleFacebookAccessToken(token:AccessToken){
        progressbar.visibility=View.VISIBLE
        var credentials:AuthCredential=FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credentials)
                .addOnCompleteListener(this,object :OnCompleteListener<AuthResult>{
                    override fun onComplete(task: Task<AuthResult>) {
                        if(task.isSuccessful){

                            var user:FirebaseUser?=mAuth.currentUser
                            databaseref=database.getReference("users")
                            databaseref.addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(p0: DataSnapshot?) {
                                    if(p0?.hasChild(user?.uid)!!){
                                        updateUI(user?.uid)
                                    }else{

                                        var myuser:User= User(user?.email!!,user?.photoUrl.toString(),user?.displayName!!,following,followers,datauser,getcode())
                                        databaseref.child(user?.uid).setValue(myuser)
                                        updateUI(user?.uid)

                                    }

                                }

                                override fun onCancelled(p0: DatabaseError?) {

                                }
                            })

                        }else{
                            Log.d("FacebookLogin","Failed to sign In")
                            Toast.makeText(this@MainActivity,"Failed to sign in try Again",Toast.LENGTH_LONG).show()
                            updateUI(null)

                        }

                    }
                })
    }


    fun gettingUsernamefromEmail(email: String):String{
        var index=email.indexOf('@')
        var username=email.substring(0,index)
        return username
    }

    fun getcode():Int{
        val r = Random()
        val i1 = r.nextInt(10000 - 1000) + 1000
        return i1
    }





}
