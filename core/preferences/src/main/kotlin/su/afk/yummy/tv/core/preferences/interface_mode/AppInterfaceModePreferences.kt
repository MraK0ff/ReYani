package su.afk.yummy.tv.core.preferences.interface_mode

/** Хранит только факт показа первичной настройки плеера. */
interface AppInterfaceModePreferences {
    fun consumePlayerSetupPending(): Boolean
}
