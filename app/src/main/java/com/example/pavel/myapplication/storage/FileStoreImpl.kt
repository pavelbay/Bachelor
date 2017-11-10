package com.example.pavel.myapplication.storage

class FileStoreImpl<T> : FileStore<T> {

    override fun readType(): T? {

        return null
    }

    override fun saveType(value: T) {
    }
}