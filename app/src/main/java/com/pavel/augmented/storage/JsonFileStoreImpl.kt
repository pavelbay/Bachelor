package com.pavel.augmented.storage

import java.io.File

class JsonFileStoreImpl<T> : FileStore<T> {

    override fun readType(): T? {
        return null
    }

    override fun saveType(value: T) {
    }

    override fun getDir(): File {

        return File("TODO")
    }

    override fun performSave(file: File, value: T) {
    }

    override fun getFilename(): String {
        return ""
    }
}