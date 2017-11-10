package com.example.pavel.myapplication.storage

class JsonFileStoreImpl<T> : FileStore<T> {

    override fun readType(): T? {
        return null
    }

    override fun saveType(value: T) {
    }
}