package csc436.aitranslator

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val role: String,
    val content: Any  // Handle both String and List<String>
)
