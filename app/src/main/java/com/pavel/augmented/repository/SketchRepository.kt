package com.pavel.augmented.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.events.SketchUploadEvents
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
import org.greenrobot.eventbus.EventBus
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

    fun fetchImage(id: String) {
        val originCall = sketchDownloadService.downloadImage(id)
        val targetCall = sketchDownloadService.downloadImageTarget(id)

        val originCallback = object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                response?.let {
                    response.body()?.let {
                        Observable.fromCallable { getBitmapFromByteArray(response.body()!!.bytes()) }
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.io())
                                .subscribe {
                                    synchronized(fileStore) {
                                        fileStore.saveBitmapOrigin(it)
                                    }
                                }
                    }
                }

                Log.d(TAG, "Origin Image fetched")
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d(TAG, "Failure fetching origin image")
            }
        }

        val targetCallback = object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                response?.let {
                    response.body()?.let {
                        Observable.fromCallable { getBitmapFromByteArray(response.body()!!.bytes()) }
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.io())
                                .subscribe {
                                    synchronized(fileStore) {
                                        fileStore.saveBitmapTarget(it)
                                    }
                                }
                    }
                }

                Log.d(TAG, "Target Image fetched")
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d(TAG, "Failure fetching target image")
            }
        }

        originCall.enqueue(originCallback)
        targetCall.enqueue(targetCallback)
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

    private fun getBitmapFromByteArray(bytes: ByteArray) = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

    fun deleteSketches(sketches: Array<Sketch?>, callback: () -> Unit) {
        Observable.fromCallable {
            sketches.forEach { sketch ->
                sketch?.let {
                    fileStore.deleteType(sketch.id)
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

    fun publicSketch(sketch: Sketch, originFile: MultipartBody.Part, targetFile: MultipartBody.Part) {
        val id = RequestBody.create(MediaType.parse("text/plain"), sketch.id.toString())
        val call = sketchUploadService.uploadImages(id, originFile, targetFile)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                Log.d(TAG, "Response from image upload: " + response?.toString())
                EventBus.getDefault().post(SketchUploadEvents.OnSuccess())
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.e(TAG, "Error from image upload: " + t?.toString())
                EventBus.getDefault().post(SketchUploadEvents.OnFailure())
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