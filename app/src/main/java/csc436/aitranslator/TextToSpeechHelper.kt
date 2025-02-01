package csc436.aitranslator

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TextToSpeechHelper(context: Context) {
    private var textToSpeech: TextToSpeech? = null  // Make it nullable to prevent issues

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.ENGLISH
            }
        }
    }

    fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
