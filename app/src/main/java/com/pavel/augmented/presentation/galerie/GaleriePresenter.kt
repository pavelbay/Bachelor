package com.pavel.augmented.presentation.galerie

import android.graphics.Bitmap
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.network.SketchUploadService
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import io.reactivex.Observable

class GaleriePresenter(private val schedulerProvider: SchedulerProvider,
                       private val sketchDao: SketchDao,
                       private val fileStore: FileStore<Bitmap>,
                       private val sketchUploadService: SketchUploadService) : GalerieContract.Presenter {

    override lateinit var view: GalerieContract.View

    private var currentSketches: Array<Sketch>? = null

    override fun start() {
    }

    override fun stop() {
    }

    override fun loadSketches() {
        Observable.fromCallable { sketchDao.loadAllSketches() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe { sketches ->
                    currentSketches = sketches
                    view.displaySketches(sketches.toCollection(ArrayList()))
                }
    }

    override fun publicSketch() {

    }

    override fun deleteSketches(sketches: Array<Sketch?>) {
        Observable.fromCallable {
            sketches.forEach { sketch ->
                sketch?.let {
                    fileStore.deleteType(sketch.name)
                }

            }
            val oldSketches = currentSketches?.toCollection(ArrayList())
            val removedSketches = sketches.toCollection(ArrayList())
            oldSketches?.removeAll(removedSketches)

            currentSketches = oldSketches?.toTypedArray()

            sketchDao.deleteSketches(sketches)
        }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    currentSketches?.let {
                        view.displaySketches(currentSketches!!.toCollection(ArrayList()))
                    }
                }
    }
}