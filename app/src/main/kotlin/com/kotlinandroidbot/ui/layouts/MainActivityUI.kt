package com.kotlinandroidbot.ui.layouts

import android.graphics.Typeface
import com.kotlinandroidbot.ui.activities.MainActivity
import org.jetbrains.anko.*

/**
 * Created by serhii_slobodyanuk on 5/30/17.
 */

class MainActivityUI : AnkoComponent<MainActivity> {

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        relativeLayout {
            textView("text") {
                typeface = Typeface.MONOSPACE
                textSize = sp(22).toFloat()
            }
        }
    }

}