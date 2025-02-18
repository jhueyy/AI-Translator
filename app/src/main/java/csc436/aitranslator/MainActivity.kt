package csc436.aitranslator

import android.os.Bundle
import android.view.View
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

        textToSpeechHelper = TextToSpeechHelper(this)

        translateButton.setOnClickListener {
            val text = inputText.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.translateText(text)
            } else {
                Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
            }
        }

        speakerButton.setOnClickListener {
            textToSpeechHelper.speak(outputText.text.toString())
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.translation.collectLatest { translatedText ->
                outputText.text = translatedText
            }
        }

        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                loadingAnimation.visibility = if (isLoading) View.VISIBLE else View.GONE
                if (isLoading) loadingAnimation.playAnimation() else loadingAnimation.cancelAnimation()
            }
        }
    }

    override fun onDestroy() {
        textToSpeechHelper.shutdown()
        super.onDestroy()
    }
}
