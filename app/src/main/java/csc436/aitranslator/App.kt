package csc436.aitranslator

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.LocaleList
import java.util.Locale

class App : Application() {
    companion object {
        lateinit var instance: App
            private set

        private const val PREFS_NAME = "AppSettings"
        private const val KEY_LANGUAGE = "selectedLanguage"

        fun setAppLanguage(languageCode: String) {
            val sharedPreferences = instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(KEY_LANGUAGE, languageCode).apply()

            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val config = Configuration(instance.resources.configuration)
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))

            instance.resources.updateConfiguration(config, instance.resources.displayMetrics)
        }

        fun getSavedLanguage(): String {
            val sharedPreferences = instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(KEY_LANGUAGE, Locale.getDefault().language) ?: "en"
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
        setAppLanguage(getSavedLanguage()) // Apply saved language at startup
    }
}
