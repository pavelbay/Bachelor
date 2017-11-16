package com.pavel.augmented.presentation.map

import com.pavel.augmented.di.AppModule
import org.koin.android.contextaware.ContextAwareFragment

class MapFragment : ContextAwareFragment() {
    override val contextName = AppModule.CTX_MAP_FRAGMENT
}