package com.pavel.augmented.presentation.canvas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.pavel.augmented.R
import com.pavel.augmented.customviews.DrawingView
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.ColorPickerEvents
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.events.SketchEvents
import com.pavel.augmented.events.SketchNameChosenEvent
import com.pavel.augmented.fragments.ColorPickerDialogFragment
import com.pavel.augmented.fragments.EditTextDialogFragment
import com.pavel.augmented.presentation.MainActivity
import com.pavel.augmented.util.*
import kotlinx.android.synthetic.main.layout_canvas_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.standalone.releaseContext
import java.io.File

@Suppress("unused")
class CanvasFragment : Fragment(), CanvasContract.View {
    private val contextName = AppModule.CTX_CANVAS_FRAGMENT

    override val presenter by inject<CanvasContract.Presenter>()
    override var tempBitmapSaved: Boolean = false

    private var mode: Mode = Mode.VIEW
    private var menu: Menu? = null

    private val orientations: SparseIntArray = SparseIntArray(4)

    private var cameraId: String? = null
    private var imageDimension: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var captureRequest: CaptureRequest? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private var imageReader: ImageReader? = null
    private var cameraOpened = false
    private var textureAvailable = false
    private lateinit var orientationEventListener: OrientationEventListener
    private var currentOrientation: Int = 0

    private var permissionCameraGranted = false
    private var permissionStorageGranted = false

