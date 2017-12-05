package com.pavel.augmented.repository

import android.graphics.Bitmap
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.network.SketchUploadService
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import io.reactivex.Observable

class SketchRepository(private val schedulerProvider: SchedulerProvider,
                       private val sketchDao: SketchDao,
                       private val fileStore: FileStore<Bitmap>,
                       private val sketchUploadService: SketchUploadService) {

    fun loadSketches(callback: (sketches: Array<Sketch>) -> Unit) {
        Observable.fromCallable { sketchDao.loadAllSketches() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe { sketches ->
                    callback(sketches)
                }
    }

    fun deleteSketches(sketches: Array<Sketch?>, callback: () -> Unit) {
        Observable.fromCallable {
            sketches.forEach { sketch ->
                sketch?.let {
                    fileStore.deleteType(sketch.name)
                }

            }
            sketchDao.deleteSketches(sketches)
        }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    callback()
                }
    }
}