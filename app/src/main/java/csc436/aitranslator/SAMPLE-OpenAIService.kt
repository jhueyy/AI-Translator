package csc436.aitranslator

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface `SAMPLE-OpenAIService` {
    // 1. Delete SAMPLE- from the filename
    // 2. Replace KEY GOES HERE in Authorization with your actual OpenAI API key
    // the key will start with
    // Bearer sk-proj-...
    @Headers(
        "Authorization: KEY GOES HERE",
        "Content-Type: application/json"
    )
    @POST("chat/completions")
    suspend fun translate(@Body request: TranslationRequest): OpenAIResponse
}
