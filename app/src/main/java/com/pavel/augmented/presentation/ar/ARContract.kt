package com.pavel.augmented.presentation.ar

import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface ARContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter : BasePresenter<View> {

    }
}