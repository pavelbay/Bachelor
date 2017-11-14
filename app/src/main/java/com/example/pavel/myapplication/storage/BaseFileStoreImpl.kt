package com.example.pavel.myapplication.storage

import android.content.Context
import com.google.gson.Gson
import java.io.File

abstract class BaseFileStoreImpl<T>(val context: Context, val gson: Gson) : FileStore<T> {

    override fun readType(): T? {

        return null
    }

    override fun saveType(value: T) {
        val file = File(getDir(), getFilename())
        if (file.exists() || file.createNewFile()) {
            performSave(file, value)
        }
    }
}