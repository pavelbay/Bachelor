package com.pavel.augmented.presentation.map

import android.location.Location
import android.util.Log
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.repository.SketchRepository

class MyMapPresenter(private val sketchRepository: SketchRepository) : MyMapContract.Presenter {

    override lateinit var view: MyMapContract.View

    override var currentTargetId: String? = null

    var sketches: List<Sketch>? = null

    private val testString = "[{\"id\":2,\"latitude\":52.5238463,\"longitude\":13.5454916,\"name\":\"second\"}, {\"id\":1,\"latitude\":52.5238391,\"longitude\":13.5454903,\"name\":\"first\"}]"


    override fun start() {
        fetchSketches()
    }

    override fun stop() {

    }

    override fun fetchSketches() {
        sketchRepository.fetchSketches {
            if (it != null) {
                it.forEach {
                    Log.d(TAG, "Sketch fetched: " + it.id)
                }
                Log.d(TAG, "Sketches fetched successfully")
                sketches = it
            }
            view.updateMarkers(it)
        }
    }

    override fun fetchImages(id: String) {
        Log.d(TAG, "Fetching for id: " + id)
        sketchRepository.fetchImage(id, false)
    }

    override fun fetchImagesForEditing(id: String) {

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

    companion object {
        private val TAG = MyMapPresenter::class.java.simpleName
    }
}