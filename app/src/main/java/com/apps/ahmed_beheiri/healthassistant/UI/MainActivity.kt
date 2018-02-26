package com.apps.ahmed_beheiri.healthassistant.UI

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.apps.ahmed_beheiri.healthassistant.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.truizlop.fabreveallayout.FABRevealLayout
import com.truizlop.fabreveallayout.OnRevealChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_login.*
import kotlinx.android.synthetic.main.content_signup.*
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.apps.ahmed_beheiri.healthassistant.Model.Contract
import com.apps.ahmed_beheiri.healthassistant.Model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import android.provider.MediaStore






class MainActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth
    lateinit var databaseref:DatabaseReference
    lateinit var database:FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureFabreveal(fab_reveal_layout)
        mAuth= FirebaseAuth.getInstance()

       database = FirebaseDatabase.getInstance()


        cancel.setOnClickListener { prepareBackTransation(fab_reveal_layout) }
        signInButton.setOnClickListener{
            if(emaillogin.text.isEmpty()||passlogin.text.isEmpty()){
                Toast.makeText(this@MainActivity,"please Enter your e-mail and Password",Toast.LENGTH_LONG).show()
            }else {
                signInEmail(emaillogin.text.toString(),passlogin.text.toString())
            }
        }

        pickimage.setOnClickListener{
            uploadImage()
        }
        signupbtn.setOnClickListener{
            if(signupemail.text.isEmpty()||passsignup.text.isEmpty()||imageuri.text.equals("Pick an Image")){
                Toast.makeText(this@MainActivity,"Please enter E-mail and password and pick an image to signup",Toast.LENGTH_LONG).show()

            }else{
                signUp(signupemail.text.toString().trim(),passsignup.text.toString().trim(),imageuri.text.toString().trim())
            }
        }



    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null) {
            val currentuser = mAuth.currentUser
            databaseref = database.getReference("users").child(currentuser?.uid)
                databaseref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value!=null) {
                            var myuser: User = dataSnapshot.value as User
                            updateUI(myuser)
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
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.currentUser
                        databaseref=database.getReference("users").child(user?.uid)
                        databaseref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                var myuser:User= dataSnapshot.value as User
                                updateUI(myuser)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Failed to read value
                                Log.w("Ch3", "Failed to read value.", error.toException())
                            }
                        })


                    } else {
                        // If sign in fails, display a message to the user.

                        Toast.makeText(this@MainActivity, "Wrong User Name or Password.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                }
    }


    private fun signUp(email: String,pass: String,imageuri:String){
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information

                        var user = mAuth.currentUser
                        databaseref=database.getReference("users")
                        var myuser=User(email,imageuri,"Ahmed")
                        databaseref.child(user?.uid).setValue(myuser)

                        updateUI(myuser)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@MainActivity, "signup failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }

    }


    private fun updateUI(user:User?){
        if (user!=null) {
            var user1:User=user
            var i: Intent = Intent(this@MainActivity, TransactionActivity::class.java)
           // i.putExtra(Contract.EXTRA_USER_VALUE1,user1)
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
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, 0)
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            0 ->{
                if(resultCode== Activity.RESULT_OK){
                    val selectedImage = data?.data
                    imageuri.text = selectedImage.toString()
                }
            }

            1 ->{
                if (resultCode== Activity.RESULT_OK){
                    val selectedImage = data?.data
                    imageuri.text = selectedImage.toString()
                }
            }
        }
    }
}
