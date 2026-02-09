// ui/views/SplashActivity.kt
package com.zakrni.app.clean.ui.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zakrni.app.R
import com.zakrni.app.clean.data.Repo
import com.zakrni.app.clean.domain.IRepo
import com.zakrni.app.clean.ui.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: IRepo

    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleHelper.enforceLtr(this)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                progressText.text = localizedText(
                    "جاري تحميل بيانات القرآن...",
                    "Loading Quran data..."
                )

                (repository as? Repo)?.preloadAllQuranData { current, total ->
                    runOnUiThread {
                        progressBar.max = total
                        progressBar.progress = current
                        progressText.text = localizedText(
                            "جاري تحميل سورة $current من $total...",
                            "Loading surah $current of $total..."
                        )
                    }
                }

                navigateToMain()
            } catch (e: Exception) {
                // Even on error, proceed to main
                navigateToMain()
            }
        }
    }

    private fun localizedText(arabic: String, english: String): String {
        return if (LocaleHelper.isArabic(this)) arabic else english
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
