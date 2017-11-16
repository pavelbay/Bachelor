package com.pavel.augmented.di

import android.graphics.Bitmap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pavel.augmented.presentation.MainActivity
import com.pavel.augmented.presentation.canvas.CanvasContract
import com.pavel.augmented.presentation.canvas.CanvasPresenter
import com.pavel.augmented.presentation.pageradapter.MainPagerAdapter
import com.pavel.augmented.rx.ApplicationSchedulerProvider
import com.pavel.augmented.rx.SchedulerProvider
import com.pavel.augmented.storage.FileStoreFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.module.AndroidModule
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


fun appModules() = listOf(AppModule(), RemoteDataSourceModule(), RxModule(), GsonModule(), FileStoreFactoryModule())

class AppModule : AndroidModule() {
    override fun context() = applicationContext {
        context(name = CTX_CANVAS_ACTIVITY) {
            provide { MainPagerAdapter(getProperty(MainActivity.FRAGMENT_MANAGER_KEY), getProperty(MainActivity.FRAGMENT_NAMES_KEY)) }

            context(name = CTX_CANVAS_FRAGMENT) {
                provide { CanvasPresenter(get(BITMAP_FILESTORE)) } bind (CanvasContract.Presenter::class)
                provide(BITMAP_FILESTORE) { createFileStoreForBitmap(get()) }
            }

            context(name = CTX_MAP_FRAGMENT) {

            }

            context(name = CTX_GALERIE_FRAGMENT) {

            }

        }
    }

    companion object {
        const val CTX_CANVAS_ACTIVITY = "CanvasActivity"
        const val CTX_CANVAS_FRAGMENT = "CanvasFragment"
        const val CTX_GALERIE_FRAGMENT = "GalerieFragment"
        const val CTX_MAP_FRAGMENT = "MapFragment"
        const val BITMAP_FILESTORE = "BitmpalFileStore"
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
//        provide { createWebService<WeatherDatasource>(get(), getProperty(AppModule.SERVER_URL)) }
    }
}

fun createFileStoreForBitmap(fileStoreFactory: FileStoreFactory) = fileStoreFactory.create(Bitmap::class.java)

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