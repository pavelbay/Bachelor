package com.pavel.augmented.presentation

import android.os.Bundle
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule.Companion.CTX_MAIN_ACTIVITY
import com.pavel.augmented.presentation.pageradapter.MainPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.contextaware.ContextAwareActivity
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject

class MainActivity : ContextAwareActivity() {

    override val contextName = CTX_MAIN_ACTIVITY

    private val pagerAdapter by inject<MainPagerAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getKoin().setProperty(FRAGMENT_MANAGER_KEY, supportFragmentManager)
        getKoin().setProperty(FRAGMENT_NAMES_KEY, resources.getStringArray(R.array.main_view_pager_items))

        main_view_pager.adapter = pagerAdapter

        supportActionBar?.title = pagerAdapter.getTitleForPosition(main_view_pager.currentItem)

        bottom_navigation_view.setOnNavigationItemSelectedListener { item ->
            val index = pagerAdapter.getPositionForTitle(item.title.toString())
            if (index != -1) {
                main_view_pager.currentItem = pagerAdapter.getPositionForTitle(item.title.toString())
                supportActionBar?.title = pagerAdapter.getTitleForPosition(main_view_pager.currentItem)
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