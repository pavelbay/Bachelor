package com.example.pavel.myapplication.mvp

interface BaseView<out T : BasePresenter<*>> {

    val presenter: T

}