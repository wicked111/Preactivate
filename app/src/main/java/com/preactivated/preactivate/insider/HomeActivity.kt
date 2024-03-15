package com.preactivated.preactivate.insider

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.profile.fragments.AndroidFragment
import com.preactivated.preactivate.insider.profile.fragments.ExploreFragment
import com.preactivated.preactivate.insider.profile.fragments.FlutterFragment
import com.preactivated.preactivate.insider.profile.fragments.PythonFragment
import com.preactivated.preactivate.insider.profile.fragments.ReactFragment

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.background)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.background)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }


        val searchbtn = findViewById<Button>(R.id.searchInputbtn)
        searchbtn.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        viewPager = findViewById(R.id.view_pager_preactivate)
        viewPager.adapter = ViewPagerAdapter(this)

        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayoutpreactivate)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Explore"
                1 -> tab.text = "Android"
                2 -> tab.text = "Flutter"
                3 -> tab.text = "React"
                4 -> tab.text = "Python"
            }
        }.attach()
    }

    private inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return 5 // Number of tabs
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ExploreFragment()
                1 -> AndroidFragment()
                2 -> FlutterFragment()
                3 -> ReactFragment()
                4 -> PythonFragment()
                else -> throw IllegalStateException("Unexpected position: $position")
            }
        }
    }
}