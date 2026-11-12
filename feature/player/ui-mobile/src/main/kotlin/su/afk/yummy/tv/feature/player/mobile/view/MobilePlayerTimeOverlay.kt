package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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

    Box(modifier = modifier.fillMaxSize()) {
        // При FIT боковые чёрные поля — отдельная зона интерфейса, не часть видеокадра.
        // Не кладём часы поверх заголовка: используем именно letterbox-поля.
        if (sideInset >= 32.dp) {
            if (showClock) {
                Text(
                    text = systemClock,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.TopStart)
                        .padding(start = 2.dp)
                        .padding(top = videoTopInset + 96.dp)
                        .mobilePlayerGlassSurface()
                        .padding(horizontal = 3.dp, vertical = 4.dp),
                )
            }
            if (showFinish) {
                Text(
                    text = finishAt,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(end = 2.dp)
                        .padding(top = videoTopInset + 96.dp)
                        .mobilePlayerGlassSurface()
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                )
            }
        } else if (showClock || showFinish) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 58.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showClock) TimeChip(systemClock)
                if (showFinish) TimeChip(finishAt)
            }
        }

        if (showElapsed) {
            val remaining = (durationMs - displayTimeMs).coerceAtLeast(0L)
            Row(
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 14.dp)
                    .mobilePlayerGlassSurface()
                    .clickable(onClick = onTimeCodeClick)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (showRemainingTime) "-${remaining.millisToClockTime().padStart(5, '0')}"
                        else displayTimeMs.millisToClockTime().padStart(5, '0'),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.96f),
                )
                Text("/", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.58f))
                Text(
                    text = durationMs.millisToClockTime().padStart(5, '0'),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.62f),
                )
            }
        }
    }
}

@Composable
private fun TimeChip(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        maxLines = 1,
        modifier = Modifier.mobilePlayerGlassSurface().padding(horizontal = 7.dp, vertical = 5.dp),
    )
}

private fun shouldShow(mode: PlayerTimeDisplayMode, controlsActive: Boolean): Boolean =
    when (mode) {
        PlayerTimeDisplayMode.ALWAYS -> true
        PlayerTimeDisplayMode.NEVER -> false
        PlayerTimeDisplayMode.CONTROLS -> controlsActive
    }
