package csc436.aitranslator

import TextToSpeechHelper
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
    private lateinit var micButton: ImageButton
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var textToSpeechHelper: TextToSpeechHelper
    private lateinit var targetLanguageSpinner: Spinner

    private var selectedLanguageCode = "en" // Default to English

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(OpenAIRepository()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        translateButton = findViewById(R.id.translateButton)
        speakerButton = findViewById(R.id.speakerButton)
        micButton = findViewById(R.id.micButton)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        targetLanguageSpinner = findViewById(R.id.targetLanguageSpinner)

        textToSpeechHelper = TextToSpeechHelper(this)


        val copyButton: ImageButton = findViewById(R.id.copyButton)


        inputText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputText.hint = ""
            } else if (inputText.text.isEmpty()) {
                inputText.hint = "Enter text to translate..."
            }
        }

        findViewById<View>(R.id.rootLayout).setOnTouchListener { view, _ ->
            inputText.clearFocus()
            hideKeyboard()
            view.performClick()
            false
        }






        copyButton.setOnClickListener {
            val textToCopy = outputText.text.toString().trim()

            // Prevent copying if the text is still the default placeholder
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



        val languages = resources.getStringArray(R.array.languages)
        val languageCodes = resources.getStringArray(R.array.language_codes)

        val adapter = ArrayAdapter(this, R.layout.spinner_item, languages)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item) // Use a different layout for dropdown
        targetLanguageSpinner.adapter = adapter


        targetLanguageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLanguageCode = languageCodes[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

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

        speakerButton.setOnClickListener {
            textToSpeechHelper.speak(outputText.text.toString())
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
