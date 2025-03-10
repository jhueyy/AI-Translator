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
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class LiveChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var closeButton: ImageButton
    private lateinit var leftLanguageButton: Button
    private lateinit var rightLanguageButton: Button
    private lateinit var microphoneAnimation: LottieAnimationView

    private var leftLanguageCode: String? = null
    private var rightLanguageCode: String? = null

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var languagePreferences: SharedPreferences
    private lateinit var speechPreferences: SharedPreferences

    private val openAIRepository = OpenAIRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_chat)
        supportActionBar?.hide()

        // Initialize SharedPreferences separately
        languagePreferences = getSharedPreferences("LanguagePrefs", MODE_PRIVATE)
        speechPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        closeButton = findViewById(R.id.closeButton)
        leftLanguageButton = findViewById(R.id.leftLanguageButton)
        rightLanguageButton = findViewById(R.id.rightLanguageButton)
        microphoneAnimation = findViewById(R.id.microphoneAnimation)

        textToSpeech = TextToSpeech(this, this)

        loadSavedLanguages()

        closeButton.setOnClickListener { finish() }

        leftLanguageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            leftLanguagePickerLauncher.launch(intent)
        }

        rightLanguageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            rightLanguagePickerLauncher.launch(intent)
        }

        microphoneAnimation.setOnClickListener {
            if (leftLanguageCode == null || rightLanguageCode == null) {
                showToast(getString(R.string.select_two_languages))
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
        languagePreferences.edit().putString(key, value).apply()
    }

    private fun loadSavedLanguages() {
        leftLanguageCode = languagePreferences.getString("leftLanguageCode", null)
        rightLanguageCode = languagePreferences.getString("rightLanguageCode", null)

        val leftLanguageText = languagePreferences.getString("leftLanguageText", getString(R.string.select_language))
        val rightLanguageText = languagePreferences.getString("rightLanguageText", getString(R.string.select_language))

        leftLanguageButton.text = leftLanguageText
        rightLanguageButton.text = rightLanguageText
    }

    private fun startVoiceRecognition() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, leftLanguageCode ?: Locale.getDefault().language)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                microphoneAnimation.playAnimation()
            }

            override fun onBeginningOfSpeech() {}

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val detectedText = matches[0]
                    Log.d("SpeechRecognition", "Detected: $detectedText")
                    processTranslation(detectedText)
                }
                microphoneAnimation.pauseAnimation()
            }

            override fun onError(error: Int) {
                showToast(getString(R.string.speech_recognition_error))
                microphoneAnimation.pauseAnimation()
            }

            override fun onEndOfSpeech() {
                microphoneAnimation.pauseAnimation()
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    private fun processTranslation(detectedText: String) {
        if (leftLanguageCode == null || rightLanguageCode == null) {
            showToast(getString(R.string.language_codes_missing))
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val detectedLanguage = openAIRepository.detectLanguage(detectedText)

            val targetLanguage = when (detectedLanguage) {
                leftLanguageCode -> rightLanguageCode
                rightLanguageCode -> leftLanguageCode
                else -> {
                    showToast(getString(R.string.unrecognized_language))
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

        val speechRate = speechPreferences.getFloat("speechRate", 1.0f)
        val speechPitch = speechPreferences.getFloat("speechPitch", 1.0f)

        textToSpeech.setSpeechRate(speechRate)
        textToSpeech.setPitch(speechPitch)
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

            val speechRate = speechPreferences.getFloat("speechRate", 1.0f)
            val speechPitch = speechPreferences.getFloat("speechPitch", 1.0f)
            textToSpeech.setSpeechRate(speechRate)
            textToSpeech.setPitch(speechPitch)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
