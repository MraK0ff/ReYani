package su.afk.yummy.tv.core.preferences.interface_language

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import su.afk.yummy.tv.core.model.settings.AppInterfaceLanguage
import java.util.Locale

/** Контекст локали интерфейса. Вызывается из attachBaseContext до инициализации Hilt. */
object AppInterfaceLanguageContext {
    internal const val PREFERENCES_NAME = "app_interface_language"
    internal const val LANGUAGE_KEY = "language"

    private val mutableLanguageChanges =
        MutableSharedFlow<AppInterfaceLanguage>(extraBufferCapacity = 1)
    val languageChanges: SharedFlow<AppInterfaceLanguage> = mutableLanguageChanges.asSharedFlow()

    internal fun notifyLanguageChanged(language: AppInterfaceLanguage) {
        mutableLanguageChanges.tryEmit(language)
    }

    internal fun resolveSystemLanguage(context: Context): AppInterfaceLanguage {
        val languageCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)?.language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale?.language
        }
        return AppInterfaceLanguage.entries.firstOrNull {
            it.languageTag.equals(languageCode, ignoreCase = true)
        } ?: AppInterfaceLanguage.DEFAULT
    }

    internal fun read(context: Context): AppInterfaceLanguage {
        val stored = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, null)
        return if (stored.isNullOrBlank() || stored == "SYSTEM") {
            resolveSystemLanguage(context)
        } else {
            AppInterfaceLanguage.fromPreferenceValue(stored)
        }
    }

    fun wrap(base: Context): Context {
        val locale = Locale.forLanguageTag(read(base).languageTag)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocales(LocaleList(locale))
        return base.createConfigurationContext(configuration)
    }
}
