package com.pavel.augmented.presentation.galerie

import com.pavel.augmented.di.AppModule
import org.koin.android.contextaware.ContextAwareFragment

class GalerieFragment : ContextAwareFragment() {
    override val contextName = AppModule.CTX_GALERIE_FRAGMENT
}