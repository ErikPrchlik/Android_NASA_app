package sk.prchlik.futured.nasa_app.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem

@Entity(tableName = "meteorites")
data class Meteorite(
    @PrimaryKey @SerializedName("id") val id: Long,
    @ColumnInfo(name = "name") @SerializedName("name") val name: String?,
    @ColumnInfo(name = "name_type") @SerializedName("nametype") val nameType: String?,
    @ColumnInfo(name = "rec_class") @SerializedName("recclass") val recClass: String?,
    @ColumnInfo(name = "mass") @SerializedName("mass") val mass: String?,
    @ColumnInfo(name = "fall") @SerializedName("fall") val fall: String?,
    @ColumnInfo(name = "year") @SerializedName("year") val year: String?,
    @ColumnInfo(name = "rec_lat") @SerializedName("reclat") val recLat: String?,
    @ColumnInfo(name = "rec_long") @SerializedName("reclong") val recLong: String?,
    @ColumnInfo(name = "geo_location") @SerializedName("geolocation") val geoLocation: GeoLocation?,
    @ColumnInfo(name = "geolocation_address") @SerializedName("geolocation_address") val geoLocationAddress: String?,
    @ColumnInfo(name = "geolocation_city") @SerializedName("geolocation_city") val geoLocationCity: String?,
    @ColumnInfo(name = "geolocation_state") @SerializedName("geolocation_state") val geoLocationState: String?,
    @ColumnInfo(name = "geolocation_country") @SerializedName("geolocation_country") val geoLocationCountry: String?,
    @ColumnInfo(name = "geolocation_zip") @SerializedName("geolocation_zip") val geoLocationZip: String?
): ClusterItem {

    @ColumnInfo(name = "lat_long")
        var latLng: LatLng? = null
            get() {
                return geoLocation?.coordinates?.get(1)?.let { lat ->
                    LatLng(lat, geoLocation.coordinates[0])
                }
            }

    override fun getPosition(): LatLng {
        return latLng!!
    }

    override fun getTitle(): String? {
        return name
    }

    override fun getSnippet(): String? {
        return year
    }
}
