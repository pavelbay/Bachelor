package com.pavel.augmented.database.dao

import android.arch.persistence.room.*
import com.pavel.augmented.model.Sketch
import io.reactivex.Single

@Dao
interface SketchDao {

    @Insert
    fun insertSketch(sketch: Sketch)

    @Update
    fun updateSketch(sketch: Sketch)

    @Delete
    fun deleteSketch(sketch: Sketch)

    @Delete
    fun deleteSketches(sketches: Array<Sketch?>)

    @Query("SELECT * FROM sketches")
    fun loadAllSketches(): Array<Sketch>

    @Query("SELECT * FROM sketches WHERE id = :search")
    fun findSketchById(search: String): Sketch

    @Query("SELECT * FROM sketches WHERE id = :search")
    fun findSketchByIdAsync(search: String): Single<Sketch>
}