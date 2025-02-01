package csc436.aitranslator

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var translateButton: Button
    private lateinit var micButton: ImageButton
    private lateinit var speakerButton: ImageButton
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        translateButton = findViewById(R.id.translateButton)
        micButton = findViewById(R.id.micButton)
        speakerButton = findViewById(R.id.speakerButton)

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.ENGLISH
            }
        }

        translateButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                translateText(text)
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
            }
        }

        speakerButton.setOnClickListener {
            textToSpeech.speak(outputText.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun translateText(text: String) {
        // Show "Translating..." before making the request
        outputText.text = "Translating..."
        outputText.setTextColor(Color.GRAY)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.translate(
                    TranslationRequest(
                        model = "gpt-3.5-turbo",
                        messages = listOf(
                            mapOf("role" to "system", "content" to "Translate this text."),
                            mapOf("role" to "user", "content" to text)
                        )
                    )
                )

                val translatedText = response.choices.getOrNull(0)?.message?.get("content") ?: "Translation failed."
                outputText.text = translatedText
                outputText.setTextColor(Color.BLACK)
            } catch (e: Exception) {
                outputText.text = "Translation failed."
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
