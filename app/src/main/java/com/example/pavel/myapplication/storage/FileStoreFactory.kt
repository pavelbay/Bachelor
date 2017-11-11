package com.example.pavel.myapplication.storage

import android.content.Context
import com.google.gson.Gson

class FileStoreFactory(private val context: Context, private val gson: Gson) {

     fun<T> create() = FileStoreImpl<T>(context, gson)
}