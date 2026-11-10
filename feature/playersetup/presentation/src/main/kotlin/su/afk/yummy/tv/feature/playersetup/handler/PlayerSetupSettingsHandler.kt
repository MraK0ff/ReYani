package su.afk.yummy.tv.feature.playersetup.handler

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import su.afk.yummy.tv.core.model.settings.AppInterfaceLanguage
import su.afk.yummy.tv.core.model.settings.AppTheme
import su.afk.yummy.tv.core.model.settings.BackgroundStyle
import su.afk.yummy.tv.core.model.settings.PlayerOrientationMode
import su.afk.yummy.tv.core.model.settings.PreferredVideoQuality
import su.afk.yummy.tv.core.model.settings.YaniContentLanguage
import su.afk.yummy.tv.core.preferences.api.YaniAccountSettingsStore
import su.afk.yummy.tv.core.preferences.settings.PlayerSettingsStore
import su.afk.yummy.tv.core.preferences.settings.AppearanceSettingsStore
import su.afk.yummy.tv.core.preferences.interface_language.AppInterfaceLanguagePreferences
import su.afk.yummy.tv.feature.playersetup.PlayerSetupState
import javax.inject.Inject

/** Читает и записывает настройки плеера, которые предлагаются на первичной настройке. */
internal class PlayerSetupSettingsHandler @Inject constructor(
    private val store: PlayerSettingsStore,
    private val accountStore: YaniAccountSettingsStore,
    private val appearanceStore: AppearanceSettingsStore,
    private val interfaceLanguagePreferences: AppInterfaceLanguagePreferences,
) {

    fun selectedInterfaceLanguage(): AppInterfaceLanguage = interfaceLanguagePreferences.selectedLanguage

    fun initialContentLanguage(): YaniContentLanguage = accountStore.resolveSystemYaniContentLanguage()

    fun observe(): Flow<PlayerSetupState.State> = combine(
        combine(
            store.showOpeningOnTimeline,
            store.autoSkipOpeningsEndings,
            store.autoPlayNextEpisode,
            store.suggestNextEpisodeOnWatched,
        ) { showOpening, autoSkip, autoPlay, suggestNext ->
            PlayerSetupState.State(
                showOpeningOnTimeline = showOpening,
                autoSkipOpeningsEndings = autoSkip,
                autoPlayNextEpisode = autoPlay,
                suggestNextEpisodeOnWatched = suggestNext,
            )
        },
        combine(
            combine(
                store.adaptiveVideoQualityEnabled,
                store.preferredVideoQuality,
                store.playerOrientationMode,
                store.pictureInPictureEnabled,
                accountStore.yaniContentLanguage,
            ) { adaptive, quality, orientation, pip, language ->
                PlayerSetupState.State(
                    adaptiveVideoQualityEnabled = adaptive,
                    preferredVideoQuality = quality,
                    playerOrientationMode = orientation,
                    pictureInPictureEnabled = pip,
                    contentLanguage = language,
                )
            },
            combine(appearanceStore.appTheme, appearanceStore.backgroundStyle) { theme, background ->
                PlayerSetupState.State(appTheme = theme, backgroundStyle = background)
            },
        ) { playback, appearance ->
            playback.copy(
                interfaceLanguage = interfaceLanguagePreferences.selectedLanguage,
                appTheme = appearance.appTheme,
                backgroundStyle = appearance.backgroundStyle,
            )
        },
    ) { toggles, playback ->
        toggles.copy(
            adaptiveVideoQualityEnabled = playback.adaptiveVideoQualityEnabled,
            preferredVideoQuality = playback.preferredVideoQuality,
            playerOrientationMode = playback.playerOrientationMode,
            pictureInPictureEnabled = playback.pictureInPictureEnabled,
            contentLanguage = playback.contentLanguage,
            interfaceLanguage = playback.interfaceLanguage,
            appTheme = playback.appTheme,
            backgroundStyle = playback.backgroundStyle,
        )
    }

    suspend fun setShowOpeningOnTimeline(enabled: Boolean) = store.setShowOpeningOnTimeline(enabled)

    suspend fun setAutoSkipOpeningsEndings(enabled: Boolean) = store.setAutoSkipOpeningsEndings(enabled)

    suspend fun setAutoPlayNextEpisode(enabled: Boolean) = store.setAutoPlayNextEpisode(enabled)

    suspend fun setSuggestNextEpisodeOnWatched(enabled: Boolean) =
        store.setSuggestNextEpisodeOnWatched(enabled)


    suspend fun setPictureInPictureEnabled(enabled: Boolean) = store.setPictureInPictureEnabled(enabled)

    fun setInterfaceLanguage(language: AppInterfaceLanguage) = interfaceLanguagePreferences.select(language)

    suspend fun setAppTheme(theme: AppTheme) = appearanceStore.setAppTheme(theme)

    suspend fun setBackgroundStyle(style: BackgroundStyle) = appearanceStore.setBackgroundStyle(style)

    suspend fun setContentLanguage(language: YaniContentLanguage) =
        accountStore.setYaniContentLanguage(language)

    suspend fun setAdaptiveVideoQualityEnabled(enabled: Boolean) =
        store.setAdaptiveVideoQualityEnabled(enabled)

    suspend fun setPreferredVideoQuality(quality: PreferredVideoQuality) =
        store.setPreferredVideoQuality(quality)

    suspend fun setPlayerOrientationMode(mode: PlayerOrientationMode) =
        store.setPlayerOrientationMode(mode)
}
