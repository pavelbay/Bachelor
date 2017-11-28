package com.pavel.augmented.presentation.galerie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import org.koin.android.contextaware.ContextAwareFragment
import org.koin.android.ext.android.inject

class GalerieFragment : ContextAwareFragment(), GalerieContract.View {
    override val contextName = AppModule.CTX_GALERIE_FRAGMENT

    override val presenter by inject<GalerieContract.Presenter>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.layout_galerie_fragment, container, false)
    }
}