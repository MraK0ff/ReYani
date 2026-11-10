package su.afk.yummy.tv.core.preferences.interface_language

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import su.afk.yummy.tv.core.model.settings.AppInterfaceLanguage
import javax.inject.Inject

internal class SharedPreferencesAppInterfaceLanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppInterfaceLanguagePreferences {

    override val selectedLanguage: AppInterfaceLanguage
        get() {
            val preferences = context.getSharedPreferences(
                AppInterfaceLanguageContext.PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            )
            val stored = preferences.getString(AppInterfaceLanguageContext.LANGUAGE_KEY, null)
            val language = if (stored.isNullOrBlank() || stored == "SYSTEM") {
                AppInterfaceLanguageContext.resolveSystemLanguage(context)
            } else {
                AppInterfaceLanguage.fromPreferenceValue(stored)
            }
            // При первом запуске и миграции старого SYSTEM сохраняем конкретный язык устройства.
            if (stored != language.name) {
                preferences.edit()
                    .putString(AppInterfaceLanguageContext.LANGUAGE_KEY, language.name)
                    .commit()
            }
            return language
        }

    override fun select(language: AppInterfaceLanguage) {
        val preferences = context.getSharedPreferences(
            AppInterfaceLanguageContext.PREFERENCES_NAME,
            Context.MODE_PRIVATE,
        )
        if (selectedLanguage == language) return
        // Запись завершается до события, которое обновит локаль Activity.
        preferences.edit()
            .putString(AppInterfaceLanguageContext.LANGUAGE_KEY, language.name)
            .commit()
        AppInterfaceLanguageContext.notifyLanguageChanged(language)
    }
}
