package csc436.aitranslator

data class TranslationRequest(
    val model: String,  // Specifies the AI model (e.g., "gpt-3.5-turbo")
    val messages: List<Map<String, String>>  // Contains user and system messages
)
