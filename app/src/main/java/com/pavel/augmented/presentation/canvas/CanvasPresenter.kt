package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import android.util.Log
import com.pavel.augmented.storage.FileStore

private val TAG = CanvasPresenter::class.java.simpleName

class CanvasPresenter(private val fileStore : FileStore<Bitmap>) : CanvasContract.Presenter {
    override lateinit var view: CanvasContract.View

    override fun start() {
        Log.d(TAG, "Presenter start: " + (fileStore != null))
    }

    override fun stop() {
    }

    override fun saveToGallery(bitmap: Bitmap?) {
        bitmap?.let {
            fileStore.saveType(bitmap)
        }
    }

    override fun publishSketch() {
    }
}