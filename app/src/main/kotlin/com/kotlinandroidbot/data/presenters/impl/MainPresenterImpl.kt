package com.kotlinandroidbot.data.presenters.impl

import com.kotlinandroidbot.data.network.ErrorResponse
import com.kotlinandroidbot.data.presenters.Presenter
import com.kotlinandroidbot.data.presenters.views.MainView
import com.kotlinandroidbot.data.rx.RxTask
import com.kotlinandroidbot.data.rx.RxTaskListeners
import io.reactivex.Observable

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */

class MainPresenterImpl(view: MainView) : Presenter<MainView>(view), RxTaskListeners<Any> {


    fun m() {
        RxTask<Int>(Observable.just(1), this).execute()
        RxTask<String>(Observable.just("Task string 1"), this).execute()
        RxTask<String>(Observable.just("Task string 2"), this).execute()
    }

    override fun onResponse(response: Any) {
         view.s(response)
    }

    override fun onError(error: Throwable) {
        view.onError(ErrorResponse(error.toString()))
    }
}