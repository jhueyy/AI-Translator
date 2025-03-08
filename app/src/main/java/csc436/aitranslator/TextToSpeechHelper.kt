package csc436.aitranslator

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TextToSpeechHelper(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private val sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    private var speechRate: Float = sharedPreferences.getFloat("speechRate", 1.0f)
    private var speechPitch: Float = sharedPreferences.getFloat("speechPitch", 1.0f)

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.ENGLISH
                textToSpeech?.setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                applySpeechSettings() // Load saved rate & pitch
                Log.d("TTS", "TextToSpeech initialized successfully.")
            } else {
                Log.e("TTS", "TextToSpeech initialization failed.")
                textToSpeech = null
            }
        }
    }

    fun speak(text: String) {
        if (text.isNotEmpty() && textToSpeech != null) {
            applySpeechSettings() // Ensure latest settings are applied before speaking
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            Log.d("TTS", "Speaking: $text at rate $speechRate and pitch $speechPitch")
        }
    }

    private fun applySpeechSettings() {
        speechRate = sharedPreferences.getFloat("speechRate", 1.0f)
        speechPitch = sharedPreferences.getFloat("speechPitch", 1.0f)
        textToSpeech?.setSpeechRate(speechRate)
        textToSpeech?.setPitch(speechPitch)
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        Log.d("TTS", "TextToSpeech shut down.")
    }
}