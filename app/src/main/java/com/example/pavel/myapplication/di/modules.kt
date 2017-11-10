package com.example.pavel.myapplication.di

import com.example.pavel.myapplication.activities.canvas.MainActivityContract
import com.example.pavel.myapplication.activities.canvas.MainActivityPresenter
import com.example.pavel.myapplication.rx.ApplicationSchedulerProvider
import com.example.pavel.myapplication.rx.SchedulerProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


fun appModules() = listOf(CanvasModule(), RemoteDataSourceModule(), RxModule())

class CanvasModule : AndroidModule() {
    override fun context() = applicationContext {
        context(name = CTX_CANVAS_ACTIVITY) {
            provide { MainActivityPresenter() } bind (MainActivityContract.Presenter::class)
        }
    }

    companion object {
        const val CTX_CANVAS_ACTIVITY = "CanvasActivity"
        const val SERVER_URL = "lbs.f4.htw-berlin.de"
    }
}

class FileStoreModule : AndroidModule() {
    override fun context() = applicationContext {
        provide { }
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