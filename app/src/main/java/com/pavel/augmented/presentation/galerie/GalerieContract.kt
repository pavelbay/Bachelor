package com.pavel.augmented.presentation.galerie

import com.pavel.augmented.model.Sketch
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface GalerieContract {

    interface View : BaseView<Presenter> {
        fun displaySketches(list: MutableList<Sketch>)
        fun updateUi()
    }

    interface Presenter : BasePresenter<View> {
        fun loadSketches()
        fun publicSketch()
        fun deleteSketches(sketches: Array<Sketch?>)
    }
}