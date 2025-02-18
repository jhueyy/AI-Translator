package csc436.aitranslator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TextToSpeechHelper(context: Context) {
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.ENGLISH
                Log.d("TTS", "TextToSpeech initialized successfully.")
            } else {
                Log.e("TTS", "TextToSpeech initialization failed.")
                textToSpeech = null // Ensure safe failure
            }
        }
    }

    fun speak(text: String) {
        if (text.isNotEmpty() && textToSpeech != null) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("TTS", "Speaking: $text")
        }
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        Log.d("TTS", "TextToSpeech shut down.")
    }
}
