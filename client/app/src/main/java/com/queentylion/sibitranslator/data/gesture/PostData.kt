package com.queentylion.sibitranslator.data.gesture
import com.google.gson.annotations.SerializedName

data class PostData(
    @SerializedName("instances") val instances: List<List<Int>>
)