package com.pavel.augmented.presentation.pageradapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.pavel.augmented.presentation.canvas.NewSketchFragment
import com.pavel.augmented.presentation.galerie.GalerieFragment
import com.pavel.augmented.presentation.map.MyMapFragment

class MainPagerAdapter(fragmentManager: FragmentManager, private val names: Array<String>) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int = names.size

    private val fragments = arrayListOf<Fragment>(GalerieFragment(), NewSketchFragment(), MyMapFragment())

    override fun getItem(position: Int): Fragment = fragments[position]

    fun getPositionForTitle(title: String) = names.indexOf(title)

    fun getTitleForPosition(position: Int): String? {
        return if (position >= 0 && position < names.size) {
            names[position]
        } else {
            null
        }
    }
}