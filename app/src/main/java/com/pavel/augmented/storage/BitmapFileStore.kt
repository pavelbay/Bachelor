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
        val file = File(getDir(), name + ".jpeg")
        if (file.exists() || file.createNewFile()) {
            performSave(file, value)
        }
    }

    override fun deleteType(id: String?) {
        id?.let {
            val targetFile = File(getDir(), id + ".jpeg")
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val originFile = File(getDir(), "origin$id.jpeg")
            if (originFile.exists()) {
                originFile.delete()
            }
        }
    }

    override fun performSave(file: File, value: Bitmap) {
        val fos = FileOutputStream(file)
        value.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
    }

    override fun deleteTarget() {
        val dir = getTargetDir()
        if (dir.isDirectory) {
            dir.list().forEach {
                File(dir, it).delete()
            }
        }
    }

    override fun getTargetDir(): File {
        val dir = File(context.getExternalFilesDir(null), TARGET_DIR_NAME)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.w(TAG, "Could not create directory " + TARGET_DIR_NAME)
            }
        }

        return dir
    }

    override fun saveBitmapTarget(value: Bitmap) {
        val file = File(getTargetDir(), "target.jpeg")
        if (file.exists() || file.createNewFile()) {
            performSave(file, value)
        }
    }

    override fun saveBitmapOrigin(value: Bitmap) {
        val file = File(getTargetDir(), "origin.jpeg")
        if (file.exists() || file.createNewFile()) {
            performSave(file, value)
        }
    }

    companion object {
        const val DIR_NAME = "images"
        const val TARGET_DIR_NAME = "target"
        const val CURRENT_BITMAP_DIR_NAME = "currentBitmap"
        private val TAG = BitmapFileStore::class.java.simpleName
    }
}