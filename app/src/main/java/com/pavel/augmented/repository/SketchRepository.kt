package com.pavel.augmented.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.events.SketchEvents
import com.pavel.augmented.events.SketchUploadEvents
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.network.SketchDownloadService
import com.pavel.augmented.network.SketchUploadService
import com.pavel.augmented.presentation.canvas.CanvasPresenter.Companion.TMP_BITMAP
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStore
import com.pavel.augmented.util.getOriginImageFile
import com.pavel.augmented.util.getTmpImageFile
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

class SketchRepository(private val context: Context,
                       private val schedulerProvider: SchedulerProvider,
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

    private fun performSave(sketch: Sketch, bitmap: Bitmap?, insertToDb: Boolean) {
        if (insertToDb) {
            Observable
                    .fromCallable {
                        // Hier wird bestimmt, was gemacht werden soll
                        sketchDao.insertSketch(sketch)
                    }
                    .subscribeOn(schedulerProvider.io()) // Hier gibt man an,
                    // auf welchem Thread das gemacht werden soll. io - InputOutput
                    .observeOn(schedulerProvider.ui()) // Auf welchem Thread subscriber aufgerufen wird
                    .subscribe {
                        // Wenn die Operation abgeschlossen ist, wird Subscriber aufgerufen
                        Log.d(TAG, "Sketch: $sketch saved to db")
                    }
        }

//        fun delete() = fileStore.deleteType(existedSketch?.name)

        bitmap?.let {
            //currentRequest?.dispose()
            Observable
                    .fromCallable { fileStore.saveType(bitmap, sketch.id) }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { EventBus.getDefault().post(SketchEvents.OnSketchSaved()) }
        }
    }

    fun saveTempBitmap(bitmap: Bitmap?, width: Int, height: Int) {
        bitmap?.let {
            Observable.fromCallable {
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                fileStore.saveType(scaledBitmap, TMP_BITMAP)
            }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe { }
        }
    }

    fun saveToGallery(name: String?, bitmap: Bitmap?, existedSketch: Sketch?, location: Location?, callback: (sketch: Sketch?) -> Unit) {
        if (existedSketch != null) {
            performSave(existedSketch, bitmap, false)
        } else if (name != null && location != null) {
            callback(createNewSketch(name, bitmap, LatLng(location.latitude, location.longitude)))
        }
    }

    private fun createNewSketch(name: String, bitmap: Bitmap?, latLng: LatLng?, defaultId: String = "0"): Sketch? {
        return if (latLng != null) {
            val newId = if (defaultId != "0") {
                defaultId
            } else {
                generateUniqueId()?.toString()
            }
            val sketch = Sketch(id = newId ?: defaultId, name = name, latitude = latLng.latitude, longitude = latLng.longitude)
                sketchDao.findSketchByIdAsync(sketch.id)
                        .subscribe { loadedSketch: Sketch?, _: Throwable? ->
                            val insertInDb = loadedSketch == null
                            Log.d(TAG, "InsertInDB:$insertInDb")
                            performSave(sketch, bitmap, insertInDb)
                            if (insertInDb) {
                                renameTempBitmap(sketch.id)
                            }
                        }
            sketch
        } else {
            null
        }
    }

    private fun renameTempBitmap(id: String) {
        val from = getTmpImageFile(context)
        val to = getOriginImageFile(context, id)
        from.renameTo(to)
    }

    private fun generateUniqueId(): Long? {
        var `val`: Long
        do {
            val uid = UUID.randomUUID()
            val buffer = ByteBuffer.wrap(ByteArray(16))
            buffer.putLong(uid.leastSignificantBits)
            buffer.putLong(uid.mostSignificantBits)
            val bi = BigInteger(buffer.array())
            `val` = bi.toLong()
        } while (`val` < 0)
        return `val`
    }

    fun fetchImage(id: String, title: String, latLng: LatLng?) {
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
                                        if (latLng == null) {
                                            fileStore.saveBitmapOrigin(it)
                                        } else {
                                            fileStore.saveType(it, "origin$id")
                                        }
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
                                        // TODO: create sketch
                                        if (latLng != null) {
                                            createNewSketch(title, it, latLng, id)
                                        } else {
                                            fileStore.saveBitmapTarget(it)
                                        }
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
        val id = RequestBody.create(MediaType.parse("text/plain"), sketch.id)
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