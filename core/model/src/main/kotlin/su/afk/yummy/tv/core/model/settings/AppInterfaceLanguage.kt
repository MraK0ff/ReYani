package su.afk.yummy.tv.core.model.settings

/** Язык интерфейса приложения. Не путать с [YaniContentLanguage] — языком контента из API. */
enum class AppInterfaceLanguage(val languageTag: String) {
    RUSSIAN("ru"),
    ENGLISH("en"),
    UKRAINIAN("uk"),
    ;

    companion object {
        val DEFAULT: AppInterfaceLanguage = RUSSIAN
        fun fromPreferenceValue(value: String?): AppInterfaceLanguage =
            entries.firstOrNull { it.name == value } ?: DEFAULT
    }
}
