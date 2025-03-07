package csc436.aitranslator

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.Switch

class SettingsActivity : AppCompatActivity() {

    private lateinit var settingsTitle: TextView
    private lateinit var themeSwitch: Switch
    private lateinit var speechRateSeekBar: SeekBar
    private lateinit var speechPitchSeekBar: SeekBar
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button
    private lateinit var closeButton: ImageButton

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide() // Hide action bar

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Initialize UI elements
        settingsTitle = findViewById(R.id.settingsTitle)
        themeSwitch = findViewById(R.id.themeSwitch)
        speechRateSeekBar = findViewById(R.id.speechRateSeekBar)
        speechPitchSeekBar = findViewById(R.id.speechPitchSeekBar)
        saveButton = findViewById(R.id.saveButton)
        resetButton = findViewById(R.id.resetButton)
        closeButton = findViewById(R.id.closeButton)

        // Load saved settings
        themeSwitch.isChecked = sharedPreferences.getBoolean("darkMode", false)
        speechRateSeekBar.progress = (sharedPreferences.getFloat("speechRate", 1.0f) * 10).toInt()
        speechPitchSeekBar.progress = (sharedPreferences.getFloat("speechPitch", 1.0f) * 10).toInt()

        // Theme toggle
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Save button action
        saveButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("darkMode", themeSwitch.isChecked)
            editor.putFloat("speechRate", speechRateSeekBar.progress / 10f)
            editor.putFloat("speechPitch", speechPitchSeekBar.progress / 10f)
            editor.apply()
            finish() // Close settings page
        }

        // Reset button action
        resetButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean("darkMode", false) // Default: Light Mode
            editor.putFloat("speechRate", 1.0f)  // Default: Normal Speed
            editor.putFloat("speechPitch", 1.0f) // Default: Normal Pitch
            editor.apply()

            // Reset UI components
            themeSwitch.isChecked = false
            speechRateSeekBar.progress = 10
            speechPitchSeekBar.progress = 10
        }

        // Close button action (Return to MainActivity)
        closeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
