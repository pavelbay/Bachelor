package com.pavel.augmented.activities.canvas

import android.graphics.Bitmap
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface MainActivityContract {

    interface View : BaseView<Presenter> {

        fun displayDialog()
    }

    interface Presenter : BasePresenter<View> {
        fun saveToGallery(bitmap: Bitmap?)
        fun publishSketch()
    }
}