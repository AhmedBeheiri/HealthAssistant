package com.apps.ahmed_beheiri.healthassistant.UI

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import com.apps.ahmed_beheiri.healthassistant.R
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity(),AppBarLayout.OnOffsetChangedListener {
    @BindView(R.id.toolbar_header_view)
      protected lateinit var toolbarHeaderView: HeaderView

    @BindView(R.id.float_header_view)
     protected lateinit var floatHeaderView: HeaderView

    private var isHideToolbarView:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        ButterKnife.bind(this)


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        initUi()
    }

    private fun initUi() {
        appbar.addOnOffsetChangedListener(this)

        toolbarHeaderView.bindTo("User Page", "Followrs : 3")
        floatHeaderView.bindTo("User Page", "Followrs : 3")
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
}
