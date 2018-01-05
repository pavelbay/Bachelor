package com.pavel.augmented.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.pavel.augmented.database.dao.SketchDao
import com.pavel.augmented.model.Sketch

@Database(entities = [(Sketch::class)], version = 1)
abstract class SketchAppDatabase  : RoomDatabase() {
    abstract fun sketchDao(): SketchDao
}