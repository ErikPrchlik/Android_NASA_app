package sk.prchlik.futured.nasa_app.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
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
        circle: CircleContent,
        drawable: Drawable?
    ) {

        when (circle) {
            is CircleContent.Cluster -> {
                binding.mapMarkerViewPin.setImageDrawable(context.getDrawable(R.drawable.blue_circle))
                binding.mapMarkerViewClusterText.isVisible = true
                binding.mapMarkerViewClusterText.text = circle.count.toString()
            }
            is CircleContent.Marker -> {
                binding.mapMarkerViewPin.setImageDrawable(drawable)
                binding.mapMarkerViewPin.background = null
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