package com.example.pavel.myapplication.di

import android.graphics.Bitmap
import com.example.pavel.myapplication.activities.canvas.MainActivityContract
import com.example.pavel.myapplication.activities.canvas.MainActivityPresenter
import com.example.pavel.myapplication.rx.ApplicationSchedulerProvider
import com.example.pavel.myapplication.rx.SchedulerProvider
import com.example.pavel.myapplication.storage.FileStoreFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


const val BITMAP_FILESTORE = "BitmpalFileStore"

fun appModules() = listOf(CanvasModule(), RemoteDataSourceModule(), RxModule(), GsonModule(), FileStoreFactoryModule())

class CanvasModule : AndroidModule() {
    override fun context() = applicationContext {
        context(name = CTX_CANVAS_ACTIVITY) {
            provide { MainActivityPresenter(get(BITMAP_FILESTORE)) } bind (MainActivityContract.Presenter::class)

            provide(BITMAP_FILESTORE) { createFileStoreForBitmap(get())}
        }
    }

    companion object {
        const val CTX_CANVAS_ACTIVITY = "CanvasActivity"
        const val SERVER_URL = "lbs.f4.htw-berlin.de"
    }
}

class GsonModule : AndroidModule() {
    override fun context() = applicationContext {
        provide { createGson() }
    }
}

class FileStoreFactoryModule : AndroidModule() {
    override fun context() = applicationContext {
        provide { FileStoreFactory(get(), get()) }
    }
}

class RemoteDataSourceModule : AndroidModule() {
    override fun context() = applicationContext {
        // provided web components
        provide { createOkHttpClient() }

        // Fill property
        // TODO: implement this later
//        provide { createWebService<WeatherDatasource>(get(), getProperty(CanvasModule.SERVER_URL)) }
    }
}

fun createFileStoreForBitmap(fileStoreFactory: FileStoreFactory) = fileStoreFactory.create<Bitmap>()

fun createGson(): Gson {
    return GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setLenient()
            .create()
}

fun createOkHttpClient(): OkHttpClient {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return OkHttpClient.Builder()
            .connectTimeout(60L, TimeUnit.SECONDS)
            .readTimeout(60L, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor).build()
}

inline fun <reified T> createWebService(okHttpClient: OkHttpClient, url: String): T {
    val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    return retrofit.create(T::class.java)
}

class RxModule : AndroidModule() {
    override fun context() = applicationContext {
        provide { ApplicationSchedulerProvider() } bind (SchedulerProvider::class)
    }
}