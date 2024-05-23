package sk.prchlik.futured.nasa_app.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import sk.prchlik.futured.nasa_app.activity.ListFragment

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val data: List<Pair<String, List<MeteoriteCategory>>>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun createFragment(position: Int): Fragment {
        return ListFragment.newInstance(ArrayList(data[position].second))
    }
}