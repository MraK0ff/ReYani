package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.feature.player.presentation.R

@Composable
internal fun MobilePlayerCenterControls(
    wantsPlay: Boolean,
    hasPrevEpisode: Boolean,
    hasNextEpisode: Boolean,
    onPlayPause: () -> Unit,
    onPrevEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CENTER_CONTROLS_GAP),
    ) {
        Box(Modifier.size(CENTER_SWITCH_SLOT), contentAlignment = Alignment.Center) {
            if (hasPrevEpisode) MobilePlayerActionButton(
                icon = Icons.Filled.SkipPrevious,
                contentDescription = stringResource(R.string.player_mobile_previous_episode),
                onClick = onPrevEpisode,
                sizeMultiplier = CENTER_SWITCH_SCALE,
                iconScale = CENTER_SWITCH_ICON_SCALE,
                surface = false,
            )
        }
        val playPauseDescription = stringResource(
            if (wantsPlay) R.string.player_mobile_pip_pause else R.string.player_mobile_pip_play,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(CENTER_PLAY_SIZE)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClickLabel = playPauseDescription,
                    onClick = onPlayPause,
                ),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(42.dp),
                )
            } else {
                Icon(
                    imageVector = if (wantsPlay) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = playPauseDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(CENTER_PLAY_ICON_SIZE),
                )
            }
        }
        Box(Modifier.size(CENTER_SWITCH_SLOT), contentAlignment = Alignment.Center) {
            if (hasNextEpisode) MobilePlayerActionButton(
                icon = Icons.Filled.SkipNext,
                contentDescription = stringResource(R.string.player_mobile_next_episode),
                onClick = onNextEpisode,
                sizeMultiplier = CENTER_SWITCH_SCALE,
                iconScale = CENTER_SWITCH_ICON_SCALE,
            )
        }
    }
}

private const val CENTER_SWITCH_SCALE = 1.035f
private const val CENTER_SWITCH_ICON_SCALE = 0.88f
private val CENTER_CONTROLS_GAP = 20.dp
private val CENTER_SWITCH_SLOT = 80.dp
private val CENTER_PLAY_SIZE = 82.dp
private val CENTER_PLAY_ICON_SIZE = 70.dp
