package com.example.pavel.myapplication.activities.canvas

import android.graphics.Bitmap
import android.util.Log
import com.example.pavel.myapplication.storage.FileStore

private val TAG = MainActivityPresenter::class.java.simpleName

class MainActivityPresenter(private val fileStore : FileStore<Bitmap>) : MainActivityContract.Presenter {
    override lateinit var view: MainActivityContract.View

    override fun start() {
        Log.d(TAG, "Presenter start: " + (fileStore != null))
    }

    override fun stop() {
    }

    override fun saveToGallery() {
    }

    override fun publishSketch() {
    }
}