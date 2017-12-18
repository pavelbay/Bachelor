package com.pavel.augmented.util

import android.content.Context
import java.io.File

fun getImagesFolder(context: Context) = File(context.getExternalFilesDir(null), "images")

fun getImageFile(context: Context, name: String) = File(getImagesFolder(context), name/* + ".jpeg"*/)