package com.pavel.augmented.presentation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.pavel.augmented.R
import com.pavel.augmented.presentation.pageradapter.MainPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val pagerAdapter: MainPagerAdapter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getKoin().setProperty(FRAGMENT_MANAGER_KEY, supportFragmentManager)
        getKoin().setProperty(FRAGMENT_NAMES_KEY, resources.getStringArray(R.array.main_view_pager_items))

        main_view_pager.adapter = pagerAdapter
        main_view_pager.setOnTouchListener({ _, _ -> true })

        bottom_navigation_view.setOnNavigationItemSelectedListener { item ->
            val index = pagerAdapter.getPositionForTitle(item.title.toString())
            if (index != -1) {
                main_view_pager.currentItem = pagerAdapter.getPositionForTitle(item.title.toString())
                return@setOnNavigationItemSelectedListener true
            }

            return@setOnNavigationItemSelectedListener false
        }

//        val colorPicker = ColorPicker(this, Color.alpha(DEFAULT_COLOR), Color.red(DEFAULT_COLOR), Color.green(DEFAULT_COLOR), Color.blue(DEFAULT_COLOR))
//        colorPicker.setCallback { color ->
//            mDrawingView.setColor(color)
//            colorPicker.dismiss()
//        }
        //            colorPicker.show()
    }

    companion object {
        const val FRAGMENT_MANAGER_KEY = "FragmentManagerKey"
        const val FRAGMENT_NAMES_KEY = "FragmentNamesKey"
    }
}