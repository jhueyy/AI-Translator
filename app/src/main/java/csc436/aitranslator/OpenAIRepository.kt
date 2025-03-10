package csc436.aitranslator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class OpenAIRepository {
    suspend fun translateText(text: String, languageCode: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("API_REQUEST", "Translating to: $languageCode | Text: $text")

                val systemMessage = "You are a translation assistant. Translate the following text into '$languageCode'. Only provide the translated text without any extra explanations or remarks."

                val response = RetrofitClient.api.translate(
                    TranslationRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            mapOf("role" to "system", "content" to systemMessage),
                            mapOf("role" to "user", "content" to text)
                        )
                    )
                )

                Log.d("API_RESPONSE", "Response: ${response.choices}")

                if (response.choices.isNotEmpty()) {
                    response.choices.first().message["content"] ?:
                    getString(R.string.translation_failed)
                } else {
                    Log.e("API", "Empty response from server.")
                    getString(R.string.translation_failed)
                }
            } catch (e: HttpException) {
                Log.e("API", "HTTP Error: ${e.code()} ${e.message()}", e)
                getString(R.string.translation_failed)
            } catch (e: Exception) {
                Log.e("API", "Translation error: ${e.message}", e)
                getString(R.string.translation_failed)
            }
        }
    }

    suspend fun detectLanguage(text: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("API_REQUEST", "Detecting language for: $text")

                val systemMessage = "You are a language detection assistant. Identify the language of the following text and return its ISO 639-1 code only (e.g., 'en' for English, 'es' for Spanish)."

                val response = RetrofitClient.api.translate(
                    TranslationRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            mapOf("role" to "system", "content" to systemMessage),
                            mapOf("role" to "user", "content" to text)
                        )
                    )
                )

                Log.d("API_RESPONSE", "Detected Language: ${response.choices}")

                if (response.choices.isNotEmpty()) {
                    response.choices.first().message["content"]
                } else {
                    Log.e("API", "Empty response from server.")
                    null
                }
            } catch (e: Exception) {
                Log.e("API", "Language detection error: ${e.message}", e)
                null
            }
        }
    }

    private fun getString(resId: Int, vararg formatArgs: Any?): String {
        return App.instance.getString(resId, *formatArgs)
    }
}
