package csc436.aitranslator

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LiveChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var closeButton: ImageButton
    private lateinit var leftLanguageButton: Button
    private lateinit var rightLanguageButton: Button
    private lateinit var microphoneButton: ImageButton

    private var leftLanguageCode: String? = null
    private var rightLanguageCode: String? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var sharedPreferences: SharedPreferences

    private val openAIRepository = OpenAIRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_chat)
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences("LanguagePrefs", MODE_PRIVATE)

        closeButton = findViewById(R.id.closeButton)
        leftLanguageButton = findViewById(R.id.leftLanguageButton)
        rightLanguageButton = findViewById(R.id.rightLanguageButton)
        microphoneButton = findViewById(R.id.microphoneButton)

        textToSpeech = TextToSpeech(this, this)

        loadSavedLanguages() // Load last used languages

        closeButton.setOnClickListener { finish() }

        leftLanguageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            leftLanguagePickerLauncher.launch(intent)
        }

        rightLanguageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            rightLanguagePickerLauncher.launch(intent)
        }

        microphoneButton.setOnClickListener {
            if (leftLanguageCode == null || rightLanguageCode == null) {
                Toast.makeText(this, "Please select two languages first!", Toast.LENGTH_SHORT).show()
            } else {
                startVoiceRecognition()
            }
        }
    }

    private val leftLanguagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                leftLanguageCode = data?.getStringExtra("selectedCode")
                val selectedLanguage = data?.getStringExtra("selectedLanguage")
                leftLanguageButton.text = selectedLanguage

                saveLanguageSelection("leftLanguageCode", leftLanguageCode)
                saveLanguageSelection("leftLanguageText", selectedLanguage)
            }
        }

    private val rightLanguagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                rightLanguageCode = data?.getStringExtra("selectedCode")
                val selectedLanguage = data?.getStringExtra("selectedLanguage")
                rightLanguageButton.text = selectedLanguage

                saveLanguageSelection("rightLanguageCode", rightLanguageCode)
                saveLanguageSelection("rightLanguageText", selectedLanguage)
            }
        }

    private fun saveLanguageSelection(key: String, value: String?) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    private fun loadSavedLanguages() {
        leftLanguageCode = sharedPreferences.getString("leftLanguageCode", null)
        rightLanguageCode = sharedPreferences.getString("rightLanguageCode", null)

        val leftLanguageText = sharedPreferences.getString("leftLanguageText", "Select Language")
        val rightLanguageText = sharedPreferences.getString("rightLanguageText", "Select Language")

        leftLanguageButton.text = leftLanguageText
        rightLanguageButton.text = rightLanguageText
    }

    private fun startVoiceRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val detectedText = matches[0]
                    Log.d("SpeechRecognition", "Detected: $detectedText")
                    processTranslation(detectedText)
                }
            }

            override fun onError(error: Int) {
                Toast.makeText(this@LiveChatActivity, "Speech recognition error!", Toast.LENGTH_SHORT).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun processTranslation(detectedText: String) {
        if (leftLanguageCode == null || rightLanguageCode == null) {
            Toast.makeText(this, "Language codes missing!", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val detectedLanguage = openAIRepository.detectLanguage(detectedText)

            val targetLanguage = when (detectedLanguage) {
                leftLanguageCode -> rightLanguageCode
                rightLanguageCode -> leftLanguageCode
                else -> {
                    Toast.makeText(this@LiveChatActivity, "Unrecognized language!", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val translatedText = openAIRepository.translateText(detectedText, targetLanguage!!)
            speakText(translatedText, targetLanguage)
        }
    }

    private fun speakText(text: String, languageCode: String) {
        val locale = when (languageCode) {
            "es" -> Locale("es", "ES")
            "en" -> Locale("en", "US")
            else -> Locale.getDefault()
        }
        textToSpeech.language = locale
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
        }
    }
}
