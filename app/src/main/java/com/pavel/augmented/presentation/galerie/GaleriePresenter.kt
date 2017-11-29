package com.pavel.augmented.presentation.galerie

import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.rx.SchedulerProvider
import io.reactivex.Observable

class GaleriePresenter(private val schedulerProvider: SchedulerProvider,
                       private val sketchDao: SketchDao) : GalerieContract.Presenter {

    override lateinit var view: GalerieContract.View

    override fun start() {
    }

    override fun stop() {
    }

    override fun loadSketches() {
        Observable.fromCallable { sketchDao.loadAllSketches() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe { sketches -> view.displaySketches(sketches.toCollection(ArrayList())) }
    }
}