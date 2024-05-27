package sk.prchlik.futured.nasa_app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sk.prchlik.futured.nasa_app.R
import sk.prchlik.futured.nasa_app.model.Meteorite

class MeteoriteAdapter(
    private val meteorites: MutableList<Meteorite>
) : RecyclerView.Adapter<MeteoriteAdapter.MeteoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeteoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meteorite, parent, false)
        return MeteoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeteoriteViewHolder, position: Int) {
        val meteorite = meteorites[position]
        // Bind meteorite data
        holder.bind(meteorite)
        holder.itemView.setOnClickListener {
            // Start detail activity
//            val intent = Intent(holder.itemView.context, TripDetailActivity::class.java)
//            intent.putExtra("TRIP", trip) // Pass any necessary data to the detail activity
//            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return meteorites.size
    }

    inner class MeteoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.meteoriteName)
        private val massCategoryTextView: TextView = itemView.findViewById(R.id.meteoriteMassCategory)
        private val massTextView: TextView = itemView.findViewById(R.id.meteoriteMass)

        // Bind views
        fun bind(meteorite: Meteorite) {
            // Bind trip category data
            titleTextView.text = meteorite.name
            massCategoryTextView.text = meteorite.massCategory
            massTextView.text = meteorite.mass
        }
    }

    fun updateData(newData: List<Meteorite>) {
        Log.d("MeteoriteAdapter", "MA updateData")
        // Method to update the meteorite list and notify the adapter
        meteorites.clear()
        meteorites.addAll(newData)
        notifyDataSetChanged()
    }
}