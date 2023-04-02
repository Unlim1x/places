package ru.lim1x.places.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.ListFragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adapter(val listFragment: ArrayList<Fragment>, fm : FragmentManager,
                        lifecycle : Lifecycle) : FragmentStateAdapter(fm, lifecycle){
    override fun getItemCount(): Int {
        return listFragment.size
    }

    override fun createFragment(position: Int): Fragment {
        return listFragment[position]
    }
}