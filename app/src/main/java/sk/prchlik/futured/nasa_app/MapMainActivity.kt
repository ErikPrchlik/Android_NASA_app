package sk.prchlik.futured.nasa_app

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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

    private lateinit var clusterManager: ClusterManager<Meteorite>
    private lateinit var boundariesListener: BoundariesListener

    private var meteorites: MutableList<Meteorite> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this)

        // Meteorite list view
        binding.listView.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.list_view).show()
            //TODO start new activity
        }

        // Filter of meteorites menu settings
        setUpFilter()

        // Refresh button on communication error
        onRefreshClick()
    }

    private fun onRefreshClick() {
        // New load data request
        binding.refresh.setOnClickListener {
            viewModel.getData()
            binding.mapContainer.loading.visibility = View.VISIBLE
            binding.refresh.visibility = View.GONE
        }
    }

    private fun setUpFilter() {
        // Behavior of filter

        // Category Fell chosen for filtering
        showFell()

        // Category Found chosen for filtering
        showFound()

        // Clear filter and show all
        removeFilter()

        binding.filter.setOnClickListener {
            if (binding.menu.visibility != View.VISIBLE) {
                binding.menu.visibility = View.VISIBLE
            } else {
                binding.menu.visibility = View.GONE
            }
        }
    }

    private fun removeFilter() {
        binding.filterClear.setOnClickListener {
            // Update the map on background thread
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                viewModel.dataFlow.combine(boundariesListener.boundariesFlow) { data, boundaries ->
                    data to boundaries
                }.collect { (data, boundaries) ->
                    // Show meteorites by boundaries
                    updateData(data, boundaries) { boundaries.contains(it.position) }
                }
            }
            binding.filterClear.visibility = View.GONE
            binding.menuFell.setTextColor(this.getColor(R.color.my_secondary))
            binding.menuFound.setTextColor(this.getColor(R.color.my_secondary))
        }
    }

    private fun showFound() {
        binding.menuFound.setOnClickListener { view ->
            (view as TextView).setTextColor(this.getColor(R.color.my_primary))
            binding.menuFell.setTextColor(this.getColor(R.color.my_secondary))

            // Update the map by filter on background thread
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                viewModel.dataFlow.combine(boundariesListener.boundariesFlow) { data, boundaries ->
                    data to boundaries
                }.collect { (data, boundaries) ->
                    // Given lambda function as specific filter for update
                    updateData(data, boundaries) { boundaries.contains(it.position) && it.fall == "Found" }
                }
            }

            binding.menu.visibility = View.GONE
            binding.filterClear.visibility = View.VISIBLE
        }
    }

    private fun showFell() {
        binding.menuFell.setOnClickListener { view ->
            (view as TextView).setTextColor(this.getColor(R.color.my_primary))
            binding.menuFound.setTextColor(this.getColor(R.color.my_secondary))

            // Update the map by filter on background thread
            val scope = CoroutineScope(Job() + Dispatchers.IO)
            scope.launch {
                viewModel.dataFlow.combine(boundariesListener.boundariesFlow) { data, boundaries ->
                    data to boundaries
                }.collect { (data, boundaries) ->
                    // Given lambda function as specific filter for update
                    updateData(data, boundaries) { boundaries.contains(it.position) && it.fall == "Fell" }
                }
            }

            binding.menu.visibility = View.GONE
            binding.filterClear.visibility = View.VISIBLE
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

    private suspend fun updateData(data: MutableList<Meteorite>,
                                   boundaries: LatLngBounds,
                                   f: (Meteorite) -> Boolean) {
        // Refresh needed
        if (data.isEmpty()) {
            binding.refresh.visibility = View.VISIBLE
        }

        // Clear map content
        withContext(Dispatchers.Main) {
            clusterManager.clearItems()
            binding.mapContainer.loading.visibility = View.VISIBLE
        }

        // Initialization of data for visualization
        if (meteorites.isEmpty()) {
            meteorites = data.filter {item ->
                item.latLng != null || (item.latLng == LatLng(0.0, 0.0) && item.fall != "Fell")
            }.toMutableList()
        }

        // Filtering by visible map section
        val meteoritesFiltered = meteorites.filter(f)

        // Adding markers to the map
        withContext(Dispatchers.Main) {
            clusterManager.addItems(meteoritesFiltered)
            clusterManager.cluster().runCatching {
                binding.mapContainer.loading.visibility = View.GONE
            }
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
        boundariesListener = BoundariesListener(googleMap)
        binding.mapContainer.loading.visibility = View.VISIBLE

        // Cluster Manager setup
        clusterManager = ClusterManager<Meteorite>(this, googleMap)
        val mapRenderer = MapMarkersRenderer(
            context = this,
            map = googleMap,
            clusterManager = clusterManager
        )
        clusterManager.renderer = mapRenderer

        // Changing of boundaries for updating
        googleMap.setOnCameraIdleListener(boundariesListener)

        // Data visualization and updating
        loadData()

        // OnClick actions
        onClusterClick()
        onClusterItemClick()
    }

    private fun onClusterItemClick() {
        // Show InfoWindow when clicked
        clusterManager.setOnClusterItemClickListener { item ->
            // Get the underlying marker associated with the clicked ClusterItem
            val marker = clusterManager.markerCollection.markers
                .find { it.position == item.position }

            marker?.title = item.title
            marker?.snippet = item.fall
            marker?.setInfoWindowAnchor(0.4f, 0.5f)
            // Show the info window for the marker
            marker?.showInfoWindow()
            clusterManager.cluster()

            // Return true to indicate that the listener has consumed the event
            true
        }
    }

    private fun onClusterClick() {
        // Zoom to the center of cluster
        clusterManager.setOnClusterClickListener {
            val latLng = it.position
            val zoomLevel = googleMap.cameraPosition.zoom+0.5f
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
            googleMap.animateCamera(cameraUpdate)
            clusterManager.cluster()
            true
        }
    }

    private fun loadData() {
        // Data visualization and updating on background thread
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            // Collecting current data and boundaries
            viewModel.dataFlow.combine(boundariesListener.boundariesFlow) { data, boundaries ->
                data to boundaries
            }.collect { (data, boundaries) ->
                // Show meteorites by boundaries
                updateData(data, boundaries) { boundaries.contains(it.position) }
                binding.filter.visibility = View.VISIBLE
            }
        }
    }

    internal class BoundariesListener(
        private val map: GoogleMap,
    ) : GoogleMap.OnCameraIdleListener {

        private val _boundariesFlow = MutableSharedFlow<LatLngBounds>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val boundariesFlow: Flow<LatLngBounds> = _boundariesFlow

        override fun onCameraIdle() {
            val boundaries = map.projection.visibleRegion.latLngBounds
            _boundariesFlow.tryEmit(boundaries)
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