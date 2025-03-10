package csc436.aitranslator

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class SettingsActivity : AppCompatActivity() {

    private lateinit var speechRateSeekBar: SeekBar
    private lateinit var speechPitchSeekBar: SeekBar
    private lateinit var saveButton: Button
    private lateinit var resetButton: Button
    private lateinit var closeButton: ImageButton

    private lateinit var sharedPreferences: SharedPreferences

    private var originalSpeechRate: Float = 1.0f
    private var originalSpeechPitch: Float = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide() // Hide action bar

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // Initialize UI elements
        speechRateSeekBar = findViewById(R.id.speechRateSeekBar)
        speechPitchSeekBar = findViewById(R.id.speechPitchSeekBar)
        saveButton = findViewById(R.id.saveButton)
        resetButton = findViewById(R.id.resetButton)
        closeButton = findViewById(R.id.closeButton)

        // Set button text using string resources
        saveButton.text = getString(R.string.save)
        resetButton.text = getString(R.string.reset)

        // Load saved settings (Use default if not found)
        loadSettings()

        // Disable reset button initially if no changes
        updateResetButtonState()

        // Listen for changes in SeekBars
        speechRateSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)
        speechPitchSeekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        // Save button action
        saveButton.setOnClickListener {
            saveSettings()
            finish() // Closes settings activity - goes back to main
        }

        // Reset button action (Shows confirmation before reset)
        resetButton.setOnClickListener {
            showResetConfirmationDialog()
        }

        // Close button action (Go back to MainActivity)
        closeButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadSettings() // ✅ Reload settings when returning
        updateResetButtonState() // ✅ Ensure reset button state is updated
    }

    // Load settings from SharedPreferences
    private fun loadSettings() {
        originalSpeechRate = sharedPreferences.getFloat("speechRate", 1.0f)
        originalSpeechPitch = sharedPreferences.getFloat("speechPitch", 1.0f)

        speechRateSeekBar.progress = (originalSpeechRate * 10).toInt()
        speechPitchSeekBar.progress = (originalSpeechPitch * 10).toInt()

        updateResetButtonState() // ✅ Ensure button state updates after loading settings
    }

    // Save settings
    private fun saveSettings() {
        val newSpeechRate = speechRateSeekBar.progress / 10f
        val newSpeechPitch = speechPitchSeekBar.progress / 10f

        val editor = sharedPreferences.edit()
        editor.putFloat("speechRate", newSpeechRate)
        editor.putFloat("speechPitch", newSpeechPitch)
        editor.apply()

        loadSettings() // ✅ Reload values after saving to ensure proper comparison
    }

    // Show a confirmation dialog before resetting
    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.reset_settings))
            .setMessage(getString(R.string.reset_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> resetSettings() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    // Reset settings and reload UI
    private fun resetSettings() {
        val editor = sharedPreferences.edit()
        editor.putFloat("speechRate", 1.0f)
        editor.putFloat("speechPitch", 1.0f)
        editor.apply()

        loadSettings() // ✅ Reload settings after reset
    }

    // Check if settings have changed
    private fun hasSettingsChanged(): Boolean {
        val currentSpeechRate = speechRateSeekBar.progress / 10f
        val currentSpeechPitch = speechPitchSeekBar.progress / 10f

        return currentSpeechRate != 1.0f || currentSpeechPitch != 1.0f
    }

    // Enable or disable reset button based on changes
    private fun updateResetButtonState() {
        resetButton.isEnabled = hasSettingsChanged()
        resetButton.alpha = if (resetButton.isEnabled) 1.0f else 0.5f // Grey out if disabled
    }

    // SeekBar Change Listener
    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateResetButtonState()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}
