package com.kotlinandroidbot.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.kotlinandroidbot.ui.layouts.MainActivityUI
import org.jetbrains.anko.setContentView

class MainActivity : AppCompatActivity() {

    lateinit var ui : MainActivityUI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ui = MainActivityUI()
        ui.setContentView(this)

    }
}
