package com.pavel.augmented.mvp

interface BasePresenter<T> {

    fun start()

    fun stop()

    var view: T
}