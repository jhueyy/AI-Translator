package csc436.aitranslator

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Map<String, String>
)
