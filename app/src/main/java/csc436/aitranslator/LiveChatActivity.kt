
package csc436.aitranslator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LiveChatActivity : AppCompatActivity() {

    private lateinit var closeButton: ImageButton
    private lateinit var leftLanguageButton: Button
    private lateinit var rightLanguageButton: Button
    private lateinit var microphoneButton: ImageButton

    private var leftLanguageCode: String? = null  // No default language
    private var rightLanguageCode: String? = null // No default language

    // Language Picker for Left Button
    private val leftLanguagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedLanguage = data?.getStringExtra("selectedLanguage")
                val selectedCode = data?.getStringExtra("selectedCode")

                if (selectedLanguage != null && selectedCode != null) {
                    leftLanguageButton.text = selectedLanguage
                    leftLanguageCode = selectedCode
                }
            }
        }

    // Language Picker for Right Button
    private val rightLanguagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedLanguage = data?.getStringExtra("selectedLanguage")
                val selectedCode = data?.getStringExtra("selectedCode")

                if (selectedLanguage != null && selectedCode != null) {
                    rightLanguageButton.text = selectedLanguage
                    rightLanguageCode = selectedCode
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_chat)
        supportActionBar?.hide() // Hide action bar

        // Initialize UI elements
        closeButton = findViewById(R.id.closeButton)
        leftLanguageButton = findViewById(R.id.leftLanguageButton)
        rightLanguageButton = findViewById(R.id.rightLanguageButton)
        microphoneButton = findViewById(R.id.microphoneButton)

        // Set default text for language buttons
        leftLanguageButton.text = "Select Language"
        rightLanguageButton.text = "Select Language"

        // Close button to exit LiveChatActivity
        closeButton.setOnClickListener {
            finish()
        }

        // Open Language Selection Screen for Left Button
        leftLanguageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            leftLanguagePickerLauncher.launch(intent)
        }

        // Open Language Selection Screen for Right Button
        rightLanguageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            rightLanguagePickerLauncher.launch(intent)
        }

        // Prevent mic activation until languages are selected
        microphoneButton.setOnClickListener {
            if (leftLanguageCode == null || rightLanguageCode == null) {
                Toast.makeText(this, "Please select two languages first!", Toast.LENGTH_SHORT).show()
            } else {
                startVoiceRecognition()
            }
        }
    }

    private fun startVoiceRecognition() {
        Toast.makeText(this, "Voice recognition not implemented yet!", Toast.LENGTH_SHORT).show()
    }
}
