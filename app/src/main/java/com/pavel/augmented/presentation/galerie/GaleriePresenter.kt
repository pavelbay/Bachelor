package com.pavel.augmented.presentation.galerie

import com.pavel.augmented.model.Sketch
import com.pavel.augmented.repository.SketchRepository
import com.pavel.augmented.util.getOriginImageFile
import com.pavel.augmented.util.getTargetImageFile
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
        val targetImageFile = getTargetImageFile(view.context(), sketch.name)
        val originImageFIle = getOriginImageFile(view.context(), sketch.name)
        val stringMediaType = "image/*"
        val requestBodyTarget = RequestBody.create(MediaType.parse(stringMediaType), targetImageFile)
        val requestBodyOrigin = RequestBody.create(MediaType.parse(stringMediaType), originImageFIle)
        val bodyArray = arrayOfNulls<MultipartBody.Part>(2)
        bodyArray[0]  = MultipartBody.Part.createFormData("picture", sketch.id.toString(), requestBodyTarget)
        bodyArray[1] = MultipartBody.Part.createFormData("picture", "origin${sketch.id}", requestBodyOrigin)
        sketchRepository.publicSketch(sketch, bodyArray)
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