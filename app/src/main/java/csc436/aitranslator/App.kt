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

        /*
         TESTING METHOD:
         edit the debugLanguage : String? = null
         change null to be the language code we want to test for example spanish

         */
        fun getSavedLanguage(): String {
            // Debugging override: Set this to force a language for testing
            val debugLanguage: String? = null //"es // Change this to "es", "fr", etc., to test

            if (debugLanguage != null && supportedLanguages.contains(debugLanguage)) {
                return debugLanguage // Force the app language for testing
            }

            // Otherwise, use the system language
            val systemLanguage = Locale.getDefault().language
            return if (supportedLanguages.contains(systemLanguage)) {
                systemLanguage
            } else {
                "en" // Default to English if system language isn't supported
            }
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

        val languageToApply = getSavedLanguage()
        setAppLanguage(languageToApply)
    }
}
