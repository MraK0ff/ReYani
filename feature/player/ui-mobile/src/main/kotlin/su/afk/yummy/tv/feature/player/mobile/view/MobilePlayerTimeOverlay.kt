package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplayMode
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime
import su.afk.yummy.tv.feature.player.presentation.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MobilePlayerTimeOverlay(
    settings: PlayerTimeDisplaySettings,
    displayTimeMs: Long,
    durationMs: Long,
    controlsActive: Boolean,
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
    if (!showClock && !showFinish && !(settings.elapsed == PlayerTimeDisplayMode.ALWAYS && !controlsActive)) return
    Box(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 76.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (showClock) {
                Text(
                    text = stringResource(R.string.player_time_display_clock_value, systemClock),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.mobilePlayerGlassSurface().padding(horizontal = 9.dp, vertical = 6.dp),
                )
            }
            if (showFinish) {
                Text(
                    text = stringResource(R.string.player_time_display_finish_value, finishAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.mobilePlayerGlassSurface().padding(horizontal = 9.dp, vertical = 6.dp),
                )
            }
        }
        if (settings.elapsed == PlayerTimeDisplayMode.ALWAYS && !controlsActive && durationMs > 0L) {
            val shownDuration = if (settings.showRemainingInsteadOfDuration) remainingMs else durationMs
            Text(
                text = "${displayTimeMs.millisToClockTime().padStart(5, '0')} / ${shownDuration.millisToClockTime().padStart(5, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.96f),
                modifier = Modifier.align(Alignment.BottomCenter)
                    .mobilePlayerGlassSurface()
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
    }
}

private fun shouldShow(mode: PlayerTimeDisplayMode, controlsActive: Boolean): Boolean =
    when (mode) {
        PlayerTimeDisplayMode.ALWAYS -> true
        PlayerTimeDisplayMode.NEVER -> false
        PlayerTimeDisplayMode.CONTROLS -> controlsActive
    }
