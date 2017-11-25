package com.pavel.augmented.presentation.map

import android.location.Location
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil

class MyMapPresenter : MyMapContract.Presenter {

    override lateinit var view: MyMapContract.View

    override fun start() {

    }

    override fun stop() {

    }

    override fun calculateCameraUpdateToMyLocation(myLocation: Location): CameraUpdate {
        val center = LatLng(myLocation.latitude, myLocation.longitude)

        return if (myLocation.accuracy > 30) {
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(center)
            val toNorth = SphericalUtil.computeOffset(center, myLocation.accuracy.toDouble(), 0.0)
            boundsBuilder.include(toNorth)
            val toEast = SphericalUtil.computeOffset(center, myLocation.accuracy.toDouble(), 90.0)
            boundsBuilder.include(toEast)
            val toSouth = SphericalUtil.computeOffset(center, myLocation.accuracy.toDouble(), 180.0)
            boundsBuilder.include(toSouth)
            val toWest = SphericalUtil.computeOffset(center, myLocation.accuracy.toDouble(), 270.0)
            boundsBuilder.include(toWest)
            val width = view.displayMetrics().widthPixels
            val height = view.displayMetrics().heightPixels
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), width, height, 25)
        } else {
            CameraUpdateFactory.newLatLngZoom(center, 15f)
        }
    }
}