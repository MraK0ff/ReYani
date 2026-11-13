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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import su.afk.yummy.tv.feature.player.mobile.model.MobilePlayerSettingsPage
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
    showAudioTracks: Boolean,
    showSubtitleTracks: Boolean,
    onMoreMenuItem: (MobilePlayerSettingsPage) -> Unit,
    onQualityClick: () -> Unit,
    selectedSpeed: Float,
    onSpeedClick: () -> Unit,
    onSpeedLongPress: () -> Unit,
    visible: Boolean,
    episodeExpanded: Boolean = false,
    dubbingExpanded: Boolean = false,
    onPopupVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    var qualityExpanded by remember { mutableStateOf(false) }
    var qualityPreviewIndex by remember { mutableIntStateOf(-1) }
    var moreExpanded by remember { mutableStateOf(false) }
    var speedExpanded by remember { mutableStateOf(false) }
    var speedPreviewIndex by remember { mutableIntStateOf(-1) }
    val density = LocalDensity.current
    val qualityScrollState = rememberScrollState()
    val speedScrollState = rememberScrollState()
    LaunchedEffect(speedExpanded) {
        if (speedExpanded) speedScrollState.scrollTo(with(density) { 48.dp.roundToPx() })
    }
    SideEffect { onPopupVisibilityChange(qualityExpanded || speedExpanded || moreExpanded) }
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.94f).padding(top = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (title.isNotBlank()) {
                Text(
                    title,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
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
                        Icon(
                            if (episodeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            stringResource(R.string.player_episodes_list_title),
                            tint = Color.White.copy(alpha = 0.62f),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            stringResource(R.string.player_mobile_episode_number, episode),
                            color = Color.White.copy(alpha = 0.62f),
                            maxLines = 1,
                            style = TextStyle(color = Color.White.copy(alpha = 0.62f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
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
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            )
                            if (!offlinePlayback) {
                                Icon(
                                    if (dubbingExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    stringResource(R.string.player_dubbing_title),
                                    tint = Color.White.copy(alpha = 0.62f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        if (offlinePlayback) {
                            Icon(Icons.Filled.Download, contentDescription = null,
                                tint = Color.White.copy(alpha = 0.92f), modifier = Modifier.size(18.dp))
                        } else if (playerName.isNotBlank()) {
                            Text("*", color = Color.White.copy(alpha = 0.72f), style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold))
                            Text(
                                playerName,
                                color = Color.White.copy(alpha = 0.78f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            )
                            Icon(
                                if (dubbingExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                stringResource(R.string.player_balancer_name_prefix),
                                tint = Color.White.copy(alpha = 0.62f),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
            MobilePlayerEpisodeFlagLabel(episodeFlags, Modifier.padding(top = 2.dp))
        }
        Row(
            Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (!offlinePlayback && qualityOptions.isNotEmpty()) {
                Box {
                    TopBarQualityAction(qualityLabel) { qualityExpanded = true }
                    DropdownMenu(expanded = qualityExpanded, onDismissRequest = { qualityExpanded = false }) {
                        val descendingQualities = qualityOptions.asReversed()
                        val choices = listOf("Адаптивное качество", "Лучшее доступное") +
                            descendingQualities + "Худшее доступное"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = minOf(420.dp, 48.dp * choices.size))
                                .pointerInput(choices) {
                                    val rowHeight = with(density) { 48.dp.toPx() }
                                    fun updatePreview(y: Float) {
                                        qualityPreviewIndex = ((y + qualityScrollState.value) / rowHeight).toInt().coerceIn(0, choices.lastIndex)
                                    }
                                    detectDragGestures(
                                        onDragStart = { updatePreview(it.y) },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            updatePreview(change.position.y)
                                        },
                                        onDragEnd = {
                                            when (qualityPreviewIndex) {
                                                0 -> onAdaptiveQualityChange(true)
                                                1 -> {
                                                    onAdaptiveQualityChange(false)
                                                    qualityOptions.lastOrNull()?.let(onQualitySelected)
                                                }
                                                choices.lastIndex -> {
                                                    onAdaptiveQualityChange(false)
                                                    qualityOptions.firstOrNull()?.let(onQualitySelected)
                                                }
                                                else -> {
                                                    onAdaptiveQualityChange(false)
                                                    descendingQualities.getOrNull(qualityPreviewIndex - 2)
                                                        ?.let(onQualitySelected)
                                                }
                                            }
                                            qualityPreviewIndex = -1
                                            qualityExpanded = false
                                        },
                                        onDragCancel = { qualityPreviewIndex = -1 },
                                    )
                                },
                        ) {
                            Column(modifier = Modifier.verticalScroll(qualityScrollState)) {
                                choices.forEachIndexed { index, choice ->
                                    DropdownMenuItem(
                                        modifier = if (index == qualityPreviewIndex) {
                                            Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                        } else Modifier,
                                        text = { Text(choice) },
                                        onClick = {
                                            when (index) {
                                                0 -> onAdaptiveQualityChange(true)
                                                1 -> {
                                                    onAdaptiveQualityChange(false)
                                                    qualityOptions.lastOrNull()?.let(onQualitySelected)
                                                }
                                                choices.lastIndex -> {
                                                    onAdaptiveQualityChange(false)
                                                    qualityOptions.firstOrNull()?.let(onQualitySelected)
                                                }
                                                else -> {
                                                    onAdaptiveQualityChange(false)
                                                    descendingQualities.getOrNull(index - 2)?.let(onQualitySelected)
                                                }
                                            }
                                            qualityExpanded = false
                                        },
                                    )
                                }
                            }

                        }
                    }
                }
            }
            Box {
                Box {
                    TopBarSpeedAction(selectedSpeed) { speedExpanded = true }
                }
                DropdownMenu(expanded = speedExpanded, onDismissRequest = { speedExpanded = false }) {
                    Box(
                        modifier = Modifier.pointerInput(speedOptions) {
                            val rowHeight = with(density) { 48.dp.toPx() }
                            detectDragGestures(
                                onDragStart = { position ->
                                    speedPreviewIndex = ((position.y + speedScrollState.value) / rowHeight).toInt().coerceIn(0, speedOptions.lastIndex)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    speedPreviewIndex = ((change.position.y + speedScrollState.value) / rowHeight).toInt().coerceIn(0, speedOptions.lastIndex)
                                },
                                onDragEnd = {
                                    speedOptions.getOrNull(speedPreviewIndex)?.let(onSpeedSelected)
                                    speedPreviewIndex = -1
                                    speedExpanded = false
                                },
                                onDragCancel = { speedPreviewIndex = -1 },
                            )
                        },
                    ) {
                        Column(
                            modifier = Modifier.heightIn(max = 420.dp).verticalScroll(speedScrollState),
                        ) {
                            speedOptions.forEachIndexed { index, speed ->
                                val label = if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
                                DropdownMenuItem(
                                    modifier = if (index == speedPreviewIndex) {
                                        Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                    } else Modifier,
                                    text = { Text(label) },
                                    onClick = {
                                        onSpeedSelected(speed)
                                        speedExpanded = false
                                    },
                                )
                            }
                        }
 }
                    }
                }
            Box {
                TopBarAction(Icons.Filled.MoreVert, stringResource(R.string.player_mobile_actions)) {
                    moreExpanded = true
                }
                DropdownMenu(expanded = moreExpanded, onDismissRequest = { moreExpanded = false }) {
                    if (showAudioTracks) DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_menu_audio_track)) },
                        leadingIcon = { Icon(Icons.Filled.Audiotrack, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.AudioTrack) },
                    )
                    if (showSubtitleTracks) DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_menu_subtitles)) },
                        leadingIcon = { Icon(Icons.Filled.Subtitles, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.Subtitles) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_sleep_title)) },
                        leadingIcon = { Icon(Icons.Filled.Bedtime, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.SleepTimer) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_resize_title)) },
                        leadingIcon = { Icon(Icons.Filled.AspectRatio, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.Resize) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_time_display_title)) },
                        leadingIcon = { Icon(Icons.Filled.AccessTime, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.TimeDisplay) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_menu_display)) },
                        leadingIcon = { Icon(Icons.Filled.BrightnessMedium, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.Display) },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player_menu_cast_pip)) },
                        leadingIcon = { Icon(Icons.Filled.Cast, contentDescription = null) },
                        onClick = { moreExpanded = false; onMoreMenuItem(MobilePlayerSettingsPage.CastAndPip) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBarQualityAction(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.widthIn(min = 72.dp, max = 112.dp)
            .size(height = mobilePlayerControlButtonSize(), width = 92.dp)
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
        modifier = Modifier.size(width = 50.dp, height = mobilePlayerControlButtonSize())
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            BasicText(text = label,
                style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 1)
        }
    }
}

@Composable
private fun TopBarAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 40.dp, height = mobilePlayerControlButtonSize()).clickable(onClick = onClick),
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
