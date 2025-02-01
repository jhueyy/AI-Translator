package csc436.aitranslator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class OpenAIRepository {

    fun translateText(text: String, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = TranslationRequest(
                    model = "gpt-3.5-turbo",
                    messages = listOf(
                        mapOf("role" to "system", "content" to "Translate this text to English."),
                        mapOf("role" to "user", "content" to text)
                    )
                )


                val response = RetrofitClient.api.translate(request)

                val translatedText = response.choices.getOrNull(0)?.message?.get("content") ?: "Translation failed."

                callback(translatedText) // Send result back to UI thread

            } catch (e: HttpException) {
                callback("Error: ${e.message}")
            } catch (e: Exception) {
                callback("Translation failed.")
            }
        }
    }
}
