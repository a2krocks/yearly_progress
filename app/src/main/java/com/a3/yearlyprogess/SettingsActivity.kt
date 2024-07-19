package com.a3.yearlyprogess

import android.icu.text.NumberFormat
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val appBarLayout: AppBarLayout = findViewById(R.id.appBarLayout)


        ViewCompat.setOnApplyWindowInsetsListener(appBarLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = insets.top)
            WindowInsetsCompat.CONSUMED
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val updateFrequencyPreference =
                findPreference<Preference>(getString(R.string.widget_widget_update_frequency))
            val defaultUpdateFrequencyPreferenceSummary = getString(R.string.adjust_widget_frequency_summary)

            updatePreferenceSummary(updateFrequencyPreference, defaultUpdateFrequencyPreferenceSummary) { value ->
                (value as? Int ?: 5).toDuration(DurationUnit.SECONDS).toString()
            }

        }

        private fun updatePreferenceSummary(preference: Preference?, defaultSummary: String, formatValue: (Any?) -> String) {
            preference?.let {
                val currentValue = it.sharedPreferences?.all?.get(it.key) ?: return
                it.summary = "$defaultSummary\nCurrent value: ${formatValue(currentValue)}"
                it.setOnPreferenceChangeListener { pref, newValue ->
                    pref.summary = "$defaultSummary\nCurrent value: ${formatValue(newValue)}"
                    true
                }
            }
        }
    }
}