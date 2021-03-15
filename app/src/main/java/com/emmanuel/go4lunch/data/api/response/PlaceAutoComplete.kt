package com.emmanuel.go4lunch.data.api.response

data class PlaceAutoComplete(
    val predictions: List<Prediction>,
    val status: String
)

data class Prediction(
    val description: String,
    val place_id: String,
    val types: List<String>,
    val structured_formatting: StructuredFormatting
)
data class StructuredFormatting(
    val main_text: String,
)