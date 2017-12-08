package com.pavel.augmented.presentation.canvas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.ColorPickerEvents
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.events.SketchNameChosenEvent
import com.pavel.augmented.fragments.ColorPickerDialogFragment
import com.pavel.augmented.fragments.EditTextDialogFragment
import com.pavel.augmented.util.askForPermissions
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.layout_canvas_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.standalone.releaseContext

@Suppress("unused")
class CanvasFragment : Fragment(), CanvasContract.View {
    private val contextName = AppModule.CTX_CANVAS_FRAGMENT

    override val presenter by inject<CanvasContract.Presenter>()

    private var cameraId: String? = null
    private var imageDimension: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var captureRequest: CaptureRequest? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var imageReader: ImageReader? = null
    private var permissionGranted = false
    private var cameraOpened = false
    private var textureAvailable = false

    inner class SurfaceTextureListener : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            textureAvailable = true
            this@CanvasFragment.openCamera()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            textureAvailable = false
            return false
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            // TODO: handle it
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            // Don't know if i need to do something about it
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.layout_canvas_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val parentActivity = activity
        if (parentActivity is AppCompatActivity) {
            parentActivity.askForPermissions(arrayOf(Manifest.permission.CAMERA), PERMISSION_LOCATION_FROM_CANVAS_FRAGMENT)
        }
        drawing_view.setColor(DEFAULT_COLOR)
        main_activity_floating_action_button.setOnClickListener {
            displayDialog()
        }

        texture_view.surfaceTextureListener = SurfaceTextureListener()
    }

    override fun onResume() {
        super.onResume()

        presenter.view = this
        presenter.start()

        startBackgroundThread()
        if (texture_view.isAvailable) {
            openCamera()
        } else {
            texture_view.surfaceTextureListener = SurfaceTextureListener()
        }
    }

    override fun onPause() {
        releaseContext(contextName)
        stopBackgroundThread()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.eraser -> {
                drawing_view.enableEraser()
                true
            }

            R.id.save_to_gallery -> {
                // TODO: permissions check
                val dialogFragment =
                        EditTextDialogFragment.newInstance(
                                getString(R.string.title_name_dialog), getString(R.string.hint_name_dialog), 2, true
                        )
                dialogFragment.show(fragmentManager, NAME_DIALOG_TAG)
                true
            }

            R.id.clear_canvas -> {
                drawing_view.clear()
                // TODO: implement this
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun displayMessageSavedToGallery() = Toast.makeText(context, getString(R.string.message_saved_to_gallery), Toast.LENGTH_SHORT).show()

    override fun displayDialog() {
        val fragmentTransaction = fragmentManager.beginTransaction()
        removeDialogIfExists(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)

        val colorPickerDialog = ColorPickerDialogFragment.newInstance(R.layout.layout_color_picker, R.id.color_picker_ok_button, drawing_view.getColor())
        colorPickerDialog.show(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)
    }

    override fun displayMessageCannotCreateSketch() =
            Toast.makeText(context, R.string.message_cannot_create_sketch, Toast.LENGTH_SHORT).show()

    override fun displayMessageSketchWithNameAlreadyExists() =
            Toast.makeText(context, R.string.message_sketch_name_exists, Toast.LENGTH_SHORT).show()

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }


    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (!cameraOpened && textureAvailable && permissionGranted) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraId = cameraManager.cameraIdList[0]
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                configurationMap?.let {
                    imageDimension = configurationMap.getOutputSizes(SurfaceTexture::class.java)[0]
                }
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice?) {
                        cameraOpened = true
                        cameraDevice = camera
                        createCameraPreview()
                    }

                    override fun onDisconnected(camera: CameraDevice?) {
                        cameraDevice?.close()
                    }

                    override fun onError(camera: CameraDevice?, error: Int) {
                        cameraDevice?.close()
                        cameraDevice = null
                    }
                }, null)
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Cannot access Camera: " + e)
            }
        }
    }

    private fun takePicture() {
        cameraDevice?.let {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                val characteristics = cameraManager.getCameraCharacteristics(cameraDevice?.id)
                val size = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(ImageFormat.JPEG)
                var width = 640
                var height = 480
                size?.let {
                    if (size.isNotEmpty()) {
                        width = size[0].width
                        height = size[0].height
                    }
                }
                val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
                val outputSurfaces = arrayListOf<Surface>(reader.surface, Surface(texture_view.surfaceTexture))
                // TODO: do it later
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error while trying take picture: " + e)
            }
        }
    }

    private fun createCameraPreview() {
        try {
            val texture = texture_view.surfaceTexture
            imageDimension?.let {
                texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            }
            val surface = Surface(texture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(surface)
            cameraDevice?.createCaptureSession(mutableListOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {
                    Log.d(TAG, "Camera configuration failed")
                }

                override fun onConfigured(session: CameraCaptureSession?) {
                    cameraDevice?.let {
                        cameraCaptureSession = session
                        updatePreview()
                    }
                }
            }, null)

        } catch (e: CameraAccessException) {

        }
    }

    private fun updatePreview() {
        cameraDevice?.let {
            captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            try {
                cameraCaptureSession?.setRepeatingRequest(captureRequestBuilder?.build(), null, backgroundHandler)
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error while updating preview: " + e)
            }
        }
    }

    private fun closeCamera() {
        cameraOpened = false
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera background")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, "Exception while stopping thread: " + e)
        }
    }

    private fun removeDialogIfExists(fragmentTransaction: FragmentTransaction?, tag: String) {
        val previousFragment = fragmentManager.findFragmentByTag(tag)
        previousFragment?.let {
            fragmentTransaction?.remove(previousFragment)
        }
    }

    @Subscribe
    fun onSketchNameChosen(sketchNameChosenEvent: SketchNameChosenEvent) {
        presenter.saveToGallery(sketchNameChosenEvent.name, drawing_view.bitmap)
    }

    @Subscribe
    fun onColorPickerDialogDismiss(ignore: ColorPickerEvents.ColorPickerOkButtonEvent) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        removeDialogIfExists(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)
        fragmentTransaction.commit()
    }

    @Subscribe
    fun onColorSelectedEvent(colorSelectedEvent: ColorPickerEvents.ColorSelectedEvent) {
        drawing_view.setColor(colorSelectedEvent.color)
    }

    @SuppressLint("MissingPermission")
    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        if (event.requestId == PERMISSION_LOCATION_FROM_CANVAS_FRAGMENT) {
            permissionGranted = event.result == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        @ColorInt private const val DEFAULT_COLOR = Color.GREEN
        private const val COLOR_PICKER_DIALOG_TAG = "ColorPickerDialogTag"
        private const val NAME_DIALOG_TAG = "NameDialogTag"
        private val TAG = CanvasFragment::class.java.simpleName
        const val PERMISSION_LOCATION_FROM_CANVAS_FRAGMENT = 1003
    }
}


