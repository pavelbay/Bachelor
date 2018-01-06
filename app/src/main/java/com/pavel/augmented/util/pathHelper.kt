package com.pavel.augmented.util

import android.content.Context
import com.pavel.augmented.presentation.canvas.CanvasPresenter
import com.pavel.augmented.storage.BitmapFileStore
import java.io.File

fun getImagesFolder(context: Context) = File(context.getExternalFilesDir(null), BitmapFileStore.DIR_NAME)

fun getTargetImageFile(context: Context, id: String) = File(getImagesFolder(context), id + ".jpeg")

fun getTmpImageFile(context: Context) = File(getImagesFolder(context), CanvasPresenter.TMP_BITMAP + ".jpeg")

fun getOriginImageFile(context: Context, id: String) = File(getImagesFolder(context), "origin$id.jpeg")

fun getModifiedFolder(context: Context) = File(context.getExternalFilesDir(null), BitmapFileStore.TARGET_DIR_NAME)

fun getModified(context: Context) = File(getModifiedFolder(context), "target.jpeg")

fun getOrigin(context: Context) = File(getModifiedFolder(context), "origin.jpeg")

fun getCurrentBitmapFolder(context: Context) = File(context.getExternalFilesDir(null), BitmapFileStore.CURRENT_BITMAP_DIR_NAME)

fun getCurrentBitmap(context: Context) = File(getCurrentBitmapFolder(context), "current.jpeg")