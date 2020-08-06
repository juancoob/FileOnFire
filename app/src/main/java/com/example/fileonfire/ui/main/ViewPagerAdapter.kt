package com.example.fileonfire.ui.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fileonfire.util.DEFAULT_NUMBER_PAGES


/**
 * Creates the ViewPager2 adapter
 */
class ViewPagerAdapter(mainActivity: AppCompatActivity) : FragmentStateAdapter(mainActivity) {
    override fun getItemCount(): Int = DEFAULT_NUMBER_PAGES

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> CompressionFragment()
        1 -> GalleryFragment()
        else -> Fragment()
    }
}