package com.apps.ahmed_beheiri.healthassistant.UI

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import com.apps.ahmed_beheiri.healthassistant.R
import com.google.firebase.auth.FirebaseAuth
import com.truizlop.fabreveallayout.FABRevealLayout
import com.truizlop.fabreveallayout.OnRevealChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_login.*
import kotlinx.android.synthetic.main.content_signup.*


class MainActivity : AppCompatActivity() {

    lateinit var mAuth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureFabreveal(fab_reveal_layout)
        mAuth= FirebaseAuth.getInstance()
        cancel.setOnClickListener { prepareBackTransation(fab_reveal_layout) }
        signInButton.setOnClickListener{

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
}
