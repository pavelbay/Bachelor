package com.pavel.augmented.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule.Companion.CTX_MAIN_ACTIVITY
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.presentation.map.MyMapFragment.Companion.PERMISSION_LOCATION_FROM_MAP_FRAGMENT
import com.pavel.augmented.presentation.pageradapter.MainPagerAdapter
import com.pavel.augmented.util.askForPermissions
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.koin.android.contextaware.ContextAwareActivity
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.android.inject

class MainActivity : ContextAwareActivity() {

    override val contextName = CTX_MAIN_ACTIVITY

    private val pagerAdapter by inject<MainPagerAdapter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askForPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_INIT_MAIN_ACTIVITY)

        getKoin().setProperty(MAIN_ACTIVITY_CONTEXT, this)
        getKoin().setProperty(FRAGMENT_MANAGER_KEY, supportFragmentManager)
        getKoin().setProperty(FRAGMENT_NAMES_KEY, resources.getStringArray(R.array.main_view_pager_items))

        setUpViewPager()

//        val colorPicker = ColorPicker(this, Color.alpha(DEFAULT_COLOR), Color.red(DEFAULT_COLOR), Color.green(DEFAULT_COLOR), Color.blue(DEFAULT_COLOR))
//        colorPicker.setCallback { color ->
//            mDrawingView.setColor(color)
//            colorPicker.dismiss()
//        }
        //            colorPicker.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_INIT_MAIN_ACTIVITY -> {
                var granted = true
                grantResults
                        .filter { it != PackageManager.PERMISSION_GRANTED }
                        .forEach {
                            // TODO: display an error
                            Toast.makeText(this, getString(R.string.no_permissions_granted), Toast.LENGTH_SHORT).show()
                            granted = false
                        }

                if (!granted) {
                    finish()
                }
            }
            PERMISSION_LOCATION_FROM_MAP_FRAGMENT -> {
                if (grantResults.isNotEmpty() ) {
                    EventBus.getDefault().post(PermissionsEvent(grantResults[0]))
                }
            }
        }
    }

    private fun setUpViewPager() {
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
    }

    companion object {
        const val FRAGMENT_MANAGER_KEY = "FragmentManagerKey"
        const val FRAGMENT_NAMES_KEY = "FragmentNamesKey"
        const val MAIN_ACTIVITY_CONTEXT = "MainActivityContext"

        const val PERMISSIONS_INIT_MAIN_ACTIVITY = 1001
        const val PERMISSIONS_CAMERA = 1003
    }
}