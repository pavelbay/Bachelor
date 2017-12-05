package com.pavel.augmented.storage

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream


class BitmapFileStore(val context: Context) : FileStore<Bitmap> {

    override fun getDir(): File {
        val dir = File(context.getExternalFilesDir(null), DIR_NAME)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.w(TAG, "Could not create directory " + DIR_NAME)
            }
        }

        return dir
    }

    override fun readType(): Bitmap? = null

    override fun saveType(value: Bitmap, name: String) {
        val file = File(getDir(), name + ".png")
        if (file.exists() || file.createNewFile()) {
            performSave(file, value)
        }
    }

    override fun deleteType(name: String) {
        val file = File(getDir(), name + ".png")
        if (file.exists()) {
            file.delete()
        }
    }

    override fun performSave(file: File, value: Bitmap) {
        val fos = FileOutputStream(file)
        value.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
    }

    companion object {
        private const val DIR_NAME = "images"
        private val TAG = BitmapFileStore::class.java.simpleName
    }
}