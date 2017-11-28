package com.pavel.augmented.presentation.galerie

import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface GalerieContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter : BasePresenter<View> {

    }
}