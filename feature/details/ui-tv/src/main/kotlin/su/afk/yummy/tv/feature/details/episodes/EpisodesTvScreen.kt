package su.afk.yummy.tv.feature.details.episodes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import su.afk.yummy.tv.core.designsystem.preview.ScreenPreviewTheme
import su.afk.yummy.tv.feature.details.episodes.view.EpisodeActionsTvDialog
import su.afk.yummy.tv.feature.details.episodes.view.EpisodeDubbingPickerOverlay
import su.afk.yummy.tv.feature.details.episodes.view.EpisodeWatchFilterTvAction
import su.afk.yummy.tv.feature.details.episodes.view.EpisodesSection
import su.afk.yummy.tv.feature.details.view.common.BalancerPickerOverlay

@Preview(
    name = "Default",
    device = "spec:width=1920dp,height=1080dp,dpi=160",
    uiMode = android.content.res.Configuration.UI_MODE_TYPE_TELEVISION,
    showBackground = true
)
@Composable
private fun EpisodesTvScreenDefaultPreview() = ScreenPreviewTheme {
    EpisodesTvScreen(EpisodesState.State(), emptyFlow()) {}
}

@Composable
fun EpisodesTvScreen(
    state: EpisodesState.State,
    effect: Flow<EpisodesState.Effect>,
    onEvent: (EpisodesState.Event) -> Unit,
) {
    var restoreEpisodesFocusRequest by remember { mutableIntStateOf(0) }
    var anchorEpisode by remember { mutableStateOf<String?>(null) }
    fun dismissBalancerPicker() {
        onEvent(EpisodesState.Event.BalancerPickerDismissed)
        restoreEpisodesFocusRequest += 1
    }

    val episodeAction = state.pendingEpisodeAction
    fun dismissEpisodeActions() {
        onEvent(EpisodesState.Event.EpisodeActionsDismissed)
        restoreEpisodesFocusRequest += 1
    }

    val balancerPicker = state.pendingBalancerSelection
    val dubbingPicker = state.pendingEpisodeDubbingSelection
    fun dismissDubbingPicker() {
        onEvent(EpisodesState.Event.EpisodeDubbingPickerDismissed)
        restoreEpisodesFocusRequest += 1
    }

    fun handleBack() {
        if (episodeAction != null) {
            dismissEpisodeActions()
        } else if (dubbingPicker != null) {
            dismissDubbingPicker()
        } else if (balancerPicker != null) {
            dismissBalancerPicker()
        } else {
            onEvent(EpisodesState.Event.BackSelected)
        }
    }

    BackHandler(enabled = balancerPicker == null && dubbingPicker == null && episodeAction == null) {
        handleBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.Back, Key.Escape -> {
                        handleBack()
                        true
                    }

                    else -> false
                }
            }
            .background(MaterialTheme.colorScheme.background),
    ) {
        EpisodesSection(
            state = state.videosState,
            animeId = state.animeId,
            episodeGroups = state.episodeGroups,
            bestDubbing = state.bestDubbing,
            watchProgress = state.watchProgress,
            restoreFocusRequest = restoreEpisodesFocusRequest,
            scrollAnchorEpisode = state.scrollAnchorEpisode,
            onAnchorEpisodeChanged = { anchorEpisode = it },
            episodeInfo = state.episodeInfo,
            episodeFlags = state.episodeFlags,
            onVideoSelected = { video -> onEvent(EpisodesState.Event.EpisodeSelected(video)) },
            onVideoLongPressed = { videos ->
                onEvent(EpisodesState.Event.EpisodeActionsRequested(videos))
            },
            onRetry = { onEvent(EpisodesState.Event.RetryVideosSelected) },
        )

        if (state.videosState is su.afk.yummy.tv.feature.details.details.model.VideosUiState.Content) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)) {
                EpisodeWatchFilterTvAction(selected = state.watchFilter) { filter ->
                    onEvent(EpisodesState.Event.WatchFilterSelected(filter, anchorEpisode))
                }
            }
        }

        BackHandler(enabled = balancerPicker != null) {
            handleBack()
        }
        if (balancerPicker != null) {
            BalancerPickerOverlay(
                picker = balancerPicker,
                onConfirmed = { option ->
                    onEvent(EpisodesState.Event.BalancerConfirmed(option.video))
                },
                onStarToggled = { option ->
                    onEvent(EpisodesState.Event.BalancerStarToggled(option.playerName))
                },
                onDismiss = ::dismissBalancerPicker,
            )
        }
        if (episodeAction != null) {
            EpisodeActionsTvDialog(
                action = episodeAction,
                onToggleWatched = {
                    onEvent(EpisodesState.Event.EpisodeWatchedToggled)
                    restoreEpisodesFocusRequest += 1
                },
                onClearWatched = {
                    onEvent(EpisodesState.Event.EpisodeWatchedCleared)
                    restoreEpisodesFocusRequest += 1
                },
                onToggleWatchedUpTo = {
                    onEvent(
                        EpisodesState.Event.EpisodesWatchedUpToChanged(
                            watched = !episodeAction.isWatched,
                        ),
                    )
                    restoreEpisodesFocusRequest += 1
                },
                onDismiss = ::dismissEpisodeActions,
            )
        }
        if (dubbingPicker != null) {
            EpisodeDubbingPickerOverlay(
                selection = dubbingPicker,
                onSelected = { option ->
                    onEvent(EpisodesState.Event.EpisodeDubbingSelected(option.video))
                },
                onStarToggled = { option ->
                    onEvent(EpisodesState.Event.DubbingStarToggled(option.item.name))
                },
                onDismiss = ::dismissDubbingPicker,
            )
        }
    }
}
