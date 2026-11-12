package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.model.anime.EpisodeFlags
import su.afk.yummy.tv.feature.player.presentation.R
import su.afk.yummy.tv.core.designsystem.R as DesignSystemR

@Composable
internal fun MobilePlayerBackButton(
    onBack: () -> Unit,
    visible: Boolean,
    buttonSize: Dp = 48.dp,
    topInset: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(top = topInset + 8.dp)
            .size(buttonSize)
            .clickable(onClick = onBack),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignSystemR.string.common_back),
            tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
internal fun MobilePlayerTopBar(
    title: String,
    episode: String,
    dubbing: String,
    playerName: String,
    episodeFlags: EpisodeFlags?,
    qualityLabel: String,
    titleStartPadding: Dp = 4.dp,
    onEpisodeClick: () -> Unit,
    onDubbingClick: () -> Unit,
    onMoreActions: () -> Unit,
    onQualityClick: () -> Unit,
    selectedSpeed: Float,
    onSpeedClick: () -> Unit,
    onSpeedLongPress: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Row(
        modifier = modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(
                Color.Black.copy(alpha = 0.58f), Color.Black.copy(alpha = 0.20f), Color.Transparent,
            )))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            Modifier.weight(1f).padding(start = titleStartPadding, top = 2.dp, end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (title.isNotBlank()) {
                Text(
                    title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.42f)).padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            if (episode.isNotBlank()) {
                Row(
                    modifier = Modifier.mobilePlayerGlassSurface(shape = MobilePlayerInteractiveShape).clickable(onClick = onEpisodeClick)
                        .padding(start = 8.dp, end = 9.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(stringResource(R.string.player_mobile_episode_number, episode),
                        color = Color.White.copy(alpha = 0.88f), maxLines = 1, style = MaterialTheme.typography.bodySmall)
                    Icon(Icons.Filled.KeyboardArrowDown, stringResource(R.string.player_episodes_list_title),
                        tint = Color.White.copy(alpha = 0.88f), modifier = Modifier.size(18.dp))
                }
            }
            if (dubbing.isNotBlank() || playerName.isNotBlank()) {
                Row(
                    modifier = Modifier.mobilePlayerGlassSurface(shape = MobilePlayerInteractiveShape).clickable(onClick = onDubbingClick)
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dubbing.isNotBlank()) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                dubbing.dubbingDisplayName().removePrefix("Озвучка ").removePrefix("озвучка "),
                                color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                    if (dubbing.isNotBlank() && playerName.isNotBlank()) {
                        Text("|", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelSmall)
                    }
                    if (playerName.isNotBlank()) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                playerName.uppercase(), color = Color.White, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(Icons.Filled.KeyboardArrowDown, stringResource(R.string.player_balancer_name_prefix),
                                tint = Color.White.copy(alpha = 0.82f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            MobilePlayerEpisodeFlagLabel(episodeFlags, Modifier.padding(top = 3.dp))
        }
        Row(
            Modifier.padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TopBarQualityAction(qualityLabel, onQualityClick)
            TopBarSpeedAction(selectedSpeed, onSpeedClick, onSpeedLongPress)
            TopBarAction(Icons.Filled.MoreVert, stringResource(R.string.player_mobile_actions), onMoreActions)
        }
    }
}

@Composable
private fun TopBarQualityAction(label: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.widthIn(min = 54.dp, max = 72.dp)
            .size(height = mobilePlayerControlButtonSize(), width = 64.dp)
            .mobilePlayerGlassSurface(shape = MobilePlayerInteractiveShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp),
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                background = Color.Transparent,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun TopBarSpeedAction(speed: Float, onClick: () -> Unit, onLongPress: () -> Unit) {
    val label = if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 64.dp, height = mobilePlayerControlButtonSize())
            .mobilePlayerGlassSurface(shape = MobilePlayerInteractiveShape)
            .pointerInput(onClick, onLongPress) {
                detectTapGestures(onTap = { onClick() }, onLongPress = { onLongPress() })
            },
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                background = Color.Transparent,
            ),
            maxLines = 1,
        )
    }
}

@Composable
private fun TopBarAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(mobilePlayerControlButtonSize()).mobilePlayerGlassSurface(shape = MobilePlayerInteractiveShape).clickable(onClick = onClick),
    ) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

private val FlagFillerColor = Color(0xFFFFC107)
private val FlagRecapColor = Color(0xFF90CAF9)

@Composable
private fun MobilePlayerEpisodeFlagLabel(flags: EpisodeFlags?, modifier: Modifier = Modifier) {
    val (text, color) = when {
        flags == null -> return
        flags.filler -> stringResource(R.string.player_episode_flag_filler) to FlagFillerColor
        flags.recap -> stringResource(R.string.player_episode_flag_recap) to FlagRecapColor
        else -> return
    }
    Text(text, color = color, maxLines = 1, style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.mobilePlayerGlassSurface().padding(horizontal = 8.dp, vertical = 3.dp))
}
