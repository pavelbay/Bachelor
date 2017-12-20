package com.pavel.augmented.util

import android.content.Context
import com.pavel.augmented.presentation.canvas.CanvasPresenter
import java.io.File

fun getImagesFolder(context: Context) = File(context.getExternalFilesDir(null), "images")

fun getTargetImageFile(context: Context, name: String) = File(getImagesFolder(context), name + ".jpeg")

fun getTmpImageFile(context: Context) = File(getImagesFolder(context), CanvasPresenter.TMP_BITMAP + ".jpeg")

fun getOriginImageFile(context: Context, name: String) = File(getImagesFolder(context), "origin$name.jpeg")