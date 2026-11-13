package su.afk.yummy.tv.core.preferences.interface_mode

/** Synchronous preferences read before the application router selects an Activity. */
interface AppInterfaceModePreferences {
    val selectedMode: AppInterfaceMode?
    fun select(mode: AppInterfaceMode)
    fun consumePlayerSetupPending(): Boolean
}
