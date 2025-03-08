package csc436.aitranslator

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var outputText: TextView
    private lateinit var translateButton: Button
    private lateinit var speakerButton: ImageButton
    private lateinit var copyButton: ImageButton
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var textToSpeechHelper: TextToSpeechHelper
    private lateinit var languageButton: Button
    private lateinit var chatButton: ImageButton
    private lateinit var settingsButton: ImageButton
    //    private lateinit var inputMicButton: ImageButton
    private lateinit var inputSpeakerButton: ImageButton

    private var selectedLanguageCode = "en" // Default to English

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(OpenAIRepository()) }

    // New API: Activity Result Launcher for Language Selection
    private val languagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedLanguage = data?.getStringExtra("selectedLanguage")
                val selectedCode = data?.getStringExtra("selectedCode")

                if (selectedLanguage != null && selectedCode != null) {
                    languageButton.text = selectedLanguage // Update button text
                    selectedLanguageCode = selectedCode // Store selected language code
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide() // Hide the AI Translator action bar

        // Initialize UI elements
        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        translateButton = findViewById(R.id.translateButton)
        speakerButton = findViewById(R.id.speakerButton)
        copyButton = findViewById(R.id.copyButton)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        languageButton = findViewById(R.id.targetLanguageButton)
        chatButton = findViewById(R.id.chatButton)
        settingsButton = findViewById(R.id.settingsButton)
//        inputMicButton = findViewById(R.id.inputMicButton)
        inputSpeakerButton = findViewById(R.id.inputSpeakerButton)

        textToSpeechHelper = TextToSpeechHelper(this)

        // Open Language Selection Screen
        languageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languagePickerLauncher.launch(intent) // Use new API
        }

        // Open Live Chat Screen
        chatButton.setOnClickListener {
            val intent = Intent(this, LiveChatActivity::class.java)
            startActivity(intent)
        }

        // Open Settings Screen
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Remove hint when typing
        inputText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputText.hint = ""
            } else if (inputText.text.isEmpty()) {
                inputText.hint = "Enter text to translate..."
            }
        }

        // Hide keyboard when clicking outside input field
        findViewById<View>(R.id.rootLayout).setOnTouchListener { view, _ ->
            inputText.clearFocus()
            hideKeyboard()
            view.performClick()
            false
        }

        // Copy translated text
        copyButton.setOnClickListener {
            val textToCopy = outputText.text.toString().trim()
            if (textToCopy.isNotEmpty() && textToCopy != "Translation will appear here...") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Translated Text", textToCopy)
                clipboard.setPrimaryClip(clip)

                // Change button to check icon
                copyButton.setImageResource(R.drawable.ic_check)

                // Revert back to copy icon after 1.5 seconds
                copyButton.postDelayed({
                    copyButton.setImageResource(R.drawable.ic_copy)
                }, 1500)
            }
        }

        // Handle translation button
        translateButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    translateButton.isEnabled = false // Disable button while translating
                    startTranslatingAnimation()
                    viewModel.translateText(text, selectedLanguageCode)
                } else {
                    Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle speech output
        speakerButton.setOnClickListener {
            val text = outputText.text.toString().trim()
            if (text.isNotEmpty() && text != "Translation will appear here...") {
                textToSpeechHelper.speak(text)
            }
        }


//        inputMicButton.setOnClickListener {
//            Toast.makeText(this, "Voice input not implemented yet!", Toast.LENGTH_SHORT).show()
//        }

        // 🔊 Play Input Text
        inputSpeakerButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                textToSpeechHelper.speak(text)
            }
        }

        observeViewModel()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.translation.collectLatest { translatedText ->
                outputText.text = translatedText
                translateButton.text = "Translate" // Reset button text
                translateButton.isEnabled = true   // Re-enable button
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                if (isLoading) {
                    startTranslatingAnimation()
                } else {
                    translateButton.text = "Translate" // Reset when translation is done
                    translateButton.isEnabled = true
                }
            }
        }
    }

    private fun startTranslatingAnimation() {
        lifecycleScope.launch {
            var dotCount = 0
            while (viewModel.loading.value) {
                val dots = ".".repeat(dotCount % 4) // Cycles: "", ".", "..", "..."
                translateButton.text = "Translating$dots"
                dotCount++
                kotlinx.coroutines.delay(500) // Change every 0.5 seconds
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    override fun onDestroy() {
        textToSpeechHelper.shutdown()
        super.onDestroy()
    }
}