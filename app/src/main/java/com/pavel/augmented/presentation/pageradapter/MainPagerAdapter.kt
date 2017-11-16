package com.pavel.augmented.presentation.pageradapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.pavel.augmented.presentation.canvas.CanvasFragment
import com.pavel.augmented.presentation.galerie.GalerieFragment
import com.pavel.augmented.presentation.map.MapFragment


class MainPagerAdapter(fragmentManager: FragmentManager, private val names: Array<String>) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int = names.size

    override fun getItem(position: Int): Fragment = when (position) {
        0 -> GalerieFragment()
        1 -> CanvasFragment()
        else -> MapFragment()
    }

    fun getPositionForTitle(title: String) = names.indexOf(title)
}