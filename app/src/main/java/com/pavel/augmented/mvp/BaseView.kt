package com.pavel.augmented.mvp

interface BaseView<out T : BasePresenter<*>> {

    val presenter: T

}