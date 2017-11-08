package com.example.pavel.myapplication.mvp

interface BasePresenter<T> {

    fun start()

    fun stop()

    var view: T
}