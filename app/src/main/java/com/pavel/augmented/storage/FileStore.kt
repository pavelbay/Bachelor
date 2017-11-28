package com.pavel.augmented.storage

import java.io.File

interface FileStore<T> {

    fun readType(): T?

    fun saveType(value: T, name: String)

    fun getDir(): File

    fun performSave(file: File, value: T)
}