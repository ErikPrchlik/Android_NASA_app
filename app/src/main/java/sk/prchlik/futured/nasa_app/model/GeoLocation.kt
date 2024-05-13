package sk.prchlik.futured.nasa_app.model

import com.google.gson.annotations.SerializedName

data class GeoLocation(
    @SerializedName("type") val type: String?,
    @SerializedName("coordinates") val coordinates: MutableList<Double>?
)
