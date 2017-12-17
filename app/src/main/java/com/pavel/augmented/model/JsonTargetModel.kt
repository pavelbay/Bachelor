package com.pavel.augmented.model

import com.google.gson.annotations.SerializedName

data class JsonTargetModel(@SerializedName("image") val image: String, @SerializedName("name") val name: String)