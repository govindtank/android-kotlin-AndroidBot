package com.kotlinandroidbot.data.rx

/**
 * Created by serhii_slobodyanuk on 6/1/17.
 */
interface RxTaskListeners<in V> {

    fun onError(error: Throwable)

    fun onResponse(response: V)

}