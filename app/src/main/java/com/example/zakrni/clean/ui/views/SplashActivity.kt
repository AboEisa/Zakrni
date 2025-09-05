// ui/views/SplashActivity.kt
package com.example.zakrni.clean.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.zakrni.R
import com.example.zakrni.clean.data.Repo
import com.example.zakrni.clean.domain.IRepo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: IRepo

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                progressText.text = "Loading Quran data..."

                (repository as? Repo)?.preloadAllQuranData { current, total ->
                    runOnUiThread {
                        progressBar.max = total
                        progressBar.progress = current
                        progressText.text = "Loading Surah $current of $total..."
                    }
                }

                navigateToMain()
            } catch (e: Exception) {
                // Even on error, proceed to main
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}