package com.pavel.augmented.presentation.galerie

import android.content.Context
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface GalerieContract {

    interface View : BaseView<Presenter> {
        fun displaySketches(list: MutableList<Sketch>)
        fun context(): Context
    }

    interface Presenter : BasePresenter<View> {
        fun loadSketches()
        fun publicSketches(sketches: Array<Sketch?>)
        fun deleteSketches(sketches: Array<Sketch?>)
    }
}