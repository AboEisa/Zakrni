package com.example.zakrni.clean.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.zakrni.R
import com.example.zakrni.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private val navOptions by lazy {
        NavOptions.Builder()
            .setEnterAnim(R.anim.animation)
            .setExitAnim(R.anim.animation2)
            .setPopEnterAnim(R.anim.animation3)
            .setPopExitAnim(R.anim.animation4)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set up edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, systemBars.bottom)
            insets
        }

        setupNavigation()
        setupCustomBottomNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set initial destination
        selectNavItem(R.id.nav_all_categories)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun selectNavItem(selectedId: Int) {
        binding.navPrayerTimes.background = null
        binding.navAllMedia.background = null
        binding.navAllCategories.background = null

        val unselectedColor = ContextCompat.getColor(this, android.R.color.white)
        val selectedColor = ContextCompat.getColor(this, R.color.selected_text_color)

        binding.navPrayerTimes.setTextColor(unselectedColor)
        binding.navAllMedia.setTextColor(unselectedColor)
        binding.navAllCategories.setTextColor(unselectedColor)



        val selectedBackground = ContextCompat.getDrawable(this, R.drawable.selected_nav_background)

        when (selectedId) {
            R.id.nav_prayer_times -> {
                binding.navPrayerTimes.background = selectedBackground
                binding.navPrayerTimes.setTextColor(selectedColor)

            }
            R.id.nav_all_media -> {
                binding.navAllMedia.background = selectedBackground
                binding.navAllMedia.setTextColor(selectedColor)

            }
            R.id.nav_all_categories -> {
                binding.navAllCategories.background = selectedBackground
                binding.navAllCategories.setTextColor(selectedColor)

            }
        }
    }

    private fun setupCustomBottomNavigation() {
        binding.navPrayerTimes.setOnClickListener {
            if (navController.currentDestination?.id != R.id.playerTimesFragment) {
                selectNavItem(R.id.nav_prayer_times)
                navController.navigate(R.id.playerTimesFragment, null, navOptions)
            }
        }
        binding.navAllMedia.setOnClickListener {
            if (navController.currentDestination?.id != R.id.allMediaFragment) {
                selectNavItem(R.id.nav_all_media)
                navController.navigate(R.id.allMediaFragment, null, navOptions)
            }
        }
        binding.navAllCategories.setOnClickListener {
            if (navController.currentDestination?.id != R.id.allCategoriesFragment) {
                selectNavItem(R.id.nav_all_categories)
                navController.navigate(R.id.allCategoriesFragment, null, navOptions)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.playerTimesFragment -> selectNavItem(R.id.nav_prayer_times)
                R.id.allMediaFragment -> selectNavItem(R.id.nav_all_media)
                R.id.allCategoriesFragment -> selectNavItem(R.id.nav_all_categories)
            }
        }
    }





    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}