package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.events.NewSketchAvailableEvent
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus


class CanvasPresenter(
        private val fileStore: FileStore<Bitmap>,
        private val schedulerProvider: SchedulerProvider,
        private val fusedLocationProviderClient: FusedLocationProviderClient,
        private val sketchDao: SketchDao
) : CanvasContract.Presenter {
    override lateinit var view: CanvasContract.View

    private var currentRequest: Disposable? = null

    override fun start() {
        Log.d(TAG, "Presenter start: " + (fileStore != null))
    }

    override fun stop() {
    }

    override fun saveToGallery(name: String, bitmap: Bitmap?) {
        //currentRequest?.dispose()
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
    }

    private fun createNewSketch(name: String, bitmap: Bitmap?) {
        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val sketch = Sketch(name = name, latitude = location.latitude, longitude = location.longitude)
                    performSave(sketch, bitmap)
                } else {
                    view.displayMessageCannotCreateSketch()
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission for getting location")
        }
    }

    private fun performSave(sketch: Sketch, bitmap: Bitmap?) {
        Observable
                .fromCallable { sketchDao.insertSketch(sketch) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe { Log.d(TAG, "Sketch: $sketch saved to db") }

        bitmap?.let {
            //currentRequest?.dispose()
            Observable
                    .fromCallable { fileStore.saveType(bitmap, sketch.name) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe {
                        view.displayMessageSavedToGallery()
                    }
        }
    }

    override fun publishSketch() {
    }

    companion object {
        private val TAG = CanvasPresenter::class.java.simpleName

    }
}