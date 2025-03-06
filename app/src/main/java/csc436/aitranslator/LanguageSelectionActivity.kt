package csc436.aitranslator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var languageListView: ListView
    private lateinit var closeButton: ImageButton

    private val languages = listOf( // Expanded List of Languages
        "Afrikaans", "Albanian", "Arabic", "Armenian", "Bengali", "Bulgarian", "Burmese", "Catalan",
        "Chinese", "Croatian", "Czech", "Danish", "Dutch", "English", "Estonian", "Filipino", "Finnish",
        "French", "Georgian", "German", "Greek", "Gujarati", "Hebrew", "Hindi", "Hungarian",
        "Icelandic", "Indonesian", "Italian", "Japanese", "Kannada", "Kazakh", "Khmer", "Korean",
        "Lao", "Latvian", "Lithuanian", "Malay", "Malayalam", "Marathi", "Mongolian", "Nepali",
        "Norwegian", "Pashto", "Persian", "Polish", "Portuguese", "Punjabi", "Romanian", "Russian",
        "Serbian", "Sinhala", "Slovak", "Slovenian", "Spanish", "Swahili", "Swedish", "Tamil",
        "Telugu", "Thai", "Turkish", "Ukrainian", "Urdu", "Uzbek", "Vietnamese", "Welsh"
    )

    private val languageCodes = listOf( // Matching Language Codes (ISO 639-1)
        "af", "sq", "ar", "hy", "bn", "bg", "my", "ca",
        "zh", "hr", "cs", "da", "nl", "en", "et", "tl", "fi",
        "fr", "ka", "de", "el", "gu", "he", "hi", "hu",
        "is", "id", "it", "ja", "kn", "kk", "km", "ko",
        "lo", "lv", "lt", "ms", "ml", "mr", "mn", "ne",
        "no", "ps", "fa", "pl", "pt", "pa", "ro", "ru",
        "sr", "si", "sk", "sl", "es", "sw", "sv", "ta",
        "te", "th", "tr", "uk", "ur", "uz", "vi", "cy"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)
        supportActionBar?.hide()

        searchBar = findViewById(R.id.searchBar)
        languageListView = findViewById(R.id.languageListView)
        closeButton = findViewById(R.id.closeButton)

        val adapter = LanguageAdapter(this, languages)
        languageListView.adapter = adapter

        // Close button action
        closeButton.setOnClickListener {
            finish()
        }

        // Handle language selection
        languageListView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = languages[position]
            val selectedCode = languageCodes[position]

            val resultIntent = Intent()
            resultIntent.putExtra("selectedLanguage", selectedLanguage)
            resultIntent.putExtra("selectedCode", selectedCode)
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Close activity after selection
        }

        // Search functionality
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
        })
    }
}
