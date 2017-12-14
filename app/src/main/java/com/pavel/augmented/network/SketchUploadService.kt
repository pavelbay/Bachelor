package com.pavel.augmented.network

import com.pavel.augmented.model.Sketch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SketchUploadService {

    @Multipart
    @POST("/uploadImage")
    fun uploadImage(@Part("id") id: RequestBody, @Part file: MultipartBody.Part): Call<ResponseBody>

    @POST("/uploadSketch")
    fun uploadSketch(@Body sketch: Sketch): Call<ResponseBody>
}