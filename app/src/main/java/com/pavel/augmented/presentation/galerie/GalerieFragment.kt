package com.pavel.augmented.presentation.galerie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import org.koin.android.contextaware.ContextAwareFragment

class GalerieFragment : ContextAwareFragment() {
    override val contextName = AppModule.CTX_GALERIE_FRAGMENT

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.layout_galerie_fragment, container, false)
    }
}