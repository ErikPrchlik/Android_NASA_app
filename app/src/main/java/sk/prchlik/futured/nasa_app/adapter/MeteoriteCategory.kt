package sk.prchlik.futured.nasa_app.adapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import sk.prchlik.futured.nasa_app.model.Meteorite

@Parcelize
data class MeteoriteCategory(
    val category: String,
    val meteoritesToShow: MutableList<Meteorite> = mutableListOf(),
    val meteoritesToFetch: MutableList<Meteorite>,
    val hasMore: Boolean, // Indicates if more data can be loaded for this category
    var isLoading: Boolean = false, // Indicates if the category is currently loading more data
    var isExpanded: Boolean = false // Indicates if the category items are shown
) : Parcelable