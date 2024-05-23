package sk.prchlik.futured.nasa_app.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoLocation(
    @SerializedName("type") val type: String?,
    @SerializedName("coordinates") val coordinates: MutableList<Double>?
) : Parcelable
