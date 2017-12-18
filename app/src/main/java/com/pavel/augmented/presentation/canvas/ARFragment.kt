package com.pavel.augmented.presentation.canvas

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.pavel.augmented.R
import com.pavel.augmented.customviews.GLView
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.OnTargetChanged
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.opengl.MyGL20Renderer
import com.pavel.augmented.opengl.Sprite
import com.pavel.augmented.presentation.MainActivity
import com.pavel.augmented.util.GlideApp
import com.pavel.augmented.util.getImageFile
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.layout_ar_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.standalone.releaseContext

class ARFragment : Fragment(), CanvasContract.View {
    private val contextName = AppModule.CTX_AR_FRAGMENT

    override var tempBitmapSaved = false

    override val presenter by inject<CanvasContract.Presenter>()

    private var permissionCameraGranted = false

    inner class ViewTarget(glView: GLView) : com.bumptech.glide.request.target.ViewTarget<GLView, Bitmap>(gl_surface_view) {
        override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
            resource?.let {
                Log.d(TAG, "Bitmap loaded")
                this@ARFragment.gl_surface_view.bitmap = resource
                this@ARFragment.onTargetChanged(com.pavel.augmented.events.OnTargetChanged())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        permissionCameraGranted = checkPermission()

        return inflater?.inflate(R.layout.layout_ar_fragment, container, false)

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (permissionCameraGranted) {
            gl_surface_view.visibility = View.VISIBLE
            val file = getImageFile(context, "test.png")
            GlideApp
                    .with(drawing_view)
                    .asBitmap()
                    .load(file)
                    .signature(ObjectKey(file.lastModified()))
                    .into(ViewTarget(gl_surface_view))
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.view = this
        presenter.start()
    }

    override fun onPause() {
        releaseContext(contextName)
        super.onPause()
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

    @Subscribe
    fun onTargetChanged(event: OnTargetChanged) {
        gl_surface_view.onTargetChanged(presenter.getJsonTarget())

    }

    companion object {
        private val TAG = ARFragment::class.java.simpleName
    }
}