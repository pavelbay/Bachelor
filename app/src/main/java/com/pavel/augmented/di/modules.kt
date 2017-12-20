package com.pavel.augmented.di

import android.arch.persistence.room.Room
import android.graphics.Bitmap
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pavel.augmented.database.SketchAppDatabase
import com.pavel.augmented.di.AppModule.Companion.SKETCH_DOWNLOAD_SERVICE
import com.pavel.augmented.di.AppModule.Companion.SKETCH_UPLOAD_SERVICE
import com.pavel.augmented.network.SketchDownloadService
import com.pavel.augmented.network.SketchUploadService
import com.pavel.augmented.presentation.MainActivity
import com.pavel.augmented.presentation.canvas.CanvasContract
import com.pavel.augmented.presentation.canvas.CanvasPresenter
import com.pavel.augmented.presentation.galerie.GalerieContract
import com.pavel.augmented.presentation.galerie.GaleriePresenter
import com.pavel.augmented.presentation.map.MyMapContract
import com.pavel.augmented.presentation.map.MyMapPresenter
import com.pavel.augmented.repository.SketchRepository
import com.pavel.augmented.repository.SketchTypeAdapterFactory
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


fun appModules() = listOf(AppModule(), RemoteDataSourceModule(), RxModule(), GsonModule(), FileStoreFactoryModule(), DatabaseModule())

class AppModule : AndroidModule() {
    override fun context() = applicationContext {
        context(name = CTX_MAIN_ACTIVITY) {

            provide { LocationServices.getFusedLocationProviderClient(getProperty(MainActivity.MAIN_ACTIVITY_CONTEXT)) }

            provide { SketchRepository(get(), get(), get(BITMAP_FILESTORE), get(SKETCH_UPLOAD_SERVICE), get(SKETCH_DOWNLOAD_SERVICE)) }

            provide(BITMAP_FILESTORE) { createFileStoreForBitmap(get()) }

            context(name = CTX_CANVAS_FRAGMENT) {
                provide { CanvasPresenter(get(BITMAP_FILESTORE), get(), get(), get()) } bind (CanvasContract.Presenter::class)
            }

            context(name = CTX_MAP_FRAGMENT) {
                provide { MyMapPresenter(get()) } bind (MyMapContract.Presenter::class)
            }

            context(name = CTX_GALERIE_FRAGMENT) {
                provide { GaleriePresenter(get()) } bind (GalerieContract.Presenter::class)
            }
        }
    }

    companion object {
        const val CTX_MAIN_ACTIVITY = "MainActivity"
        const val CTX_CANVAS_FRAGMENT = "CanvasFragment"
        const val CTX_GALERIE_FRAGMENT = "GalerieFragment"
        const val CTX_AR_FRAGMENT = "ARFragment"
        const val CTX_MAP_FRAGMENT = "MyMapFragment"
        const val BITMAP_FILESTORE = "BitmpalFileStore"
        const val SKETCH_UPLOAD_SERVICE = "SketchUploadService"
        const val SKETCH_DOWNLOAD_SERVICE = "SketchDownloaddService"
        const val SERVER_URL = "http://lbs.f4.htw-berlin.de"
    }
}

class DatabaseModule : AndroidModule() {
    override fun context() = applicationContext {
        provide { createDatabase(get()) }

        provide { createDatabase(get()).sketchDao() }
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
        provide(SKETCH_UPLOAD_SERVICE) { createWebService<SketchUploadService>(get(), AppModule.SERVER_URL) }

        provide(SKETCH_DOWNLOAD_SERVICE) { createWebService<SketchDownloadService>(get(), AppModule.SERVER_URL) }
    }
}

fun createDatabase(appContext: android.content.Context): SketchAppDatabase =
        Room.databaseBuilder(appContext, SketchAppDatabase::class.java, "sketchDatabase").build()

fun createFileStoreForBitmap(fileStoreFactory: FileStoreFactory) = fileStoreFactory.create(Bitmap::class.java)

fun createGson(): Gson {
    return GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setLenient()
            .registerTypeAdapterFactory(SketchTypeAdapterFactory())
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
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
    return retrofit.create(T::class.java)
}

class RxModule : AndroidModule() {
    override fun context() = applicationContext {
        provide { ApplicationSchedulerProvider() } bind (SchedulerProvider::class)
    }
}