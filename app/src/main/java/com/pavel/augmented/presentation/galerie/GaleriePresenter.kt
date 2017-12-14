package com.pavel.augmented.presentation.galerie

import android.net.Uri
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.repository.SketchRepository
import com.pavel.augmented.util.getImageFile
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class GaleriePresenter(private val sketchRepository: SketchRepository) : GalerieContract.Presenter {

    override lateinit var view: GalerieContract.View

    private var currentSketches: Array<Sketch>? = null

    override fun start() {
    }

    override fun stop() {
    }

    override fun loadSketches() {
        sketchRepository.loadSketches { sketches ->
            currentSketches = sketches
            view.displaySketches(sketches.toCollection(ArrayList()))
        }
    }

    override fun publicSketches(sketches: Array<Sketch?>) {
        sketches.forEach { sketch ->
            sketch?.let {
                publicSketch(sketch)
            }
        }
    }

    private fun publicSketch(sketch: Sketch) {
        val imageFile = getImageFile(view.context(), sketch.name)
        val requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile)
        val body = MultipartBody.Part.createFormData("picture", sketch.id.toString(), requestBody)
        sketchRepository.publicSketch(sketch, body)
    }

    override fun deleteSketches(sketches: Array<Sketch?>) {
        val oldSketches = currentSketches?.toCollection(ArrayList())
        val removedSketches = sketches.toCollection(ArrayList())
        oldSketches?.removeAll(removedSketches)
        currentSketches = oldSketches?.toTypedArray()
        sketchRepository.deleteSketches(sketches, {
            currentSketches?.let {
                view.displaySketches(currentSketches!!.toCollection(ArrayList()))
            }
        })
    }
}