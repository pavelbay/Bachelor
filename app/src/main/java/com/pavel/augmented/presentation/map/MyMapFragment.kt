package com.pavel.augmented.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.*
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.model.Sketch
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

    private var markers: MutableList<Marker> = ArrayList()

    private val fusedLocationProviderClient by inject<FusedLocationProviderClient>()

    private var permissionGranted = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        permissionGranted = checkPermission()
        setHasOptionsMenu(true)

        return inflater?.inflate(R.layout.layout_map_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (!permissionGranted) {
//            val parentActivity = activity
//            if (parentActivity is AppCompatActivity) {
//                parentActivity.askForPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_FROM_MAP_FRAGMENT)
//            }
//        }
        mapFragment = childFragmentManager.findFragmentById(R.id.google_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            if (permissionGranted) {
                setupMap()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.map_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_map_refresh -> {
                presenter.fetchSketches()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun updateMarkers(sketches: List<Sketch>?) {
        markers.forEach(Marker::remove)
        markers.clear()
        sketches?.forEach {
            val marker = googleMap.addMarker(MarkerOptions()
                    .position(LatLng(it.latitude, it.longitude))
                    .title(it.name))
            marker.tag = it.id
            markers.add(marker)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location -> if (location != null) zoomToMyLocation(location) }
        googleMap.setOnMarkerClickListener {
            presenter.fetchImages(it.tag as String)
            presenter.currentTargetId = it.tag as String
            if (isAdded) {
                Toast.makeText(context, String.format(getString(R.string.map_set_as_target), it.title), Toast.LENGTH_SHORT).show()
            }
            return@setOnMarkerClickListener true
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

    private fun checkPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        permissionGranted = checkPermission()
        if (permissionGranted) {
            setupMap()
        }
    }

    companion object {
        const val PERMISSION_LOCATION_FROM_MAP_FRAGMENT = 1003
    }
}