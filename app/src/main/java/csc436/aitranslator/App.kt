package csc436.aitranslator

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import java.util.*

class App : Application() {

    companion object {
        lateinit var instance: App
            private set

        private const val PREFS_NAME = "AppSettings"
        private const val KEY_LANGUAGE = "selectedLanguage"

        val supportedLanguages = setOf(
            "en", "es", "fr", "de", "it", "ru", "ja", "ko", "zh", "ar", "hi",
            "pt", "nl", "sv", "da", "no", "fi", "pl", "cs", "tr", "el"
        )

        fun setAppLanguage(languageCode: String) {
            val sharedPreferences = instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply()

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(instance.resources.configuration)
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))

            instance.applicationContext.createConfigurationContext(config)

        }

        fun getSavedLanguage(): String {
            val sharedPreferences = instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedLanguage = sharedPreferences.getString(KEY_LANGUAGE, Locale.getDefault().language) ?: "en"

            return if (supportedLanguages.contains(savedLanguage)) savedLanguage else "en"
        }

        fun applyLanguageToContext(context: Context): Context {
            val locale = Locale(getSavedLanguage())
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))

            return context.createConfigurationContext(config)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Ensure only supported languages are applied
        val languageToApply = getSavedLanguage()
        setAppLanguage(languageToApply)
        // to test different languages, enter the language code
        // if language code is not in the list/xml, then we default to english
    }
}
