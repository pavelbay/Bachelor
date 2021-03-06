package com.pavel.augmented.presentation.map

import android.location.Location
import android.util.DisplayMetrics
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLng
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.mvp.BasePresenter
import com.pavel.augmented.mvp.BaseView

interface MyMapContract {

    interface View : BaseView<Presenter> {
        fun displayMetrics(): DisplayMetrics
        fun updateMarkers(sketches: List<Sketch>?)
    }

    interface Presenter : BasePresenter<View> {
        var currentTargetId: String?
        fun calculateCameraUpdateToMyLocation(myLocation: Location): CameraUpdate
        fun fetchSketches()
        fun fetchImages(id: String, name: String, latLng: LatLng?)
        fun fetchImagesForEditing(id: String, name: String, latLng: LatLng?)
    }
}