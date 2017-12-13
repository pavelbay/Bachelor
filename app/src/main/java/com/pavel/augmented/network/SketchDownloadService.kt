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

    @Streaming
    @GET("/sketchimage")
    fun downloadImage(@Query("name") name: String): Call<ResponseBody>
}