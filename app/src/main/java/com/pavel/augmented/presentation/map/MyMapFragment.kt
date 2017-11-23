package com.pavel.augmented.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.util.askForPermissions
import com.pavel.augmented.util.toggleRegister
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.contextaware.ContextAwareFragment
import org.koin.android.ext.android.inject
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil


class MyMapFragment : ContextAwareFragment() {
    override val contextName = AppModule.CTX_MAP_FRAGMENT

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var googleMap: GoogleMap

    private val fusedLocationProviderClient by inject<FusedLocationProviderClient>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.layout_map_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.google_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            val parentActivity = activity
            if (parentActivity is AppCompatActivity) {
                parentActivity.askForPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_FROM_MAP_FRAGMENT)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    private fun zoomToMyLocation(location: Location) {
        val center = LatLng(location.latitude, location.longitude)

        if (location.accuracy > 30) {
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(center)
            val toNorth = SphericalUtil.computeOffset(center, location.accuracy.toDouble(), 0.0)
            boundsBuilder.include(toNorth)
            val toEast = SphericalUtil.computeOffset(center, location.accuracy.toDouble(), 90.0)
            boundsBuilder.include(toEast)
            val toSouth = SphericalUtil.computeOffset(center, location.accuracy.toDouble(), 180.0)
            boundsBuilder.include(toSouth)
            val toWest = SphericalUtil.computeOffset(center, location.accuracy.toDouble(), 270.0)
            boundsBuilder.include(toWest)
            val width = resources.displayMetrics.widthPixels
            val height = resources.displayMetrics.heightPixels
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), width, height, 25))
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15f))
        }
    }

    @SuppressLint("MissingPermission")
    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        if (event.result == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location -> zoomToMyLocation(location) }
        }
    }

    companion object {
        const val PERMISSION_LOCATION_FROM_MAP_FRAGMENT = 1002
    }
}