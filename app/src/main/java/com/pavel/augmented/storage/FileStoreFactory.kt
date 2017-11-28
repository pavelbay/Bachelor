package com.pavel.augmented.storage

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson

class FileStoreFactory(private val context: Context, private val gson: Gson) {

     fun<T> create(clazz: Class<T>): FileStore<Bitmap> {
         return when (clazz) {
             Bitmap::class.java  -> BitmapFileStore(context)
             else -> BitmapFileStore(context)
         }
     }
}