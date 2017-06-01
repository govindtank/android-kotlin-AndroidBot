package com.kotlinandroidbot.data.rx

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */

class RxTask<T> {

    val observable: Observable<T>
    val observers: List<RxTaskListeners<T>>
    val subscribeOn: Scheduler
    val observeOn: Scheduler

    constructor(observable: Observable<T>, observers: List<RxTaskListeners<T>>,
                subscribeOn: Scheduler = Schedulers.io(),
                observeOn: Scheduler = AndroidSchedulers.mainThread()) {
        this.observable = observable
        this.observers = observers
        this.subscribeOn = subscribeOn
        this.observeOn = observeOn
    }

    constructor(observable: Observable<T>, subscriber: RxTaskListeners<T>,
                subscribeOn: Scheduler = Schedulers.io(),
                observeOn: Scheduler = AndroidSchedulers.mainThread()) {
        this.observable = observable
        this.observers = listOf(subscriber)
        this.subscribeOn = subscribeOn
        this.observeOn = observeOn
    }

    fun execute() : Observable<T> {
        observable
                .publish()
                .autoConnect(observers.size)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)

        observers.forEach({
            observable.subscribe(
                    { next -> it.onResponse(next) },
                    { error -> it.onError(error) }
            )
        })

        return observable
    }

}