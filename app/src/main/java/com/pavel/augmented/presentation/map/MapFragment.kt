package com.pavel.augmented.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import org.koin.android.contextaware.ContextAwareFragment

class MapFragment : ContextAwareFragment() {
    override val contextName = AppModule.CTX_MAP_FRAGMENT

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.layout_map_fragment, container, false)
    }
}