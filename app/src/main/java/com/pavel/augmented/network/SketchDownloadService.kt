package com.pavel.augmented.network

import com.pavel.augmented.model.Sketch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface SketchDownloadService {
    @GET("/sketches")
    fun downloadSketches(): Call<List<Sketch>>

    @GET("/sketchimage")
    fun downloadImage(@Query("id") id: String, @Query("origin") orig: Boolean): Call<ResponseBody>
}