package com.example.pavel.myapplication.storage

import android.content.Context
import com.google.gson.Gson

class FileStoreImpl<T>(val context: Context, val gson: Gson) : FileStore<T> {

    override fun readType(): T? {

        return null
    }

    override fun saveType(value: T) {
    }
}