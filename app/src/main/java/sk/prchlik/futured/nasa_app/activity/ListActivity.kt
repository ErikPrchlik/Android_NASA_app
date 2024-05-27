package sk.prchlik.futured.nasa_app.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import sk.prchlik.futured.nasa_app.adapter.MeteoriteCategory
import sk.prchlik.futured.nasa_app.adapter.ViewPagerAdapter
import sk.prchlik.futured.nasa_app.databinding.ActivityListBinding
import sk.prchlik.futured.nasa_app.model.Meteorite
import sk.prchlik.futured.nasa_app.view_model.MapMainActivityVM

class ListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ListActivity"
    }

    private lateinit var binding: ActivityListBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private val viewModel: MapMainActivityVM by viewModel()

    private var meteorites: MutableList<Meteorite> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.listContainer.viewPager
        tabLayout = binding.listContainer.tabLayout

        binding.loading.visibility = View.VISIBLE

        updateContent(null)

        prepareData()

        onRefreshClick()

        binding.mapView.setOnClickListener { view ->
            finish()
        }
    }

    private fun updateContent(data: List<Pair<String, List<MeteoriteCategory>>>?) {
        val dataCollection =  if (data.isNullOrEmpty()) {
            listOf(
                "ALL" to mutableListOf(
                    MeteoriteCategory(
                        "Meteorites",
                        meteoritesToShow =  mutableListOf(),
                        meteoritesToFetch = mutableListOf(),
                        hasMore = false
                    )
                )
            )
        } else {
            data
        }

        viewPagerAdapter = ViewPagerAdapter(this@ListActivity, dataCollection)
        viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = dataCollection[position].first
        }.attach()
    }

    private fun prepareData() {
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        scope.launch {
            viewModel.dataFlow.collect { data ->
                if (data.isNotEmpty()) {
                    meteorites = data
                    meteorites.sortedBy { it.name }
                    massCategorizeMeteorites()

                    var filtered = meteorites.filter { !it.fall.isNullOrEmpty() }
                    var filteredMap = filtered.groupBy { it.fall }
                    val dataByCategory = getCategoryData(filteredMap)


                    filtered = meteorites.filter { !it.date.isNullOrEmpty() }
                        .sortedWith(compareByDescending<Meteorite> {it.getYear()} .thenBy {it.name})
                    filteredMap = filtered.groupBy { it.getYear() }
                    val dataByTime = getCategoryData(filteredMap)

                    filtered = meteorites
                        .filter { !it.massCategory.isNullOrEmpty() }
                        .sortedByDescending { it.mass }
                    filteredMap = filtered.groupBy { it.massCategory }
                    val dataByMass = getCategoryData(filteredMap)

                    val dataCollection = listOf(
                        "ALL" to mutableListOf(
                            MeteoriteCategory(
                                "Meteorites",
                                meteoritesToShow = meteorites.subList(0, 20).toMutableList(),
                                meteoritesToFetch = (
                                        meteorites - meteorites.subList(0, 20).toSet()
                                        ).toMutableList(),
                                hasMore = true
                            )
                        ),
                        "Category" to dataByCategory,
                        "Time" to dataByTime,
                        "Mass" to dataByMass
                    )

                    withContext(Dispatchers.Main) {
                        updateContent(dataCollection)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.refresh.visibility = View.VISIBLE
                    }
                }
                withContext(Dispatchers.Main) {
                    binding.loading.visibility = View.GONE
                }
            }
        }
    }

    private fun getCategoryData(meteoritesMap: Map<String?, List<Meteorite>>): MutableList<MeteoriteCategory> {
        val data = mutableListOf<MeteoriteCategory>()
        meteoritesMap.forEach { (category, meteorites) ->
            var meteoritesToShow = meteorites
            val meteoritesToFetch = mutableListOf<Meteorite>()
            if (meteorites.size > 20) {
                meteoritesToShow = meteorites.subList(0,20)
                meteoritesToFetch.addAll(meteorites - meteorites.subList(0,20).toSet())
            }
            data.add(
                MeteoriteCategory(
                    category = category!!,
                    meteoritesToShow =  meteoritesToShow.toMutableList(),
                    meteoritesToFetch = meteoritesToFetch,
                    hasMore = meteoritesToFetch.isNotEmpty()
                )
            )
        }
        return data
    }

    private fun onRefreshClick() {
        // New load data request
        binding.refresh.setOnClickListener {
            viewModel.refreshData()
            binding.loading.visibility = View.VISIBLE
            binding.refresh.visibility = View.GONE
        }
    }

    private fun massCategorizeMeteorites() {
        // Convert mass to Double and filter out null or non-numeric values
        Log.d(TAG, meteorites.toString())
        val massList = meteorites.mapNotNull { it.mass?.toDoubleOrNull() }.sorted()

        // Calculate breakpoints for three classes
        val size = massList.size
        val firstBreak = massList[size / 4]
        val secondBreak = massList[size / 2]
        val thirdBreak = massList[3 * size / 4]

        // Assign categories based on breakpoints
        meteorites.forEach { meteorite ->
            val mass = meteorite.mass?.toDoubleOrNull()
            meteorite.massCategory = when {
                mass == null -> null // Use -1 or any other value to indicate invalid mass
                mass <= firstBreak -> "Small"
                mass <= secondBreak -> "Medium"
                mass <= thirdBreak -> "Large"
                else -> "Massive"
            }
        }
    }

}