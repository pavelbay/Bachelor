package com.example.pavel.myapplication.activities.canvas

import com.example.pavel.myapplication.mvp.BasePresenter
import com.example.pavel.myapplication.mvp.BaseView

interface MainActivityContract {

    interface View : BaseView<Presenter> {

        fun displayDialog()
    }

    interface Presenter : BasePresenter<View> {
        fun saveToGallery()
        fun publishSketch()
    }
}