package com.pavel.augmented.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel

@PaperParcel
@Entity(tableName = "sketches")
data class Sketch(
        @SerializedName("id") @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @SerializedName("name") @ColumnInfo(name = "name") val name: String,
        @SerializedName("latitude") @ColumnInfo(name = "latitude") val latitude: Double,
        @SerializedName("longitude") @ColumnInfo(name = "longitude") val longitude: Double
) : Parcelable {

    companion object {
        @JvmField
        @Ignore
        val CREATOR = PaperParcelSketch.CREATOR
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let {
            PaperParcelSketch.writeToParcel(this, dest, flags)
        }
    }
}