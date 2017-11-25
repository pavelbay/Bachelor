package com.pavel.augmented.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import paperparcel.PaperParcel

@PaperParcel
data class Sketch(
        @SerializedName("name") val name: String,
        @SerializedName("latitude") val latitude: String,
        @SerializedName("longitude") val longitude: String,
        @SerializedName("bitmapname") val bitmapName: String
) : Parcelable {

    companion object {
        @JvmField
        val CREATOR = PaperParcelSketch.CREATOR
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.let {
            PaperParcelSketch.writeToParcel(this, dest, flags)
        }
    }
}