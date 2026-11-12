package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
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
    onPlayerSettings: () -> Unit,
    onMoreActions: () -> Unit,
    modifier: Modifier = Modifier,
    episodesAvailable: Boolean = false,
    episodesExpanded: Boolean = false,
    episodesStripVisible: Boolean = false,
    onEpisodesExpandedChange: (Boolean) -> Unit = {},
    episodesStrip: @Composable () -> Unit = {},
    videoWidthFraction: Float = 1f,
    videoBottomInset: androidx.compose.ui.unit.Dp = 0.dp,
) {
    if (!visible) return
    var holdHintTick by remember { mutableIntStateOf(0) }
    var holdHintVisible by remember { mutableStateOf(false) }
    LaunchedEffect(holdHintTick) {
        if (holdHintTick > 0) {
            holdHintVisible = true
            delay(HOLD_HINT_DURATION_MS)
            holdHintVisible = false
        }
    }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val expandedControlsOffset = minOf(72.dp, maxHeight * 0.12f)
        Box(
            modifier = Modifier.align(Alignment.Center)
                .offset(y = if (episodesStripVisible) -expandedControlsOffset else 0.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MobilePlayerActionButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = stringResource(R.string.player_mobile_previous_episode),
                    enabled = hasPrevEpisode,
                    onClick = onPrevEpisode,
                    onHold = onPrevEpisode.takeIf { holdToSwitchEpisode },
                    onHoldHint = { holdHintTick++ },
                )
                MobilePlayerActionButton(
                    icon = Icons.Filled.FastRewind,
                    contentDescription = stringResource(R.string.player_mobile_pip_rewind_10),
                    onClick = { onStepSeek(StepSeekDirection.Backward) },
                    onLongPress = { onLongPressSeek(StepSeekDirection.Backward) },
                )
                MobilePlayerActionButton(
                    icon = if (wantsPlay) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(if (wantsPlay) R.string.player_mobile_pip_pause else R.string.player_mobile_pip_play),
                    primary = true,
                    pauseButton = wantsPlay,
                    onClick = onPlayPause,
                )
                MobilePlayerActionButton(
                    icon = Icons.Filled.FastForward,
                    contentDescription = stringResource(R.string.player_mobile_pip_forward_10),
                    onClick = { onStepSeek(StepSeekDirection.Forward) },
                    onLongPress = { onLongPressSeek(StepSeekDirection.Forward) },
                )
                if (hasNextEpisode) {
                    MobilePlayerActionButton(
                        icon = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.player_mobile_next_episode),
                        onClick = onNextEpisode,
                        onHold = onNextEpisode.takeIf { holdToSwitchEpisode },
                        onHoldHint = { holdHintTick++ },
                    )
                }
            }
            AnimatedVisibility(
                visible = holdHintVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter).padding(top = 90.dp),
            ) {
                Text(
                    stringResource(R.string.player_mobile_hold_to_switch_episode),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.68f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.30f), Color.Black.copy(alpha = 0.72f))))
                .padding(vertical = 12.dp),
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
private const val HOLD_HINT_DURATION_MS = 1_800L
private const val EPISODES_STRIP_ANIMATION_MS = 220
