package com.apps.ahmed_beheiri.healthassistant.UI

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

/**
 * Created by ahmed_beheiri on 23/02/18.
 */
class ViewUtils {
    private var screenWidth = 0


    fun getScreenWidth(c: Context): Int {
        if (screenWidth == 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        }

        return screenWidth
    }
}