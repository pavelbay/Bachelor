package com.pavel.augmented.presentation.canvas

import android.graphics.Bitmap
import android.util.Log
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import io.reactivex.Observable
import io.reactivex.disposables.Disposable


class CanvasPresenter(private val fileStore: FileStore<Bitmap>, private val schedulerProvider: SchedulerProvider) : CanvasContract.Presenter {
    override lateinit var view: CanvasContract.View

    private var currentRequest: Disposable? = null

    override fun start() {
        Log.d(TAG, "Presenter start: " + (fileStore != null))
    }

    override fun stop() {
    }

    override fun saveToGallery(bitmap: Bitmap?) {
        bitmap?.let {
            currentRequest?.dispose()
            currentRequest = Observable
                    .just(fileStore.saveType(bitmap))
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { view.displayMessageSavedToGallery() }
        }
    }

    override fun publishSketch() {
    }

    companion object {
        private val TAG = CanvasPresenter::class.java.simpleName

    }
}