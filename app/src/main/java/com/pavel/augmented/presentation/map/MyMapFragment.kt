package com.pavel.augmented.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.util.askForPermissions
import com.pavel.augmented.util.toggleRegister
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.contextaware.ContextAwareFragment
import org.koin.android.ext.android.inject


class MyMapFragment : ContextAwareFragment(), MyMapContract.View {
    override val contextName = AppModule.CTX_MAP_FRAGMENT

    override val presenter by inject<MyMapContract.Presenter>()

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

    override fun onResume() {
        super.onResume()

        presenter.view = this
        presenter.start()
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun displayMetrics(): DisplayMetrics = resources.displayMetrics

    private fun zoomToMyLocation(location: Location) {
        googleMap.animateCamera(presenter.calculateCameraUpdateToMyLocation(location))
    }

    @SuppressLint("MissingPermission")
    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        if (event.requestId == PERMISSION_LOCATION_FROM_MAP_FRAGMENT && event.result == PackageManager.PERMISSION_GRANTED) {
            // TODO: check availability
            googleMap.isMyLocationEnabled = true
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location -> if (location != null) zoomToMyLocation(location) }
        }
    }

    companion object {
        const val PERMISSION_LOCATION_FROM_MAP_FRAGMENT = 1002
    }
}