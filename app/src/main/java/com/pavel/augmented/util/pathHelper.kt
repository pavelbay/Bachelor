package com.pavel.augmented.util

import android.content.Context
import com.pavel.augmented.presentation.canvas.CanvasPresenter
import com.pavel.augmented.storage.BitmapFileStore
import java.io.File

fun getImagesFolder(context: Context) = File(context.getExternalFilesDir(null), BitmapFileStore.DIR_NAME)

fun getTargetImageFile(context: Context, id: String) = File(getImagesFolder(context), id + ".jpeg")

fun getTmpImageFile(context: Context) = File(getImagesFolder(context), CanvasPresenter.TMP_BITMAP + ".jpeg")

fun getOriginImageFile(context: Context, id: String) = File(getImagesFolder(context), "origin$id.jpeg")

fun getTargetFolder(context: Context) = File(context.getExternalFilesDir(null), BitmapFileStore.TARGET_DIR_NAME)