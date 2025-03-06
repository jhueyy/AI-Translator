package csc436.aitranslator

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class LiveChatActivity : AppCompatActivity() {

    private lateinit var leftLanguageButton: Button
    private lateinit var rightLanguageButton: Button
    private lateinit var microphoneButton: ImageButton
    private lateinit var closeButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_chat)
        supportActionBar?.hide() // Hide action bar for fullscreen experience

        // Initialize UI components
        leftLanguageButton = findViewById(R.id.leftLanguageButton)
        rightLanguageButton = findViewById(R.id.rightLanguageButton)
        microphoneButton = findViewById(R.id.microphoneButton)
        closeButton = findViewById(R.id.closeButton)

        // Close button action
        closeButton.setOnClickListener {
            finish() // Closes the Live Chat screen
        }

        // TODO: Handle clicking language buttons
        leftLanguageButton.setOnClickListener {
            // TODO: Open language selection screen for left language
        }

        rightLanguageButton.setOnClickListener {
            // TODO: Open language selection screen for right language
        }

        // TODO: Start speech recognition when microphone is clicked
        microphoneButton.setOnClickListener {
            // TODO: Implement speech recognition here
        }
    }
}
