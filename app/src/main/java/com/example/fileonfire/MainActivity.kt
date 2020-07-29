package com.example.fileonfire

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.fileonfire.databinding.MainActivityBinding
import com.example.fileonfire.ui.main.ViewPagerAdapter
import com.example.fileonfire.util.PAGE_SELECTED

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.mainViewPager.adapter = ViewPagerAdapter(this)
        manageBottomNavigationView()
        if(savedInstanceState != null) {
            binding.mainViewPager.setCurrentItem(savedInstanceState.getInt(PAGE_SELECTED))
        }
    }

    /**
     * Checks whether the user slides on the screens or clicks on the bottom navigation menu
     * to move between fragments
     */
    private fun manageBottomNavigationView() {
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.mainBottomNavigation.menu.getItem(position).isChecked = true
            }
        })

        binding.mainBottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.compress -> binding.mainViewPager.setCurrentItem(0, true)
                R.id.gallery -> binding.mainViewPager.setCurrentItem(1, true)
            }
            true
        }
    }

    override fun onBackPressed() {
        if(binding.mainViewPager.currentItem == 0){
            super.onBackPressed()
        } else {
            binding.mainViewPager.currentItem = binding.mainViewPager.currentItem - 1
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt(PAGE_SELECTED, binding.mainViewPager.currentItem)
    }
}
