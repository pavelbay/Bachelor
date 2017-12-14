package com.pavel.augmented.presentation

import android.Manifest
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule.Companion.CTX_MAIN_ACTIVITY
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.events.SketchEvents
import com.pavel.augmented.presentation.pageradapter.MainPagerAdapter
import com.pavel.augmented.util.askForPermissions
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.contextaware.ContextAwareActivity
import org.koin.android.ext.android.getKoin

class MainActivity : ContextAwareActivity() {

    override val contextName = CTX_MAIN_ACTIVITY

    private lateinit var pagerAdapter: MainPagerAdapter
    private var restoredCurrentItem: Int = 0
    private var askedForPermission = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_activity_toolbar)

        pagerAdapter = MainPagerAdapter(supportFragmentManager, resources.getStringArray(R.array.main_view_pager_items))

        savedInstanceState?.let {
            restoredCurrentItem = savedInstanceState.getInt(VIEW_PAGER_CURRENT_ITEM_KEY, 0)
        }

        getKoin().setProperty(MAIN_ACTIVITY_CONTEXT, this)

        setUpViewPager()

//        val colorPicker = ColorPicker(this, Color.alpha(DEFAULT_COLOR), Color.red(DEFAULT_COLOR), Color.green(DEFAULT_COLOR), Color.blue(DEFAULT_COLOR))
//        colorPicker.setCallback { color ->
//            mDrawingView.setColor(color)
//            colorPicker.dismiss()
//        }
        //            colorPicker.show()
    }

    override fun onResume() {
        super.onResume()

        if (!askedForPermission) {
            askForPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA), PERMISSION_REQUEST_FROM_MAIN_ACTIVITY)
            askedForPermission = true
        }
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putInt(VIEW_PAGER_CURRENT_ITEM_KEY, main_view_pager.currentItem)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty()) {
            EventBus.getDefault().post(PermissionsEvent(grantResults[0], requestCode))
        }
//        when (requestCode) {
//            PERMISSIONS_INIT_MAIN_ACTIVITY -> {
//                var granted = true
//                grantResults
//                        .filter { it != PackageManager.PERMISSION_GRANTED }
//                        .forEach {
//                            // TODO: display an error
//                            Toast.makeText(this, getString(R.string.no_permissions_granted), Toast.LENGTH_SHORT).show()
//                            granted = false
//                        }
//
//                if (!granted) {
//                    finish()
//                }
//            }
//            else -> {
//                if (grantResults.isNotEmpty()) {
//                    EventBus.getDefault().post(PermissionsEvent(grantResults[0], requestCode))
//                }
//            }
//        }
    }

    private fun setUpViewPager() {
        main_view_pager.adapter = pagerAdapter

        main_view_pager.currentItem = restoredCurrentItem

        main_view_pager.offscreenPageLimit = PAGER_OFFSCREEN_PAGE_LIMIT

        main_view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
//                when (position) {
//                    0 -> askForPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_STORAGE_FROM_GALERIE_FRAGMENT)
//                    1 -> askForPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_FROM_CANVAS_FRAGMENT)
//                    2 -> askForPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_LOCATION_FROM_MAP_FRAGMENT)
//                }
            }
        })

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

    @Subscribe
    fun onSketchChosen(onSketchChosen: SketchEvents.OnSketchChosen) {
        bottom_navigation_view.selectedItemId = R.id.action_canvas
    }

    companion object {
        const val FRAGMENT_MANAGER_KEY = "FragmentManagerKey"
        const val FRAGMENT_NAMES_KEY = "FragmentNamesKey"
        const val MAIN_ACTIVITY_CONTEXT = "MainActivityContext"
        const val PERMISSION_REQUEST_FROM_MAIN_ACTIVITY = 1
        private const val PAGER_OFFSCREEN_PAGE_LIMIT = 2
        private const val VIEW_PAGER_CURRENT_ITEM_KEY = "ViewPagerCurrentItemKey"
    }
}