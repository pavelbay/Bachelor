package com.pavel.augmented.repository

import android.graphics.Bitmap
import android.util.Log
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.network.SketchDownloadService
import com.pavel.augmented.network.SketchUploadService
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SketchRepository(private val schedulerProvider: SchedulerProvider,
                       private val sketchDao: SketchDao,
                       private val fileStore: FileStore<Bitmap>,
                       private val sketchUploadService: SketchUploadService,
                       private val sketchDownloadService: SketchDownloadService) {

    fun loadSketches(callback: (sketches: Array<Sketch>) -> Unit) {
        Observable.fromCallable { sketchDao.loadAllSketches() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe { sketches ->
                    callback(sketches)
                }
    }

    fun fetchImage(name: String) {
        val call = sketchDownloadService.downloadImage(name)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                Log.d(TAG, "Image fetched: " + response?.body()?.bytes()?.size)
            }

            override  fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d(TAG, "Failure fetching image")
            }
        })
    }

    fun fetchSketches(callback: (sketches: List<Sketch>?) -> Unit) {
        val call = sketchDownloadService.downloadSketches()
        call.enqueue(object : Callback<List<Sketch>> {
            override fun onResponse(call: Call<List<Sketch>>?, response: Response<List<Sketch>>?) {
                Log.d(TAG, "Sketches downloaded")
                callback(response?.body())
            }

            override fun onFailure(call: Call<List<Sketch>>?, t: Throwable?) {
                Log.e(TAG, "Error loading sketches", t)
            }
        })
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

    fun publicSketch(sketch: Sketch, file: MultipartBody.Part) {
        val name = RequestBody.create(MediaType.parse("text/plain"), sketch.name)
        val call = sketchUploadService.uploadImage(name, file)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                Log.d(TAG, "Response from image upload: " + response?.toString())
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e(TAG, "Error from image upload: " + t?.toString())
            }
        })

        val sketchCall = sketchUploadService.uploadSketch(sketch)

        sketchCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                Log.d(TAG, "Response from sketch upload: " + response?.toString())
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e(TAG, "Error from sketch upload: " + t?.toString())
            }
        })
    }

    companion object {
        private val TAG = SketchRepository::class.java.simpleName
    }
}