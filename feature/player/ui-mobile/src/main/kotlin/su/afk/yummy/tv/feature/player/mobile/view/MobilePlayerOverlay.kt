package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.feature.player.common.model.PlayerPlaybackProgressState
import su.afk.yummy.tv.feature.player.common.model.StepSeekDirection
import su.afk.yummy.tv.feature.player.presentation.R

@Composable
internal fun MobilePlayerOverlay(
    visible: Boolean,
    wantsPlay: Boolean,
    progress: PlayerPlaybackProgressState,
    hasPrevEpisode: Boolean,
    hasNextEpisode: Boolean,
    holdToSwitchEpisode: Boolean,
    onPlayPause: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    onStepSeek: (StepSeekDirection) -> Unit,
    onLongPressSeek: (StepSeekDirection) -> Unit,
    onPrevEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onTrackSettings: () -> Unit,
    onMoreActions: () -> Unit,
    modifier: Modifier = Modifier,
    episodesAvailable: Boolean = false,
    episodesExpanded: Boolean = false,
    episodesStripVisible: Boolean = false,
    onEpisodesExpandedChange: (Boolean) -> Unit = {},
    episodesStrip: @Composable () -> Unit = {},
    videoWidthFraction: Float = 1f,
    videoBottomInset: androidx.compose.ui.unit.Dp = 0.dp,
    timeDisplaySettings: PlayerTimeDisplaySettings = PlayerTimeDisplaySettings(),
    showRemainingTime: Boolean = false,
    onTimeCodeClick: () -> Unit = {},
) {
    if (!visible) return
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val expandedControlsOffset = minOf(72.dp, maxHeight * 0.12f)
        Box(
            modifier = Modifier.align(Alignment.Center)
                .offset(y = if (episodesStripVisible) -expandedControlsOffset else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            MobilePlayerCenterControls(
                wantsPlay = wantsPlay,
                hasPrevEpisode = hasPrevEpisode,
                hasNextEpisode = hasNextEpisode,
                onPlayPause = onPlayPause,
                onPrevEpisode = onPrevEpisode,
                onNextEpisode = onNextEpisode,
                loading = false,
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MobilePlayerProgressRow(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                displayTime = progress.displayTimeMs,
                duration = progress.duration,
                seekProgress = progress.progressFraction,
                bufferedProgress = progress.bufferedProgress,
                onSeekChange = onSeekChange,
                onSeekFinished = onSeekFinished,
                videoWidthFraction = videoWidthFraction,
                videoBottomInset = videoBottomInset,
                timeDisplaySettings = timeDisplaySettings,
                showRemainingTime = showRemainingTime,
                onTimeCodeClick = onTimeCodeClick,
            )
            if (episodesAvailable) {
                AnimatedVisibility(
                    visible = episodesStripVisible,
                    enter = expandVertically(animationSpec = tween(EPISODES_STRIP_ANIMATION_MS)),
                    exit = shrinkVertically(animationSpec = tween(EPISODES_STRIP_ANIMATION_MS)),
                    modifier = Modifier.pointerInput(Unit) { collapseOnSwipeDown(onEpisodesExpandedChange) },
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                        episodesStrip()
                    }
                }
            }
        }
    }
}

private suspend fun PointerInputScope.collapseOnSwipeDown(onExpandedChange: (Boolean) -> Unit) {
    var total = 0f
    detectVerticalDragGestures(
        onDragStart = { total = 0f },
        onVerticalDrag = { _, delta -> total += delta },
        onDragEnd = { if (total > SWIPE_THRESHOLD_PX) onExpandedChange(false) },
    )
}

private const val SWIPE_THRESHOLD_PX = 40f
private const val EPISODES_STRIP_ANIMATION_MS = 220
