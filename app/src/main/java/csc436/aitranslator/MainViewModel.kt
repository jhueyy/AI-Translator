package csc436.aitranslator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: OpenAIRepository) : ViewModel() {

    private val _translation = MutableStateFlow<String>(R.string.translation_placeholder.toString())
    val translation: StateFlow<String> = _translation

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading: StateFlow<Boolean> = _loading

    fun translateText(text: String, languageCode: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                _translation.value = repository.translateText(text, languageCode)
            } catch (e: Exception) {
                _translation.value = R.string.translation_failed.toString()
            } finally {
                _loading.value = false
            }
        }
    }
}
