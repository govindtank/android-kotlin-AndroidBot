package com.kotlinandroidbot.data.presenters.impl

import com.kotlinandroidbot.data.network.ErrorResponse
import com.kotlinandroidbot.data.network.RestClient
import com.kotlinandroidbot.data.presenters.Presenter
import com.kotlinandroidbot.data.presenters.views.LoginView
import com.kotlinandroidbot.data.rx.RxTaskListeners
import okhttp3.ResponseBody

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */

class AuthPresenterImpl(view: LoginView) : Presenter<LoginView>(view), RxTaskListeners<ResponseBody> {


    fun login(username: String, password: String) {
        RestClient().login(username, password)
    }

    override fun onResponse(response: ResponseBody) {
        view.onLogged(response)
    }

    override fun onError(error: Throwable) {
        view.onError(ErrorResponse(error.toString()))
    }
}