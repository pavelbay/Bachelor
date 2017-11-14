package com.example.pavel.myapplication.storage

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

class BitmapFileStore(context: Context, gson: Gson) : BaseFileStoreImpl<Bitmap>(context, gson) {

    override fun getFilename(): String {

        return ""
    }

    override fun getDir(): File {

        return File("TODO")
    }

    override fun performSave(file: File, value: Bitmap) {
        val fos = FileOutputStream(file)
        value.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
    }
}