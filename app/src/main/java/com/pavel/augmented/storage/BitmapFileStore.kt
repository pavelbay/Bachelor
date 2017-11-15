package com.pavel.augmented.storage

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream


private val TAG = BitmapFileStore::class.java.simpleName

class BitmapFileStore(context: Context, gson: Gson) : BaseFileStoreImpl<Bitmap>(context, gson) {

    override fun getFilename() = "Image-" + System.currentTimeMillis() + ".png"

    override fun getDir(): File {
        val dir = File(context.getExternalFilesDir(null), DIR_NAME)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.w(TAG, "Could not create directory " + DIR_NAME)
            }
        }

        return dir
    }

    override fun performSave(file: File, value: Bitmap) {
        val fos = FileOutputStream(file)
        value.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
    }

    companion object {
        const val DIR_NAME = "images"
    }
}