    inner class ViewTarget(drawingView: DrawingView) : com.bumptech.glide.request.target.ViewTarget<DrawingView, Bitmap>(drawingView) {
        override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
            resource?.let {
                this@CanvasFragment.drawing_view.updateBitmap(resource)
                this@CanvasFragment.drawing_view.requestLayout()
            }
        }
    }

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

    inner class OrientationEventListener : android.view.OrientationEventListener(context) {
        override fun onOrientationChanged(orientation: Int) {
            currentOrientation = orientation
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        permissionCameraGranted = checkPermission()
        orientationEventListener = OrientationEventListener()
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }

        return inflater?.inflate(R.layout.layout_canvas_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        drawing_view.setColor(DEFAULT_COLOR)
        main_activity_floating_action_button.setOnClickListener {
            if (mode == Mode.VIEW) {
                takePicture()
            } else {
                displayDialog()
            }
        }

        setupViewVisibility()
        texture_view.surfaceTextureListener = SurfaceTextureListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        orientationEventListener.disable()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        outState?.putInt(MODE_SAVE_STATE_KEY, mode.ordinal)
        outState?.putBoolean(TEMP_BITMAP_SAVED_KEY, tempBitmapSaved)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        savedInstanceState?.let {
            mode = Mode.values()[savedInstanceState.getInt(MODE_SAVE_STATE_KEY)]
            tempBitmapSaved = savedInstanceState.getBoolean(TEMP_BITMAP_SAVED_KEY, false)
        }
    }

//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//
//        if (tempBitmapSaved) {
//            loadImage()
//        }
//    }

    override fun onResume() {
        super.onResume()

        presenter.view = this
        presenter.start()

        if (mode == Mode.VIEW) {
            handleOpenCamera()
        }
    }

    override fun onPause() {
        releaseContext(contextName)
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        this.menu = menu
        setupMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.switch_mode -> {
                menu?.findItem(R.id.switch_mode)?.isEnabled = drawing_view.pictureAvailable || mode == Mode.DRAW
                if (drawing_view.pictureAvailable || mode == Mode.DRAW) {
                    changeMode()
                } else {
                    if (isAdded) {
                        Toast.makeText(context, R.string.message_no_available_picture, Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            R.id.save_to_gallery -> {
                val parentActivity = activity
                if (parentActivity is AppCompatActivity) {
                    permissionStorageGranted = !parentActivity.askForPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_FROM_CANVAS_FRAGMENT)
                    if (permissionStorageGranted) {
                        permissionStorageGranted = true
                        if (presenter.existedSketch == null) {
                            val dialogFragment =
                                    EditTextDialogFragment.newInstance(
                                            getString(R.string.title_name_dialog), getString(R.string.hint_name_dialog), 2, true
                                    )
                            dialogFragment.show(fragmentManager, NAME_DIALOG_TAG)
                        } else {
                            presenter.saveToGallery(null, drawing_view.bitmap)
                        }
                    }
                }
                true
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

    override fun context(): Context = context

//    private fun loadImage() {
//        GlideApp.with(drawing_view)
//                .asBitmap()
//                .load(File(getImagesFolder(context), CanvasPresenter.TEMP_SAVED_BITMAP_NAME + ".jpeg"))
//                .into(ViewTarget(drawing_view))
//    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun setupMenu() {
        menu?.findItem(R.id.save_to_gallery)?.isVisible = mode == Mode.DRAW
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (!cameraOpened && textureAvailable && permissionCameraGranted && mode == Mode.VIEW) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraId = cameraManager.cameraIdList[0]
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                configurationMap?.let {
                    imageDimension = configurationMap.getOutputSizes(SurfaceTexture::class.java)[0]
                }
                configureTransform(getJpegOrientation(characteristics, currentOrientation))
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice?) {
                        cameraOpened = true
                        cameraDevice = camera
                        createCameraPreview()
                    }

                    override fun onClosed(camera: CameraDevice?) {
                        super.onClosed(camera)

                        cameraOpened = false
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
                val outputSurfaces = arrayListOf<Surface>(reader.surface/*, Surface(texture_view.surfaceTexture)*/)
                val captBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captBuilder.addTarget(reader.surface)
                captBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                captBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(characteristics, currentOrientation))
                reader.setOnImageAvailableListener({
                    presenter.existedSketch = null
                    tempBitmapSaved = false
                    val image = it.acquireLatestImage()
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                    presenter.saveTempBitmap(bitmap, drawing_view.mWidth, drawing_view.mHeight)
                    drawing_view.updateBitmap(bitmap)
                }, backgroundHandler)
                val captureListener = object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(session: CameraCaptureSession?, request: CaptureRequest?, result: TotalCaptureResult?) {
                        super.onCaptureCompleted(session, request, result)
                        if (isAdded) {
                            activity.runOnUiThread({
                                changeMode()
                                setupMenu()
                                Toast.makeText(context, R.string.message_picture_has_been_taken, Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }
                cameraDevice!!.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(session: CameraCaptureSession?) {
                    }

                    override fun onConfigured(session: CameraCaptureSession?) {
                        try {
                            session?.capture(captBuilder.build(), captureListener, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, "Error accepting camera: " + e)
                        }
                    }
                }, backgroundHandler)
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Error while trying take picture: " + e)
            }
        }
    }

    private fun handleOpenCamera() {
        if (permissionCameraGranted) {
            startBackgroundThread()
            if (texture_view.isAvailable) {
                openCamera()
            } else {
                texture_view.surfaceTextureListener = SurfaceTextureListener()
            }
        }
    }

    private fun changeMode() {
        if (mode == Mode.VIEW) {
            mode = Mode.DRAW
            closeCamera()
            stopBackgroundThread()
        } else {
            mode = Mode.VIEW
            handleOpenCamera()
        }
        setupViewVisibility()
        setupMenu()
    }

    private fun setupViewVisibility() {
        if (mode == Mode.VIEW) {
            texture_view.visibility = View.VISIBLE
            drawing_view.isEnabled = false
        } else {
            texture_view.visibility = View.GONE
            drawing_view.isEnabled = true
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
            Log.e(TAG, "Error creating preview: " + e)
        }
    }

    private fun getJpegOrientation(c: CameraCharacteristics, deviceOrientation: Int): Int {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) return 0
        val sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION)

        // Round device orientation to a multiple of 90
        val deviceOrientation1 = (deviceOrientation + 45) / 90 * 90

        // Reverse device orientation for front-facing cameras
        val facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        if (facingFront) -deviceOrientation1

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientation1 + 360) % 360
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

//    private fun updateMatrix(width: Int, height: Int) {
//        if (textureAvailable) {
//            val matrix = Matrix()
//            val rotation = activity.windowManager.defaultDisplay.rotation
//            val textureRectF = RectF(0F, 0F, width.toFloat(), height.toFloat())
//            val previewRectF = RectF(0F, 0F, texture_view.height.toFloat(), texture_view.width.toFloat())
//            val centerX = textureRectF.centerX()
//            val centerY = textureRectF.centerY()
//            if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
//                previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY())
//                matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL)
//                val scale = Math.max(width.toFloat() / width, height.toFloat() / width)
//                matrix.postScale(scale, scale, centerX, centerY)
//                matrix.postRotate(90F * (rotation - 2), centerX, centerY)
//            }
//            texture_view.setTransform(matrix)
//        }
//    }

    private fun configureTransform(displayOrientation: Int) {
        val matrix = Matrix()
        if (displayOrientation == 180 || displayOrientation == 0) {
            val width = texture_view.width
            val height = texture_view.height
            // Rotate the camera preview when the screen is landscape.
            matrix.setPolyToPoly(
                    floatArrayOf(0f, 0f, // top left
                            width.toFloat(), 0f, // top right
                            0f, height.toFloat(), // bottom left
                            width.toFloat(), height.toFloat())// bottom right
                    , 0,
                    if (displayOrientation == 0)
                    // Clockwise
                        floatArrayOf(0f, height.toFloat(), // top left
                                0f, 0f, // top right
                                width.toFloat(), height.toFloat(), // bottom left
                                width.toFloat(), 0f)// bottom right
                    else
                    // displayOrientation == 180
                    // Counter-clockwise
                        floatArrayOf(width.toFloat(), 0f, // top left
                                width.toFloat(), height.toFloat(), // top right
                                0f, 0f, // bottom left
                                0f, height.toFloat())// bottom right
                    , 0,
                    4)
        }

        texture_view.setTransform(matrix)
    }

    private fun closeCamera() {
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        stopBackgroundThread()
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
    fun onSketchChosen(onSketchChosen: SketchEvents.OnSketchChosen) {
        val file = getTargetImageFile(context, onSketchChosen.sketch.id)
        GlideApp
                .with(drawing_view)
                .asBitmap()
                .load(file)
                .signature(ObjectKey(file.lastModified()))
                .into(ViewTarget(drawing_view))

        presenter.existedSketch = onSketchChosen.sketch

        if (mode == Mode.VIEW) {
            changeMode()
        }
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

    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        when (event.requestId) {
            MainActivity.PERMISSION_REQUEST_FROM_MAIN_ACTIVITY -> permissionCameraGranted = event.result == PackageManager.PERMISSION_GRANTED
            PERMISSION_REQUEST_FROM_CANVAS_FRAGMENT -> permissionStorageGranted = event.result == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        @ColorInt private const val DEFAULT_COLOR = Color.GREEN
        private const val COLOR_PICKER_DIALOG_TAG = "ColorPickerDialogTag"
        private const val NAME_DIALOG_TAG = "NameDialogTag"
        private val TAG = CanvasFragment::class.java.simpleName
        const val PERMISSION_REQUEST_FROM_CANVAS_FRAGMENT = 2
        private const val MODE_SAVE_STATE_KEY = "ModeSaveStateKey"
        private const val TEMP_BITMAP_SAVED_KEY = "TempBitmapSavedKey"
    }

    enum class Mode {
        VIEW,
        DRAW
    }
}