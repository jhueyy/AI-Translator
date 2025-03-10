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
import java.util.Locale

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var languageListView: ListView
    private lateinit var closeButton: ImageButton

    private val languageCodes = listOf(
        "af", "sq", "ar", "hy", "bn", "bg", "my", "ca", "zh", "hr", "cs", "da", "nl",
        "en", "et", "tl", "fi", "fr", "ka", "de", "el", "gu", "he", "hi", "hu", "is",
        "id", "it", "ja", "kn", "kk", "km", "ko", "lo", "lv", "lt", "ms", "ml", "mr",
        "mn", "ne", "no", "ps", "fa", "pl", "pt", "pa", "ro", "ru", "sr", "si", "sk",
        "sl", "es", "sw", "sv", "ta", "te", "th", "tr", "uk", "ur", "uz", "vi", "cy"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)
        supportActionBar?.hide()

        searchBar = findViewById(R.id.searchBar)
        languageListView = findViewById(R.id.languageListView)
        closeButton = findViewById(R.id.closeButton)

        val systemLocale = Locale.getDefault() // Get the system's language
        val sortedPairs = languageCodes.map { code ->
            code to Locale(code).getDisplayLanguage(systemLocale).replaceFirstChar { it.uppercaseChar() }
        }.sortedBy { it.second }

        val sortedLanguageCodes = sortedPairs.map { it.first }
        val sortedLanguages = sortedPairs.map { it.second }

        val adapter = LanguageAdapter(this, sortedLanguages)
        languageListView.adapter = adapter

        closeButton.setOnClickListener { finish() }

        languageListView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = adapter.getItem(position)
            val selectedCode = sortedLanguageCodes[position] // ✅ Now it's properly aligned

            val resultIntent = Intent()
            resultIntent.putExtra("selectedLanguage", selectedLanguage)
            resultIntent.putExtra("selectedCode", selectedCode)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }


        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
        })
    }
}
