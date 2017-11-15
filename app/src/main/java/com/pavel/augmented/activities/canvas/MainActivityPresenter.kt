package com.pavel.augmented.activities.canvas

import android.graphics.Bitmap
import android.util.Log
import com.pavel.augmented.storage.FileStore

private val TAG = MainActivityPresenter::class.java.simpleName

class MainActivityPresenter(private val fileStore : FileStore<Bitmap>) : MainActivityContract.Presenter {
    override lateinit var view: MainActivityContract.View

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