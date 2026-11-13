package su.afk.yummy.tv.feature.playersetup

import androidx.compose.runtime.Immutable
import su.afk.yummy.tv.core.model.settings.AppInterfaceLanguage
import su.afk.yummy.tv.core.model.settings.AppTheme
import su.afk.yummy.tv.core.model.settings.BackgroundStyle
import su.afk.yummy.tv.core.model.settings.PlayerOrientationMode
import su.afk.yummy.tv.core.model.settings.PreferredVideoQuality
import su.afk.yummy.tv.core.model.settings.YaniContentLanguage
import su.afk.yummy.tv.core.mvi.UiEffect
import su.afk.yummy.tv.core.mvi.UiEvent
import su.afk.yummy.tv.core.mvi.UiState

class PlayerSetupState {

    @Immutable
    data class State(
        val showOpeningOnTimeline: Boolean = false,
        val autoSkipOpeningsEndings: Boolean = true,
        val autoPlayNextEpisode: Boolean = true,
        val suggestNextEpisodeOnWatched: Boolean = true,
        val adaptiveVideoQualityEnabled: Boolean = true,
        val preferredVideoQuality: PreferredVideoQuality = PreferredVideoQuality.BEST,
        val playerOrientationMode: PlayerOrientationMode = PlayerOrientationMode.SENSOR_LANDSCAPE,
        val pictureInPictureEnabled: Boolean = true,
        val autoBrightnessEnabled: Boolean = true,
        val contentLanguage: YaniContentLanguage = YaniContentLanguage.DEFAULT,
        val interfaceLanguage: AppInterfaceLanguage = AppInterfaceLanguage.DEFAULT,
        val appTheme: AppTheme = AppTheme.GRAPHITE,
        val backgroundStyle: BackgroundStyle = BackgroundStyle.AMOLED,
    ) : UiState

    sealed interface Event : UiEvent {
        data object ShowOpeningOnTimelineToggled : Event
        data object AutoSkipOpeningsEndingsToggled : Event
        data object AutoPlayNextEpisodeToggled : Event
        data object SuggestNextEpisodeOnWatchedToggled : Event
        data object PictureInPictureToggled : Event
        data object AutoBrightnessToggled : Event
        data object AdaptiveVideoQualityToggled : Event
        data class ContentLanguageSelected(val language: YaniContentLanguage) : Event
        data class InterfaceLanguageSelected(val language: AppInterfaceLanguage) : Event
        data class AppThemeSelected(val theme: AppTheme) : Event
        data class BackgroundStyleSelected(val style: BackgroundStyle) : Event
        data class PreferredVideoQualitySelected(val quality: PreferredVideoQuality) : Event
        data class PlayerOrientationModeSelected(val mode: PlayerOrientationMode) : Event
        data object DoneSelected : Event

        /** Пользователь выбрал «Остальное в настройках» — закрываем экран и открываем настройки. */
        data object MoreSettingsSelected : Event
    }

    sealed interface Effect : UiEffect
}
