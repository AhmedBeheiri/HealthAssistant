package com.apps.ahmed_beheiri.healthassistant.UI

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.apps.ahmed_beheiri.healthassistant.R

/**
 * Created by ahmed_beheiri on 23/02/18.
 */
class HeaderBehavior :CoordinatorLayout.Behavior<HeaderView> {
    private var mContext: Context? = null

    private var mStartMarginLeft: Int = 0
    private var mEndMarginLeft: Int = 0
    private var mMarginRight: Int = 0
    private var mStartMarginBottom: Int = 0
    private var mTitleStartSize: Float = 0.toFloat()
    private var mTitleEndSize: Float = 0.toFloat()
    private var isHide: Boolean = false

   constructor(context: Context, attrs: AttributeSet):super(context, attrs) {

        mContext = context
    }

    constructor(context: Context, attrs: AttributeSet, mContext: Context): super(context, attrs) {

        this.mContext = mContext
    }

    fun getToolbarHeight(context: Context?): Int {
        var result = 0
        val tv = TypedValue()
        if (context!!.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            result = TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return result
    }

    override fun layoutDependsOn(parent: CoordinatorLayout?, child: HeaderView?, dependency: View?): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout?, child: HeaderView?, dependency: View?): Boolean {
        shouldInitProperties()

        val maxScroll = (dependency as AppBarLayout).totalScrollRange
        val percentage = Math.abs(dependency.y) / maxScroll.toFloat()
        var childPosition = (dependency.height + dependency.y
                - child!!.height
                - (getToolbarHeight(mContext) - child.height) * percentage / 2)

        childPosition = childPosition - mStartMarginBottom * (1f - percentage)

        val lp = child.layoutParams as CoordinatorLayout.LayoutParams
        if (Math.abs(dependency.y) >= maxScroll / 2) {
            val layoutPercentage = (Math.abs(dependency.y) - maxScroll / 2) / Math.abs(maxScroll / 2)
            lp.leftMargin = (layoutPercentage * mEndMarginLeft).toInt() + mStartMarginLeft
            child.setTextSize(getTranslationOffset(mTitleStartSize, mTitleEndSize, layoutPercentage))
        } else {
            lp.leftMargin = mStartMarginLeft
        }
        lp.rightMargin = mMarginRight
        child.layoutParams = lp
        child.y = childPosition

        if (isHide && percentage < 1) {
            child.visibility = View.VISIBLE
            isHide = false
        } else if (!isHide && percentage == 1f) {
            child.visibility = View.GONE
            isHide = true
        }
        return true
    }

    protected fun getTranslationOffset(expandedOffset: Float, collapsedOffset: Float, ratio: Float): Float {
        return expandedOffset + ratio * (collapsedOffset - expandedOffset)
    }

    private fun shouldInitProperties() {
        if (mStartMarginLeft == 0) {
            mStartMarginLeft = mContext!!.resources.getDimensionPixelOffset(R.dimen.header_view_start_margin_left)
        }

        if (mEndMarginLeft == 0) {
            mEndMarginLeft = mContext!!.resources.getDimensionPixelOffset(R.dimen.header_view_end_margin_left)
        }

        if (mStartMarginBottom == 0) {
            mStartMarginBottom = mContext!!.resources.getDimensionPixelOffset(R.dimen.header_view_start_margin_bottom)
        }

        if (mMarginRight == 0) {
            mMarginRight = mContext!!.resources.getDimensionPixelOffset(R.dimen.header_view_end_margin_right)
        }

        if (mTitleStartSize == 0f) {
            mTitleEndSize = mContext!!.resources.getDimensionPixelSize(R.dimen.header_view_end_text_size).toFloat()
        }

        if (mTitleStartSize == 0f) {
            mTitleStartSize = mContext!!.resources.getDimensionPixelSize(R.dimen.header_view_start_text_size).toFloat()
        }
    }

}