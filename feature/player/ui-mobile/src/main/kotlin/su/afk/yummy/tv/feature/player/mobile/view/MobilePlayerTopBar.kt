package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(top = topInset).size(buttonSize).clickable(onClick = onBack),
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
    offlinePlayback: Boolean = false,
    episodeFlags: EpisodeFlags?,
    qualityLabel: String,
    qualityOptions: List<String>,
    selectedQuality: String?,
    adaptiveQuality: Boolean,
    onAdaptiveQualityChange: (Boolean) -> Unit,
    onQualitySelected: (String) -> Unit,
    onAdaptiveLimitSelected: (String) -> Unit,
    onAdaptiveLimitBestSelected: () -> Unit,
    speedOptions: List<Float>,
    onSpeedSelected: (Float) -> Unit,
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
    var qualityExpanded by remember { mutableStateOf(false) }
    var qualityPreviewIndex by remember { mutableIntStateOf(-1) }
    var qualityPreviewOffsetPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    var speedExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(start = 0.dp, end = 0.dp, top = 0.dp, bottom = 8.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.70f).padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (title.isNotBlank()) {
                Text(
                    title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (episode.isNotBlank()) {
                    Row(
                        modifier = Modifier.clickable(onClick = onEpisodeClick).padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            stringResource(R.string.player_mobile_episode_number, episode),
                            color = Color.White,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            stringResource(R.string.player_episodes_list_title),
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (dubbing.isNotBlank() || playerName.isNotBlank() || offlinePlayback) {
                    Text(" · ", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodyLarge)
                    Row(
                        modifier = (if (offlinePlayback) Modifier else Modifier.clickable(onClick = onDubbingClick))
                            .padding(horizontal = 3.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (dubbing.isNotBlank()) {
                            Text(
                                dubbing.dubbingDisplayName().removePrefix("Озвучка ").removePrefix("озвучка "),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        if (offlinePlayback) {
                            Icon(Icons.Filled.Download, contentDescription = null,
                                tint = Color.White.copy(alpha = 0.92f), modifier = Modifier.size(18.dp))
                        } else if (playerName.isNotBlank()) {
                            Text("|", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodyMedium)
                            Text(
                                playerName,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                Icons.Filled.KeyboardArrowDown,
                                stringResource(R.string.player_balancer_name_prefix),
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
            MobilePlayerEpisodeFlagLabel(episodeFlags, Modifier.padding(top = 2.dp))
        }
        Row(
            Modifier.align(Alignment.TopEnd).padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!offlinePlayback && qualityOptions.isNotEmpty()) {
                Box {
                    TopBarQualityAction(qualityLabel) { qualityExpanded = true }
                    DropdownMenu(expanded = qualityExpanded, onDismissRequest = { qualityExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Адаптивное качество") },
                            onClick = { onAdaptiveQualityChange(true); qualityExpanded = false },
                        )
                        DropdownMenuItem(
                            text = { Text("Лучшее доступное") },
                            onClick = {
                                onAdaptiveQualityChange(false)
                                qualityOptions.lastOrNull()?.let(onQualitySelected)
                                qualityExpanded = false
                            },
                        )
                        val descendingQualities = qualityOptions.asReversed()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .pointerInput(descendingQualities) {
                                    val rowHeight = with(density) { 48.dp.toPx() }
                                    fun updatePreview(y: Float) {
                                        qualityPreviewOffsetPx = y.coerceIn(0f, size.height.toFloat())
                                        qualityPreviewIndex = (y / rowHeight).toInt()
                                            .coerceIn(0, (descendingQualities.lastIndex).coerceAtLeast(0))
                                    }
                                    detectDragGestures(
                                        onDragStart = { updatePreview(it.y) },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            updatePreview(change.position.y)
                                        },
                                        onDragEnd = {
                                            descendingQualities.getOrNull(qualityPreviewIndex)?.let { quality ->
                                                onAdaptiveQualityChange(false)
                                                onQualitySelected(quality)
                                            }
                                            qualityPreviewIndex = -1
                                            qualityExpanded = false
                                        },
                                        onDragCancel = { qualityPreviewIndex = -1 },
                                    )
                                },
                        ) {
                            Column {
                                descendingQualities.forEach { quality ->
                                    DropdownMenuItem(
                                        text = { Text(quality) },
                                        onClick = {
                                            onAdaptiveQualityChange(false)
                                            onQualitySelected(quality)
                                            qualityExpanded = false
                                        },
                                    )
                                }
                            }
                            if (qualityPreviewIndex >= 0) {
                                Text(
                                    text = descendingQualities.getOrNull(qualityPreviewIndex).orEmpty(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(y = with(density) { qualityPreviewOffsetPx.toDp() } - 18.dp)
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        }
                        DropdownMenuItem(
                            text = { Text("Худшее доступное") },
                            onClick = {
                                onAdaptiveQualityChange(false)
                                qualityOptions.firstOrNull()?.let(onQualitySelected)
                                qualityExpanded = false
                            },
                        )
                    }
                }
            }
            Box {
                TopBarSpeedAction(selectedSpeed) { speedExpanded = true }
                val speedListState = rememberLazyListState(
                    initialFirstVisibleItemIndex = speedOptions.indexOfFirst { it >= 0.75f }.coerceAtLeast(0),
                )
                DropdownMenu(expanded = speedExpanded, onDismissRequest = { speedExpanded = false }) {
                    LazyColumn(state = speedListState, modifier = Modifier.heightIn(max = 384.dp)) {
                        itemsIndexed(speedOptions) { _, speed ->
                            DropdownMenuItem(
                                text = { Text(if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x") },
                                onClick = { onSpeedSelected(speed); speedExpanded = false },
                            )
                        }
                    }
                }
            }
            TopBarAction(Icons.Filled.MoreVert, stringResource(R.string.player_mobile_actions), onMoreActions)
        }
    }
}

@Composable
private fun TopBarQualityAction(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.widthIn(min = 72.dp, max = 112.dp)
            .size(height = mobilePlayerControlButtonSize(), width = 104.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.Filled.HighQuality, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            BasicText(
                text = label,
                style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun TopBarSpeedAction(speed: Float, onClick: () -> Unit) {
    val label = if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 56.dp, height = mobilePlayerControlButtonSize())
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            BasicText(text = label,
                style = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                maxLines = 1)
        }
    }
}

@Composable
private fun TopBarAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 44.dp, height = mobilePlayerControlButtonSize()).clickable(onClick = onClick),
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
