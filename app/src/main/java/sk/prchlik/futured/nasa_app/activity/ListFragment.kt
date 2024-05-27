package sk.prchlik.futured.nasa_app.activity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.prchlik.futured.nasa_app.R
import sk.prchlik.futured.nasa_app.adapter.MeteoriteCategory
import sk.prchlik.futured.nasa_app.adapter.MeteoriteCategoryAdapter
import sk.prchlik.futured.nasa_app.databinding.FragmentListBinding
import sk.prchlik.futured.nasa_app.utils.parcelableArrayList

/**
 * A simple [Fragment] subclass.
 * Use the [ListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ListFragment : Fragment() {

    private lateinit var bind: FragmentListBinding
    private lateinit var recyclerView: RecyclerView

    private lateinit var meteoriteCategoryAdapter: MeteoriteCategoryAdapter

    private var meteoriteCategories: ArrayList<MeteoriteCategory> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentListBinding.inflate(inflater, container, false)
        recyclerView = bind.recyclerView
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Set LayoutManager
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Set data
        val list: ArrayList<MeteoriteCategory>? = arguments?.parcelableArrayList<MeteoriteCategory>(
            ARG_LIST
        )
        list?.let {
            meteoriteCategories = it

            // Initialize adapters
            meteoriteCategoryAdapter = MeteoriteCategoryAdapter(context, it,
                { category -> setListener(category) },
                { categoryName -> loadMoreData(categoryName) }
            )

            // Set adapters to RecyclerViews
            val verticalSpacingHeight = this.resources.getDimensionPixelSize(R.dimen.vertical_spacing) // Adjust as needed
            val itemDecoration = MeteoriteCategoryAdapter.MeteoriteCategoryViewHolder.VerticalSpaceItemDecoration(
                verticalSpacingHeight
            )

            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.adapter = meteoriteCategoryAdapter

        }
    }

    private fun setListener(meteoriteCategory: MeteoriteCategory) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                Log.d("MCAdapter", "onScrolled")
                // Only trigger loadMoreData when scrolling down
                // Trigger load more when reaching the end of the list
                if (!meteoriteCategory.isLoading && meteoriteCategory.hasMore
                    && !recyclerView.canScrollVertically(1)
                ) {
                    Log.d("MCAdapter", "onLoadMore called")
                    loadMoreData(meteoriteCategory.category)
                }
            }
        })

    }

    private fun loadMoreData(categoryName: String) {
        val categoryIndex = meteoriteCategories.indexOfFirst { it.category == categoryName }
        if (categoryIndex != -1) {
            val category = meteoriteCategories[categoryIndex].copy(isLoading = true)
            if (category.isExpanded) {
                Log.d("ListFragment", "LOAD MORE DATA")
                var meteoritesToAdd = category.meteoritesToFetch
                if (meteoritesToAdd.size > 20) {
                    meteoritesToAdd = meteoritesToAdd.subList(0,20)
                }
                category.meteoritesToShow.addAll(meteoritesToAdd)
                category.meteoritesToFetch.removeAll(meteoritesToAdd)

                val updatedCategory = category.copy(
                    meteoritesToShow = category.meteoritesToShow,
                    meteoritesToFetch = category.meteoritesToFetch,
                    hasMore = category.meteoritesToFetch.isNotEmpty()
                )
                meteoriteCategoryAdapter.updateCategory(updatedCategory, bind.recyclerView)
            }
        }
    }

    companion object {

        private const val ARG_LIST = "list"

        fun newInstance(list: ArrayList<MeteoriteCategory>): ListFragment {
            val fragment = ListFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_LIST, list)
            fragment.arguments = args
            return fragment
        }
    }
}