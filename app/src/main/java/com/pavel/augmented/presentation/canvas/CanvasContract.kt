package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface CanvasContract {

    interface View : BaseView<Presenter> {

        fun displayDialog()

        fun displayMessageSavedToGallery()

        fun displayMessageCannotCreateSketch()
    }

    interface Presenter : BasePresenter<View> {
        fun saveToGallery(name: String, bitmap: Bitmap?)
        fun publishSketch()
    }
}