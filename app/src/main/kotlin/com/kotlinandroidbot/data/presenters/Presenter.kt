package com.kotlinandroidbot.data.presenters

/**
 * Created by serhii_slobodyanuk on 5/31/17.
 */
open class Presenter <out V : BaseView>(val view: V)