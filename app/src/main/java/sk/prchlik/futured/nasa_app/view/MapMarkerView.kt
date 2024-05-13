package sk.prchlik.futured.nasa_app.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import sk.prchlik.futured.nasa_app.R
import sk.prchlik.futured.nasa_app.databinding.ViewMapMarkerBinding

class MapMarkerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by lazy { ViewMapMarkerBinding.inflate(LayoutInflater.from(context)) }

    init {
        addView(binding.root)
    }

    fun setContent(
        circle: CircleContent
    ) {

        when (circle) {
            is CircleContent.Cluster -> {
                binding.mapMarkerViewClusterText.isVisible = true
                binding.mapMarkerViewClusterText.text = circle.count.toString()
            }
            is CircleContent.Marker -> {
                binding.mapMarkerViewClusterText.isVisible = false
            }
        }
    }

    sealed interface CircleContent {

        data class Cluster(
            val count: Int,
        ) : CircleContent

        data object Marker : CircleContent
    }
}