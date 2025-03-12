package csc436.aitranslator

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import csc436.aitranslator.App.Companion.setAppLanguage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

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
    private lateinit var cameraButton: ImageButton
    private lateinit var inputSpeakerButton: ImageButton

    private var selectedLanguageCode = Locale.getDefault().language // Uses system language
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(OpenAIRepository()) }

    private val languagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val selectedLanguage = data?.getStringExtra("selectedLanguage")
                val selectedCode = data?.getStringExtra("selectedCode")

                if (selectedLanguage != null && selectedCode != null) {
                    languageButton.text = selectedLanguage
                    selectedLanguageCode = selectedCode
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val extractedText = result.data?.getStringExtra("extracted_text")
                if (!extractedText.isNullOrEmpty()) {
                    inputText.setText(extractedText)
                    translateButton.performClick()
                }
            }
        }




    override fun onCreate(savedInstanceState: Bundle?) {
        /* TEST SYSTEM LANGUAGE: change language code to test
        * need to look at strings.xml and place a language code in
        * don't forget to rebuild the app. if just ran, will not change everything*/
//        setAppLanguage("en")


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        inputText = findViewById(R.id.inputText)
        outputText = findViewById(R.id.outputText)
        translateButton = findViewById(R.id.translateButton)
        speakerButton = findViewById(R.id.speakerButton)
        copyButton = findViewById(R.id.copyButton)
        loadingAnimation = findViewById(R.id.loadingAnimation)
        languageButton = findViewById(R.id.targetLanguageButton)
        chatButton = findViewById(R.id.chatButton)
        settingsButton = findViewById(R.id.settingsButton)
        inputSpeakerButton = findViewById(R.id.inputSpeakerButton)
        cameraButton = findViewById(R.id.cameraButton)
        textToSpeechHelper = TextToSpeechHelper(this)

        languageButton.setOnClickListener {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            languagePickerLauncher.launch(intent)
        }

        chatButton.setOnClickListener {
            startActivity(Intent(this, LiveChatActivity::class.java))
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        cameraButton.setOnClickListener {
            cameraLauncher.launch(Intent(this, CameraActivity::class.java))
        }

        inputText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputText.hint = ""
            } else if (inputText.text.isEmpty()) {
                inputText.hint = getString(R.string.enter_text_hint)
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
            if (textToCopy.isNotEmpty() && textToCopy != getString(R.string.translation_placeholder)) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(R.string.copied_text), textToCopy)
                clipboard.setPrimaryClip(clip)

                copyButton.setImageResource(R.drawable.ic_check)
                copyButton.postDelayed({ copyButton.setImageResource(R.drawable.ic_copy) }, 1500)
            }
        }

        translateButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    translateButton.isEnabled = false
                    startTranslatingAnimation()
                    viewModel.translateText(text, selectedLanguageCode)
                } else {
                    showToast(getString(R.string.no_internet))
                }
            } else {
                showToast(getString(R.string.enter_text_to_translate))
            }
        }

        speakerButton.setOnClickListener {
            val text = outputText.text.toString().trim()
            if (text.isNotEmpty() && text != getString(R.string.translation_placeholder)) {
                textToSpeechHelper.speak(text)
            }
        }

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
                translateButton.text = getString(R.string.translate)
                translateButton.isEnabled = true
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                if (isLoading) {
                    startTranslatingAnimation()
                } else {
                    translateButton.text = getString(R.string.translate)
                    translateButton.isEnabled = true
                }
            }
        }
    }

    private fun startTranslatingAnimation() {
        lifecycleScope.launch {
            var dotCount = 0
            while (viewModel.loading.value) {
                val dots = ".".repeat(dotCount % 4)
                translateButton.text = getString(R.string.translating) + dots
                dotCount++
                kotlinx.coroutines.delay(500)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(App.applyLanguageToContext(newBase))
    }


    override fun onDestroy() {
        textToSpeechHelper.shutdown()
        super.onDestroy()
    }
}
