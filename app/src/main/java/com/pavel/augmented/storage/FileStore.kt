package com.pavel.augmented.storage

import java.io.File

interface FileStore<T> {

    fun readType(): T?

    fun saveType(value: T, name: String)

    fun getDir(): File

    fun deleteType(id: String?)

    fun performSave(file: File, value: T)

    fun getTargetDir(): File

    fun saveBitmapTarget(value: T)

    fun saveBitmapOrigin(value: T)

    fun deleteTarget()
}