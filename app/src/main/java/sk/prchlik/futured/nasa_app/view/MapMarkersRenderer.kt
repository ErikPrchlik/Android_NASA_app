package sk.prchlik.futured.nasa_app.view

import android.content.Context
import android.graphics.Bitmap
import android.util.TypedValue
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import sk.prchlik.futured.nasa_app.R
import sk.prchlik.futured.nasa_app.model.Meteorite

class MapMarkersRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Meteorite>,
) : DefaultClusterRenderer<Meteorite>(context, map, clusterManager) {

    private val mapMarkerView: MapMarkerView = MapMarkerView(context)
    private val markerIconGenerator = IconGenerator(context)

    init {
        markerIconGenerator.setBackground(null)
        markerIconGenerator.setContentView(mapMarkerView)
    }

    override fun onBeforeClusterItemRendered(clusterItem: Meteorite, markerOptions: MarkerOptions) {
        val data = getItemIcon(clusterItem)
        markerOptions
            .icon(data.bitmapDescriptor)
            .anchor(data.anchorU, data.anchorV)
    }

    override fun onClusterItemUpdated(clusterItem: Meteorite, marker: Marker) {
        val data = getItemIcon(clusterItem)
        marker.setIcon(data.bitmapDescriptor)
        marker.setAnchor(data.anchorU, data.anchorV)
    }

    private fun getItemIcon(marker: Meteorite): IconData {
        // Setting icon by meteorite category
        val drawable = when(marker.fall) {
            "Fell" -> AppCompatResources.getDrawable(context, R.drawable.meteorite)
            "Found" -> AppCompatResources.getDrawable(context, R.drawable.crater)
            else -> null
        }
        // Custom marker view
        mapMarkerView.setContent(
            circle = MapMarkerView.CircleContent.Marker,
            drawable
        )

        val icon: Bitmap = markerIconGenerator.makeIcon()
        val r = context.resources
        val middleBalloon = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, r.displayMetrics)
        return IconData(
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(icon),
            anchorU = middleBalloon / 2 / icon.width,
            anchorV = 1f
        )
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<Meteorite>,
        markerOptions: MarkerOptions
    ) {
        val data = getClusterIcon(cluster)
        markerOptions
            .icon(data.bitmapDescriptor)
            .anchor(data.anchorU, data.anchorV)
    }

    override fun onClusterUpdated(cluster: Cluster<Meteorite>, marker: Marker) {
        val data = getClusterIcon(cluster)
        marker.setIcon(data.bitmapDescriptor)
        marker.setAnchor(data.anchorU, data.anchorV)
    }

    private fun getClusterIcon(cluster: Cluster<Meteorite>): IconData {
        // Custom Cluster view
        mapMarkerView.setContent(
            circle = MapMarkerView.CircleContent.Cluster(
                count = cluster.size
            ),
            drawable = null
        )

        val icon: Bitmap = markerIconGenerator.makeIcon()
        val r = context.resources
        val middleBalloon = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, r.displayMetrics)
        return IconData(
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(icon),
            anchorU = middleBalloon / 2 / icon.width,
            anchorV = 1f
        )
    }

    override fun shouldRenderAsCluster(cluster: Cluster<Meteorite>): Boolean = cluster.size > 10

    private data class IconData(
        val bitmapDescriptor: BitmapDescriptor,
        val anchorU: Float,
        val anchorV: Float,
    )

    companion object {
        private const val TAG = "MapMarkersRenderer"
    }
}