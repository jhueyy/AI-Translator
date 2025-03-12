package csc436.aitranslator

import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.UnknownHostException

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
                    when (val content = response.choices.first().message.content) {
                        is String -> content
                        is List<*> -> content.joinToString(" ")  // Convert array to a single string
                        else -> getString(R.string.translation_failed)
                    }
                } else {
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
                    return@withContext when (val content = response.choices.first().message.content) {
                        is String -> content
                        is List<*> -> content.firstOrNull()?.toString()
                        else -> null
                    }
                } else {
                    Log.e("API", "Empty response from server.")
                    return@withContext null
                }


            } catch (e: UnknownHostException) {
                Log.e("API", "No internet connection.", e)
                null
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
