package com.pavel.augmented.presentation.map

import android.location.Location
import android.util.DisplayMetrics
import com.google.android.gms.maps.CameraUpdate
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface MyMapContract {

    interface View : BaseView<Presenter> {
        fun displayMetrics(): DisplayMetrics
    }

    interface Presenter : BasePresenter<View> {
        fun calculateCameraUpdateToMyLocation(myLocation: Location): CameraUpdate
    }
}