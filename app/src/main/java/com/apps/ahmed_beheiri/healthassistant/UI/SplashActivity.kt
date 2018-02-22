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

    lateinit var animationSet: AnimationSet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        var viewwidth=view_progress.width

        val move = TranslateAnimation((-(getScreenWidth() / 2) + viewwidth / 2).toFloat(), (getScreenWidth() / 2 + viewwidth / 2 + viewwidth).toFloat(), 0f, 0f)
        move.duration = 1000
        val move1 = TranslateAnimation((-viewwidth).toFloat(), 0f, 0f, 0f)
        move1.duration = 500
        val laftOut = ScaleAnimation(0f, 1f, 1f, 1f)
        laftOut.duration = 500

        animationSet = AnimationSet(true)
        animationSet.addAnimation(move)
        animationSet.addAnimation(move1)
        animationSet.addAnimation(laftOut)
        animationSet.addAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slideout))

        startAnimation()



       /* Handler().postDelayed({
            var intent:Intent= Intent(this,MainActivity::class.java)
            startActivity(intent)},5000)*/


    }


    private fun startAnimation() {
        view_progress.startAnimation(animationSet)
        android.os.Handler().postDelayed({
            finish()
            var intent:Intent= Intent(this,MainActivity::class.java)
            startActivity(intent) }, 2000)
    }

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }
}
