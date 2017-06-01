package com.kotlinandroidbot.data.presenters

import com.kotlinandroidbot.data.network.ErrorResponse

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */

interface BaseView {

    fun onError(error : ErrorResponse)

}