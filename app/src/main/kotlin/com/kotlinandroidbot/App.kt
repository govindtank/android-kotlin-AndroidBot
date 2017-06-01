package com.kotlinandroidbot

import android.app.Application
import android.content.ContextWrapper
import com.pixplicity.easyprefs.library.Prefs

/**
 * Created by serhii_slobodyanuk on 5/30/17.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()

    }
}