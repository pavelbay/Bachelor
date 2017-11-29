package com.pavel.augmented.util

import android.content.Context
import java.io.File

fun getImagesFolder(context: Context) = File(context.getExternalFilesDir(null), "images")