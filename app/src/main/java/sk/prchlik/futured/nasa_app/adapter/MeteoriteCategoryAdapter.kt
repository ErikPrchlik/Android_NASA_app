package sk.prchlik.futured.nasa_app.adapter

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.prchlik.futured.nasa_app.R

class MeteoriteCategoryAdapter(private val context: Context?,
                               private val meteoriteCategories: MutableList<MeteoriteCategory>,
                               private val setListener: (MeteoriteCategory) -> Unit, // Callback for loading more data
                               private val loadData: (String) -> Unit

) : RecyclerView.Adapter<MeteoriteCategoryAdapter.MeteoriteCategoryViewHolder>() {

    private fun toggleCategoryExpansion(position: Int) {
        val category = meteoriteCategories[position]
        category.isExpanded = !category.isExpanded
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeteoriteCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meteorite_category, parent, false)
        return MeteoriteCategoryViewHolder(context!!, view, setListener, loadData)
    }

    override fun onBindViewHolder(holder: MeteoriteCategoryViewHolder, position: Int) {
        val meteoriteCategory = meteoriteCategories[position]
        holder.bind(meteoriteCategory)
        holder.itemView.setOnClickListener {
            toggleCategoryExpansion(position)
        }
    }

    override fun getItemCount(): Int {
        return meteoriteCategories.size
    }

    class MeteoriteCategoryViewHolder(private val context: Context, itemView: View,
                                      private val setListener: (MeteoriteCategory) -> Unit,
                                      private val loadData: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val meteoriteAdapter: MeteoriteAdapter = MeteoriteAdapter(mutableListOf())

        private val meteoritesRecyclerView: RecyclerView = itemView.findViewById(R.id.meteoritesRecyclerView)

        private val categoryTitleTextView: TextView = itemView.findViewById(R.id.categoryTitleTextView)
        private val categoryCountTextView: TextView = itemView.findViewById(R.id.categoryCount)
        private val expandButton: View = itemView.findViewById(R.id.expandButton)

        private val loadButton: ImageView = itemView.findViewById(R.id.load)

        init {
            meteoritesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            val verticalSpacingHeight = context.resources.getDimensionPixelSize(R.dimen.vertical_spacing) // Adjust as needed
            val itemDecoration = VerticalSpaceItemDecoration(verticalSpacingHeight)
            meteoritesRecyclerView.addItemDecoration(itemDecoration)
        }

        fun bind(meteoriteCategory: MeteoriteCategory) {
            Log.d("MCAdapter", "BIND")

            // Bind trip category data
            categoryTitleTextView.text = meteoriteCategory.category
            categoryCountTextView.text = meteoriteCategory.count.toString()

            // Set up sub-items RecyclerView
            this.meteoriteAdapter.updateData(meteoriteCategory.meteoritesToShow)
            meteoritesRecyclerView.adapter = meteoriteAdapter

            // Set visibility of sub-items RecyclerView based on expanded state
            meteoritesRecyclerView.visibility = if (meteoriteCategory.isExpanded) View.VISIBLE else View.GONE
            expandButton.rotation = if (meteoriteCategory.isExpanded) 180f else 0f

            setListener(meteoriteCategory)

            loadButton.setOnClickListener {
                loadData(meteoriteCategory.category)
            }

            if (meteoriteCategory.isExpanded && meteoriteCategory.hasMore) {
                loadButton.visibility = View.VISIBLE
            } else {
                loadButton.visibility = View.GONE
            }

        }

        class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.bottom = verticalSpaceHeight
                } else {
                    outRect.top = verticalSpaceHeight
                    outRect.bottom = verticalSpaceHeight
                }
            }
        }
    }
    fun updateCategory(category: MeteoriteCategory, recyclerView: RecyclerView) {
        Log.d("MCAdapter", "MCA updateCategory")
        val index = meteoriteCategories.indexOfFirst { it.category == category.category }
        if (index != -1) {
            safeNotifyItemChanged(index, recyclerView)
            category.isLoading = false
            meteoriteCategories[index] = category
        }
    }

    private fun safeNotifyItemChanged(index: Int, recyclerView: RecyclerView) {
        if (!recyclerView.isComputingLayout) {
            notifyItemChanged(index)
        } else {
            recyclerView.post {
                notifyItemChanged(index)
            }
        }
    }

}