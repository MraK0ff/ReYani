package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplayMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MobilePlayerTimeOverlay(
    settings: PlayerTimeDisplaySettings,
    displayTimeMs: Long,
    durationMs: Long,
    controlsActive: Boolean,
    sideInset: Dp,
    videoTopInset: Dp,
    videoBottomInset: Dp,
    showRemainingTime: Boolean,
    onTimeCodeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1_000L)
        }
    }
    val remainingMs = (durationMs - displayTimeMs).coerceAtLeast(0L)
    val clockFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val systemClock = clockFormat.format(Date(nowMs))
    val finishAt = clockFormat.format(Date(nowMs + remainingMs))
    val showClock = shouldShow(settings.systemClock, controlsActive)
    val showFinish = durationMs > 0L && shouldShow(settings.finishAt, controlsActive)
    val showElapsed = durationMs > 0L && settings.elapsed == PlayerTimeDisplayMode.ALWAYS && !controlsActive
    val showDuration = durationMs > 0L && settings.duration == PlayerTimeDisplayMode.ALWAYS && !controlsActive
    val edgeTopPadding = if (videoTopInset > 0.dp) videoTopInset / 2 else 12.dp
    val edgeBottomPadding = if (videoBottomInset > 0.dp) videoBottomInset / 2 else 12.dp

    Box(modifier = modifier.fillMaxSize()) {
        if (showClock) {
            TimeChip(
                value = systemClock,
                modifier = Modifier.align(Alignment.TopStart)
                    .padding(start = 20.dp, top = edgeTopPadding),
            )
        }
        if (showFinish) {
            TimeChip(
                value = finishAt,
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(end = 20.dp, top = edgeTopPadding),
            )
        }
        if (showElapsed) {
            TimeValueChip(
                value = if (showRemainingTime) "-${remainingMs.millisToClockTime().padStart(5, '0')}"
                    else displayTimeMs.millisToClockTime().padStart(5, '0'),
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = edgeBottomPadding),
                onClick = onTimeCodeClick,
            )
        }
        if (showDuration) {
            TimeValueChip(
                value = durationMs.millisToClockTime().padStart(5, '0'),
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = edgeBottomPadding),
                onClick = onTimeCodeClick,
            )
        }
    }
}

@Composable
private fun TimeChip(value: String, modifier: Modifier = Modifier) {
    Text(
        text = value,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        maxLines = 1,
        modifier = modifier.mobilePlayerGlassSurface().padding(horizontal = 7.dp, vertical = 5.dp),
    )
}

@Composable
private fun TimeValueChip(
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = value,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        maxLines = 1,
        modifier = modifier.mobilePlayerGlassSurface()
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    )
}

private fun shouldShow(mode: PlayerTimeDisplayMode, controlsActive: Boolean): Boolean =
    when (mode) {
        PlayerTimeDisplayMode.ALWAYS -> true
        PlayerTimeDisplayMode.CONTROLS -> controlsActive
    }
