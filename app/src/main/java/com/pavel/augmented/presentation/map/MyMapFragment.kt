package com.pavel.augmented.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import org.koin.android.contextaware.ContextAwareFragment

class MyMapFragment : ContextAwareFragment() {
    override val contextName = AppModule.CTX_MAP_FRAGMENT

    lateinit var mapFragment: SupportMapFragment

    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.layout_map_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.google_map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
        }
    }
}