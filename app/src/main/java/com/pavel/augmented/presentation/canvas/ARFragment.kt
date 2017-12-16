package com.pavel.augmented.presentation.canvas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.presentation.MainActivity
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.layout_ar_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject

class ARFragment : Fragment(), CanvasContract.View {
    private val contextName = AppModule.CTX_AR_FRAGMENT

    override var tempBitmapSaved = false

    override val presenter by inject<CanvasContract.Presenter>()

    private var permissionCameraGranted = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        permissionCameraGranted = checkPermission()

        if (permissionCameraGranted) {
            gl_surface_view.visibility = View.VISIBLE
        }

        return inflater?.inflate(R.layout.layout_ar_fragment, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }


    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun displayDialog() {
    }

    override fun displayMessageCannotCreateSketch() {
    }

    override fun displayMessageSavedToGallery() {
    }

    override fun displayMessageSketchWithNameAlreadyExists() {
    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        when (event.requestId) {
            MainActivity.PERMISSION_REQUEST_FROM_MAIN_ACTIVITY -> {
                permissionCameraGranted = event.result == PackageManager.PERMISSION_GRANTED
                if (permissionCameraGranted) {
                    gl_surface_view.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private val TAG = ARFragment::class.java.simpleName
    }
}