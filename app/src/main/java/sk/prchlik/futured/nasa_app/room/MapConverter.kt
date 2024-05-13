package sk.prchlik.futured.nasa_app.room

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sk.prchlik.futured.nasa_app.model.GeoLocation

class MapConverter {

    @TypeConverter
    fun fromLatLng(position: LatLng?): String? {
        if (position == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<LatLng>() {}.type
        return gson.toJson(position, type)
    }

    @TypeConverter
    fun toLatLng(position: String?): LatLng? {
        if (position == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<LatLng>() {}.type
        return gson.fromJson<LatLng>(position, type)
    }

    @TypeConverter
    fun fromGeoLocation(geoLocation: GeoLocation?): String? {
        if (geoLocation == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<GeoLocation>() {}.type
        return gson.toJson(geoLocation, type)
    }

    @TypeConverter
    fun toGeoLocation(geoLocation: String?): GeoLocation? {
        if (geoLocation == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<GeoLocation>() {}.type
        return gson.fromJson<GeoLocation>(geoLocation, type)
    }

    @TypeConverter
    fun fromListOfDoubles(doubles: ArrayList<Double>?): String? {
        if (doubles == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Double>>() {}.type
        return gson.toJson(doubles, type)
    }

    @TypeConverter
    fun toListOfDoubles(doubles: String?): ArrayList<Double>? {
        if (doubles == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<Double>>() {}.type
        return gson.fromJson<ArrayList<Double>>(doubles, type)
    }

}