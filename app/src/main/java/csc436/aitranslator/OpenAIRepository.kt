package csc436.aitranslator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class OpenAIRepository {
    suspend fun translateText(text: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.translate(
                    TranslationRequest(
                        model = "gpt-4o-mini",
                        messages = listOf(
                            mapOf("role" to "system", "content" to "Translate this text."),
                            mapOf("role" to "user", "content" to text)
                        )
                    )
                )

                if (response.choices.isNotEmpty()) {
                    response.choices.first().message["content"] ?: "Translation failed."
                } else {
                    "No response from server."
                }
            } catch (e: HttpException) {
                Log.e("API", "HTTP Error: ${e.code()} ${e.message()}")
                "Translation error: ${e.message()}"
            } catch (e: Exception) {
                Log.e("API", "Translation error: ${e.message}")
                "Translation failed due to network issues."
            }
        }
    }
}
