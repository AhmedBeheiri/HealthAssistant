package com.apps.ahmed_beheiri.healthassistant.UI

import android.content.Intent
import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import com.apps.ahmed_beheiri.healthassistant.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


       Handler().postDelayed({
            var intent:Intent= Intent(this,MainActivity::class.java)
            startActivity(intent)},3000)


    }
}
