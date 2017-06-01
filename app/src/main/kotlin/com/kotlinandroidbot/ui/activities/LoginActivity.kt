package com.kotlinandroidbot.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.kotlinandroidbot.data.network.ErrorResponse
import com.kotlinandroidbot.data.presenters.impl.AuthPresenterImpl
import com.kotlinandroidbot.data.presenters.views.LoginView
import com.kotlinandroidbot.ui.layouts.LoginActivityUI
import okhttp3.ResponseBody
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity(), LoginView {

    lateinit var ui: LoginActivityUI

    lateinit var auth: AuthPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = LoginActivityUI()
        ui.setContentView(this)

        auth = AuthPresenterImpl(this)
    }

    fun onLoginClick(username: String, password: String) {
        auth.login(username, password)
    }

    override fun onLogged(response: ResponseBody) {
        Log.e("res", response.toString())
    }

    override fun onError(error: ErrorResponse) {
        toast(error.message)
    }

}

