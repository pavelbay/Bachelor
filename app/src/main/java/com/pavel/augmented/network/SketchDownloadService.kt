package com.pavel.augmented.network

import retrofit2.http.GET

interface SketchDownloadService {
    @GET("/sketches")
    fun downloadSketches()
}