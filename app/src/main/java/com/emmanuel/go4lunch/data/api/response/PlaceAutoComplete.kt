package com.emmanuel.go4lunch.data.api.response

import com.google.gson.annotations.SerializedName

data class PlaceAutoComplete(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val place_id: String,
    val types: List<String>,
    val rating: Double?,
    @SerializedName("structured_formatting")
    val structuredFormatting: StructuredFormatting
)
data class StructuredFormatting(
    @SerializedName("main_text")
    val mainText: String,
)