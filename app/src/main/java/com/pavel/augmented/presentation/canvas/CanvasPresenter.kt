package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.repository.SketchRepository
import com.pavel.augmented.util.getOriginImageFile
import com.pavel.augmented.util.getTmpImageFile
import io.reactivex.Observable
import io.reactivex.disposables.Disposable


class CanvasPresenter(
        private val sketchRepository: SketchRepository,
        private val fusedLocationProviderClient: FusedLocationProviderClient
) : CanvasContract.Presenter {
    override lateinit var view: CanvasContract.View

    private var currentRequest: Disposable? = null

    override var existedSketch: Sketch? = null

    override fun start() {
    }

    override fun stop() {
    }

    override fun saveToGallery(name: String?, bitmap: Bitmap?) {
        try {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                sketchRepository.saveToGallery(name, bitmap, existedSketch, location, { sketch ->
                    existedSketch = sketch
                })
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission for getting location")
        }
//            sketchRepository.saveToGallery()
//            //currentRequest?.dispose()
//            if (name != null) {
//                sketchDao.findSketchByNameAsync(name)
//                        .subscribeOn(schedulerProvider.io())
//                        .observeOn(schedulerProvider.ui())
//                        .subscribe { sketch: Sketch?, _: Throwable? ->
//                            if (sketch != null) {
//                                view.displayMessageSketchWithNameAlreadyExists()
//                            } else {
//                                createNewSketch(name, bitmap)
//                            }
//                        }
//            } else if (existedSketch != null) {
//                performSave(existedSketch!!, bitmap)
//            }
        }

        override fun saveTempBitmap(bitmap: Bitmap?, width: Int, height: Int) {
            sketchRepository.saveTempBitmap(bitmap, width, height)
        }

//    private fun createNewSketch(name: String, bitmap: Bitmap?) {
//        try {
//            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//                if (location != null) {
//                    val sketch = Sketch(id = generateUniqueId()?.toString() ?: "0", name = name, latitude = location.latitude, longitude = location.longitude)
//                    performSave(sketch, bitmap)
//                    existedSketch = sketch
//                } else {
//                    view.displayMessageCannotCreateSketch()
//                }
//            }
//        } catch (e: SecurityException) {
//            Log.e(TAG, "No permission for getting location")
//        }
//    }

//    private fun generateUniqueId(): Long? {
//        var `val`: Long
//        do {
//            val uid = UUID.randomUUID()
//            val buffer = ByteBuffer.wrap(ByteArray(16))
//            buffer.putLong(uid.leastSignificantBits)
//            buffer.putLong(uid.mostSignificantBits)
//            val bi = BigInteger(buffer.array())
//            `val` = bi.toLong()
//        } while (`val` < 0)
//        return `val`
//    }

        override fun publishSketch() {
        }

        companion object {
            private val TAG = CanvasPresenter::class.java.simpleName
            const val TMP_BITMAP = "temp"
        }
    }