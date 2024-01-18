package com.queentylion.sibitranslator.data.gesture

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("predictions") val predictions: List<String>,
    @SerializedName("deployedModelId") val deployedModelID: String,
    @SerializedName("model") val model: String,
    @SerializedName("modelDisplayName") val modelDisplayName: String,
    @SerializedName("modelVersionId") val modelVersionId: String
)