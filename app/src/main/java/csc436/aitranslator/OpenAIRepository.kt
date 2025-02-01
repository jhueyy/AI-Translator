package csc436.aitranslator

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAIRepository {
    suspend fun translateText(text: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.translate(
                    TranslationRequest(
                        model = "gpt-3.5-turbo",
                        messages = listOf(
                            mapOf("role" to "system", "content" to "Translate this text."),
                            mapOf("role" to "user", "content" to text)
                        )
                    )
                )

                response.choices.getOrNull(0)?.message?.get("content") ?: "Translation failed."
            } catch (e: Exception) {
                Log.e("API", "Translation error: ${e.message}")
                "Translation failed."
            }
        }
    }
}
