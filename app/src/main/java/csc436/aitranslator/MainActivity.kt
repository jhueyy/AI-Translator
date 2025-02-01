package csc436.aitranslator

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var translateButton: Button
    private lateinit var micButton: ImageButton
    private lateinit var speakerButton: ImageButton
    private lateinit var textToSpeechHelper: TextToSpeechHelper
    private val repository = OpenAIRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        translateButton = findViewById(R.id.translateButton)
        micButton = findViewById(R.id.micButton)
        speakerButton = findViewById(R.id.speakerButton)

        // Initialize Text-to-Speech
        textToSpeechHelper = TextToSpeechHelper(this)

        translateButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                translateText(text)
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
            }
        }

        speakerButton.setOnClickListener {
            textToSpeechHelper.speak(outputText.text.toString())
        }
    }

    private fun translateText(text: String) {
        outputText.text = "Translating..."
        outputText.setTextColor(Color.GRAY)

        lifecycleScope.launch {
            val translatedText = repository.translateText(text)
            outputText.text = translatedText
            outputText.setTextColor(Color.BLACK)
        }
    }

    override fun onDestroy() {
        textToSpeechHelper.shutdown()
        super.onDestroy()
    }
}
