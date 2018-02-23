package com.apps.ahmed_beheiri.healthassistant.UI

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.apps.ahmed_beheiri.healthassistant.R
import kotlinx.android.synthetic.main.headerview.view.*

/**
 * Created by ahmed_beheiri on 23/02/18.
 */
class HeaderView :LinearLayout {

    @BindView(R.id.name)
    internal lateinit var name: TextView

    @BindView(R.id.followersnum)
    internal lateinit var followersnum: TextView
    constructor(context: Context) : super(context) {
    }

   constructor(context: Context, attrs: AttributeSet): super(context,attrs) {

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int):super(context,attrs,defStyleAttr) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
   constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this)
    }

    fun bindTo(name: String, lastSeen: String) {
        this.name!!.text = name
        this.followersnum!!.text = lastSeen
    }

    fun setTextSize(size: Float) {
        name!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }
}