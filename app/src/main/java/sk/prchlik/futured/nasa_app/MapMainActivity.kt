package sk.prchlik.futured.nasa_app

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import sk.prchlik.futured.nasa_app.databinding.ActivityMainBinding
import sk.prchlik.futured.nasa_app.model.Meteorite
import sk.prchlik.futured.nasa_app.view.MapMarkerView
import sk.prchlik.futured.nasa_app.view_model.MapMainActivityVM

class MapMainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "MapMainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var googleMap: GoogleMap
    private var mapFragment: SupportMapFragment? = null

    private val viewModel: MapMainActivityVM by viewModel()

    private var meteorites: MutableList<Meteorite> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap

        //TODO Save/Load state

        // Load the custom dark map style
        val darkMapStyle = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark)

        // Apply the dark map style to the map fragment
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setMapStyle(darkMapStyle)
        }

        // Initialize the map position
        val latLng = LatLng(48.685867415751666, 19.356373470760435) // specify your latitude and longitude here
        val zoomLevel = 7f // specify your zoom level here
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        googleMap.animateCamera(cameraUpdate)

        // Custom Listener
        val boundariesListener = BoundariesListener(googleMap)
        binding.mapContainer.loading.visibility = View.VISIBLE

        // Cluster Manager setup
        val clusterManager = ClusterManager<Meteorite>(this, googleMap)
        val mapRenderer = MapMarkersRenderer(
            context = this,
            map = googleMap,
            clusterManager = clusterManager
        )
        clusterManager.renderer = mapRenderer

        googleMap.setOnCameraIdleListener(boundariesListener)
        googleMap.setOnMarkerClickListener(boundariesListener)

        // Data visualization and updating
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            viewModel.dataFlow.combine(boundariesListener.boundariesFlow) { data, boundaries ->
                data to boundaries
            }.collect { (data, boundaries) ->
                //Clear map content
                withContext(Dispatchers.Main) {
                    clusterManager.clearItems()
                    binding.mapContainer.loading.visibility = View.VISIBLE
                }

                // Initialization of data for visualization
                if (meteorites.isEmpty()) {
                    meteorites = data.filter {
                        it.latLng != null && (it.latLng != LatLng(0.0,0.0) && it.fall != "Fell")
                    }.toMutableList()
                }

                // Filtering by visible map section
                val meteoritesFiltered = meteorites.filter { boundaries.contains(it.position) }

                // Adding markers to the map
                withContext(Dispatchers.Main) {
                    clusterManager.addItems(meteoritesFiltered)
                    clusterManager.cluster().runCatching {
                        binding.mapContainer.loading.visibility = View.GONE
                    }
                }
            }

        }
    }

    internal class BoundariesListener(
        private val map: GoogleMap,
    ) : GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

        private val _boundariesFlow = MutableSharedFlow<LatLngBounds>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val boundariesFlow: Flow<LatLngBounds> = _boundariesFlow

        override fun onCameraIdle() {
            val boundaries = map.projection.visibleRegion.latLngBounds
            _boundariesFlow.tryEmit(boundaries)
        }

        override fun onMarkerClick(marker: Marker): Boolean {
            // Show info window for the clicked marker
            marker.showInfoWindow()
            return true // Return true to indicate that the click event has been consumed
        }
    }

}

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
        mapMarkerView.setContent(
            circle = MapMarkerView.CircleContent.Marker
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
        mapMarkerView.setContent(
            circle = MapMarkerView.CircleContent.Cluster(
                count = cluster.size
            )
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

    override fun shouldRenderAsCluster(cluster: Cluster<Meteorite>): Boolean = cluster.size > 1

    private data class IconData(
        val bitmapDescriptor: BitmapDescriptor,
        val anchorU: Float,
        val anchorV: Float,
    )

    companion object {
        private const val TAG = "MapMarkersRenderer"
    }
}