package com.pavel.augmented.presentation.pageradapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import com.pavel.augmented.presentation.canvas.CanvasFragment
import com.pavel.augmented.presentation.galerie.GalerieFragment
import com.pavel.augmented.presentation.map.MapFragment
import android.os.Parcelable




class MainPagerAdapter(fragmentManager: FragmentManager, private val names: Array<String>) : FragmentStatePagerAdapter(fragmentManager) {

    override fun getCount(): Int = names.size

    private val fragments = arrayListOf<Fragment>(GalerieFragment(), CanvasFragment(), MapFragment())

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