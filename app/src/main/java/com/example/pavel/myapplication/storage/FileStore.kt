package com.example.pavel.myapplication.storage

interface FileStore<T> {

    fun readType(): T?

    fun saveType(value: T)
}