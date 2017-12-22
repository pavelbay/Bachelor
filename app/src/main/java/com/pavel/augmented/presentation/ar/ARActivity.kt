package com.pavel.augmented.presentation.ar

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import cn.easyar.Engine
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.pavel.augmented.R
import com.pavel.augmented.customviews.GLView
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.BitmapLoaded
import com.pavel.augmented.util.GlideApp
import com.pavel.augmented.util.getModified
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.activity_ar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import org.koin.standalone.releaseContext

class ARActivity : AppCompatActivity(), ARContract.View {
    private val key = "JQHIN00Qmj3hEvW2C2AW1yWM2zHJBhFFZrfFASmMvUasnclxi11EbPBevQZhGVinPnrKKJynRKfQpR0n7brbcPN8IiV3KhB7ZI6n4yY1COunxg4jT2JiZS6pKf3pqniT7n1RaHN0nQYCMvS8c5POCvwdMZw6WZkoI88KWqIMzzJHNDKdgYGsJ8CFk4Ve6r9jNrFzBBaJ"

    private val contextName = AppModule.CTX_AR_ACTIVITY

    private var originLoaded = false

    override val presenter by inject<ARContract.Presenter>()

    private var glView: GLView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ar)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!Engine.initialize(this, key)) {
            Log.e(TAG, "EasyAR initialization Failed.")
        }

        loadModifiedFile()
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

    private fun loadModifiedFile() {
        val file = getModified(this)
        GlideApp
                .with(preview)
                .asBitmap()
                .load(file)
                .signature(ObjectKey(file.lastModified()))
                .into(ModifiedViewTarget(preview))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBitmapLoaded(event: BitmapLoaded) {
        GlideApp.get(this).clearMemory()
    }

    inner class ModifiedViewTarget(frameLayout: FrameLayout) : com.bumptech.glide.request.target.ViewTarget<FrameLayout, Bitmap>(frameLayout) {
        override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
            if (resource != null) {
                originLoaded = true
                Log.d(TAG, "Bitmap loaded")

                glView = GLView(resource, this@ARActivity)

                requestCameraPermission(object : PermissionCallback {
                    override fun onSuccess() {
                        preview.addView(glView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                    }

                    override fun onFailure() {}
                })
            } else {
                Toast.makeText(this@ARActivity, "Cannot load origin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private interface PermissionCallback {
        fun onSuccess()
        fun onFailure()
    }

    private val permissionCallbacks = HashMap<Int, PermissionCallback>()
    private var permissionRequestCodeSerial = 0
    @TargetApi(23)
    private fun requestCameraPermission(callback: PermissionCallback) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                val requestCode = permissionRequestCodeSerial
                permissionRequestCodeSerial += 1
                permissionCallbacks.put(requestCode, callback)
                requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
            } else {
                callback.onSuccess()
            }
        } else {
            callback.onSuccess()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (permissionCallbacks.containsKey(requestCode)) {
            val callback = permissionCallbacks[requestCode]!!
            permissionCallbacks.remove(requestCode)
            var executed = false
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true
                    callback.onFailure()
                }
            }
            if (!executed) {
                callback.onSuccess()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private val TAG = ARActivity::class.java.simpleName
    }
}