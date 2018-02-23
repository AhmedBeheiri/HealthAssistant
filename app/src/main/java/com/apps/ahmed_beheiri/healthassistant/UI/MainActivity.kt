package com.apps.ahmed_beheiri.healthassistant.UI

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
import com.apps.ahmed_beheiri.healthassistant.R.drawable.email
import com.apps.ahmed_beheiri.healthassistant.R.drawable.email






class MainActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureFabreveal(fab_reveal_layout)
        mAuth= FirebaseAuth.getInstance()
        cancel.setOnClickListener { prepareBackTransation(fab_reveal_layout) }
        signInButton.setOnClickListener{
            if(emaillogin.text.isEmpty()||passlogin.text.isEmpty()){
                Toast.makeText(this@MainActivity,"please Enter your e-mail and Password",Toast.LENGTH_LONG).show()
            }else {
                signInEmail(emaillogin.text.toString(),passlogin.text.toString())
            }
        }



    }

    override fun onStart() {
        super.onStart()
        if(mAuth.currentUser!=null){
            val currentuser= mAuth.currentUser
            updateUI(currentuser)
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

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.

                        Toast.makeText(this@MainActivity, "Wrong User Name or Password.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                }
    }


    private fun signUp(email: String,pass: String){
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information

                        var user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this@MainActivity, "signup failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // ...
                }

    }


    private fun updateUI(user:FirebaseUser?){
        if (user!=null) {
            var user1:User= User(user.email!!,user.photoUrl.toString()!!, user.displayName!!)
            var i: Intent = Intent(this@MainActivity, TransactionActivity::class.java)
           // i.putExtra(Contract.EXTRA_USER_VALUE1,user1)
            startActivity(i)
        }

    }
}
