package su.afk.yummy.tv.feature.playersetup

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import su.afk.yummy.tv.core.error.api.ErrorHandler
import su.afk.yummy.tv.core.error.api.RetryStorage
import su.afk.yummy.tv.core.mvi.BaseViewModel
import su.afk.yummy.tv.core.navigation.manager.INavigationManager
import su.afk.yummy.tv.feature.playersetup.handler.PlayerSetupSettingsHandler
import su.afk.yummy.tv.feature.settings.ISettingsNavigator
import javax.inject.Inject

@HiltViewModel
class PlayerSetupViewModel @Inject internal constructor(
    override val errorHandler: ErrorHandler,
    override val retryStorage: RetryStorage,
    private val nav: INavigationManager,
    private val settingsHandler: PlayerSetupSettingsHandler,
    private val settingsNavigator: ISettingsNavigator,
) : BaseViewModel<PlayerSetupState.State, PlayerSetupState.Event, PlayerSetupState.Effect>() {

    override fun createInitialState() = PlayerSetupState.State(
        contentLanguage = settingsHandler.initialContentLanguage(),
    )

    init {
        // Сразу подставляем язык устройства/сохранённый язык до первой отрисовки мастера.
        setState { copy(interfaceLanguage = settingsHandler.selectedInterfaceLanguage()) }
        settingsHandler.observe()
            .onEach { settings -> setState { settings } }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: PlayerSetupState.Event) {
        when (event) {
            PlayerSetupState.Event.ShowOpeningOnTimelineToggled -> launchWrite {
                settingsHandler.setShowOpeningOnTimeline(!currentState.showOpeningOnTimeline)
            }

            PlayerSetupState.Event.AutoSkipOpeningsEndingsToggled -> launchWrite {
                settingsHandler.setAutoSkipOpeningsEndings(!currentState.autoSkipOpeningsEndings)
            }

            PlayerSetupState.Event.AutoPlayNextEpisodeToggled -> launchWrite {
                settingsHandler.setAutoPlayNextEpisode(!currentState.autoPlayNextEpisode)
            }

            PlayerSetupState.Event.SuggestNextEpisodeOnWatchedToggled -> launchWrite {
                settingsHandler.setSuggestNextEpisodeOnWatched(!currentState.suggestNextEpisodeOnWatched)
            }

            PlayerSetupState.Event.PictureInPictureToggled -> launchWrite {
                settingsHandler.setPictureInPictureEnabled(!currentState.pictureInPictureEnabled)
            }

            PlayerSetupState.Event.AutoBrightnessToggled -> launchWrite {
                settingsHandler.setAutoBrightnessEnabled(!currentState.autoBrightnessEnabled)
            }

            PlayerSetupState.Event.AdaptiveVideoQualityToggled -> launchWrite {
                settingsHandler.setAdaptiveVideoQualityEnabled(!currentState.adaptiveVideoQualityEnabled)
            }

            is PlayerSetupState.Event.InterfaceLanguageSelected -> {
                settingsHandler.setInterfaceLanguage(event.language)
                setState { copy(interfaceLanguage = event.language) }
            }
            is PlayerSetupState.Event.AppThemeSelected -> launchWrite { settingsHandler.setAppTheme(event.theme) }
            is PlayerSetupState.Event.BackgroundStyleSelected -> launchWrite { settingsHandler.setBackgroundStyle(event.style) }
            is PlayerSetupState.Event.ContentLanguageSelected -> launchWrite {
                settingsHandler.setContentLanguage(event.language)
            }

            is PlayerSetupState.Event.PreferredVideoQualitySelected -> launchWrite {
                settingsHandler.setPreferredVideoQuality(event.quality)
            }

            is PlayerSetupState.Event.PlayerOrientationModeSelected -> launchWrite {
                settingsHandler.setPlayerOrientationMode(event.mode)
            }

            PlayerSetupState.Event.DoneSelected -> nav.back()
            PlayerSetupState.Event.MoreSettingsSelected -> {
                nav.back()
                nav.navigate(settingsNavigator.getSettingsDest())
            }
        }
    }

    private fun launchWrite(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
