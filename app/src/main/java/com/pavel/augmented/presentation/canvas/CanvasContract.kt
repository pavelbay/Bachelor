package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface CanvasContract {

    interface View : BaseView<Presenter> {
        var tempBitmapSaved: Boolean

        fun displayDialog()

        fun displayMessageSavedToGallery()

        fun displayMessageSketchWithNameAlreadyExists()

        fun displayMessageCannotCreateSketch()
    }

    interface Presenter : BasePresenter<View> {
        var existedSketch: Sketch?
        fun saveToGallery(name: String?, bitmap: Bitmap?)
        fun publishSketch()
        fun saveTempBitmap(bitmap: Bitmap?)
        fun getJsonTarget(): String?
    }
}