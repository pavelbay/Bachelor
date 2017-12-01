package com.pavel.augmented.network

import com.pavel.augmented.model.Sketch
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SketchUploadService {

    @Multipart
    @POST("/upload")
    fun upload(@Part("object") @Body sketch: Sketch, @Part file: MultipartBody): Call<Sketch>
}