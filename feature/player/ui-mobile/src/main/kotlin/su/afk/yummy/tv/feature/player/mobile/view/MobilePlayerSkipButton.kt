package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummySemanticColors
import su.afk.yummy.tv.feature.player.common.model.PlayerActiveSkip
import su.afk.yummy.tv.feature.player.common.view.autoSkipProgressFill
import su.afk.yummy.tv.feature.player.model.PlayerSkipType
import su.afk.yummy.tv.feature.player.presentation.R

@Composable
internal fun MobilePlayerSkipButton(
    skip: PlayerActiveSkip?,
    onClick: () -> Unit,
    onStay: () -> Unit,
    modifier: Modifier = Modifier,
    countdownSeconds: Int? = null,
    countdownProgress: Float? = null,
) {
    var lastType by remember { mutableStateOf(PlayerSkipType.Opening) }
    skip?.type?.let { lastType = it }
    val shape = RoundedCornerShape(16.dp)
    val displayedCountdown by animateIntAsState(
        targetValue = countdownSeconds ?: 0,
        animationSpec = tween(durationMillis = 180),
        label = "skipCountdown",
    )
    val accent = MaterialTheme.colorScheme.primary
    val textColor = Color.White

    AnimatedVisibility(
        visible = skip != null || countdownSeconds != null,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 3 },
        exit = fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { it / 3 },
    ) {
        Row(
            modifier = Modifier.animateContentSize(animationSpec = tween(180)),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (countdownSeconds != null) {
                Text(
                    text = stringResource(R.string.player_stay),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    modifier = Modifier
                        .clip(shape)
                        .background(YummySemanticColors.PanelScrim)
                        .border(1.dp, Color.White.copy(alpha = 0.20f), shape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onStay,
                        )
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                )
            }
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(accent.copy(alpha = 0.22f))
                    .autoSkipProgressFill(countdownProgress, accent.copy(alpha = 0.32f), 24.dp)
                    .border(1.dp, accent.copy(alpha = 0.62f), shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.FastForward, contentDescription = null,
                    tint = textColor, modifier = Modifier.size(19.dp))
                Text(
                    text = if (countdownSeconds != null) {
                        stringResource(
                            when (lastType) {
                                PlayerSkipType.Opening -> R.string.player_mobile_skip_opening_countdown
                                PlayerSkipType.Ending -> R.string.player_mobile_skip_ending_countdown
                            },
                            displayedCountdown,
                        )
                    } else {
                        stringResource(
                            when (lastType) {
                                PlayerSkipType.Opening -> R.string.player_mobile_skip_opening
                                PlayerSkipType.Ending -> R.string.player_mobile_skip_ending
                            }
                        )
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                )
            }
        }
    }
}
