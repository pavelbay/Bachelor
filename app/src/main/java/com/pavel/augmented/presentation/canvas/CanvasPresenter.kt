package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*


class CanvasPresenter(
        private val fileStore: FileStore<Bitmap>,
        private val schedulerProvider: SchedulerProvider,
        private val fusedLocationProviderClient: FusedLocationProviderClient,
        private val sketchDao: SketchDao
) : CanvasContract.Presenter {
    override lateinit var view: CanvasContract.View

    private var currentRequest: Disposable? = null

    override var existedSketch: Sketch? = null

    override fun start() {
        Log.d(TAG, "Presenter start: " + (fileStore != null))
    }

    override fun stop() {
    }

    override fun saveToGallery(name: String?, bitmap: Bitmap?) {
        //currentRequest?.dispose()
        if (name != null) {
            sketchDao.findSketchByNameAsync(name)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { sketch: Sketch?, _: Throwable? ->
                        if (sketch != null) {
                            view.displayMessageSketchWithNameAlreadyExists()
                        } else {
                            createNewSketch(name, bitmap)
                        }
                    }
        } else if (existedSketch != null){
            performSave(existedSketch!!, bitmap)
        }
    }

    override fun saveTempBitmap(bitmap: Bitmap?) {
        bitmap?.let {
            Observable.fromCallable { fileStore.saveType(bitmap, TEMP_SAVED_BITMAP_NAME) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { view.tempBitmapSaved = true }
        }
    }

    private fun createNewSketch(name: String, bitmap: Bitmap?) {
        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val sketch = Sketch(id = generateUniqueId() ?: 0, name = name, latitude = location.latitude, longitude = location.longitude)
                    performSave(sketch, bitmap)
                    existedSketch = sketch
                } else {
                    view.displayMessageCannotCreateSketch()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission for getting location")
        }
    }

    private fun generateUniqueId(): Long? {
        var `val`: Long
        do {
            val uid = UUID.randomUUID()
            val buffer = ByteBuffer.wrap(ByteArray(16))
            buffer.putLong(uid.leastSignificantBits)
            buffer.putLong(uid.mostSignificantBits)
            val bi = BigInteger(buffer.array())
            `val` = bi.toLong()
        } while (`val` < 0)
        return `val`
    }

    private fun performSave(sketch: Sketch, bitmap: Bitmap?) {
        if (sketch != existedSketch) {
            Observable
                    .fromCallable { sketchDao.insertSketch(sketch) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { Log.d(TAG, "Sketch: $sketch saved to db") }
        }

//        fun delete() = fileStore.deleteType(existedSketch?.name)

        bitmap?.let {
            //currentRequest?.dispose()
            Observable
                    .fromCallable { fileStore.saveType(bitmap, sketch.name) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { view.displayMessageSavedToGallery() }
        }
    }

    override fun publishSketch() {
    }

    companion object {
        private val TAG = CanvasPresenter::class.java.simpleName
        const val TEMP_SAVED_BITMAP_NAME = "TempSavedBitmapName"
    }
}