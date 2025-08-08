package com.example.zakrni.clean.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.zakrni.R
import com.example.zakrni.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        updateTimeDisplay()
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(NamesOfAllahFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_names_of_allah -> {
                    loadFragment(NamesOfAllahFragment())
                    true
                }
                R.id.nav_dhikr -> {
                    loadFragment(DhikrFragment())
                    true
                }
                R.id.nav_supplication -> {
                    loadFragment(SupplicationFragment())
                    true
                }
                R.id.nav_hadith -> {
                    loadFragment(HadithFragment())
                    true
                }
                R.id.nav_remembrance -> {
                    loadFragment(RemembranceFragment())
                    true
                }
                R.id.nav_qibla -> {
                    loadFragment(QiblaFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun updateTimeDisplay() {
        // Set fixed time as requested: 21:30
        binding.timeDisplay.text = "21:30"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}