package com.kotlinandroidbot.data.presenters.views

import com.kotlinandroidbot.data.presenters.BaseView
import okhttp3.ResponseBody

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */
interface LoginView : BaseView {

    fun onLogged(response: ResponseBody)

}