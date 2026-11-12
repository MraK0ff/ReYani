package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    val visibleRows = listOf(
        shouldShow(settings.systemClock, controlsActive) to stringResource(R.string.player_time_display_clock_value, systemClock),
        shouldShow(settings.elapsed, controlsActive) to stringResource(R.string.player_time_display_elapsed_value, displayTimeMs.millisToClockTime()),
        shouldShow(settings.remaining, controlsActive) to stringResource(R.string.player_time_display_remaining_value, remainingMs.millisToClockTime()),
        shouldShow(settings.finishAt, controlsActive) to stringResource(R.string.player_time_display_finish_value, finishAt),
    ).filter { it.first }.map { it.second }
    if (visibleRows.isEmpty()) return
    Column(
        modifier = modifier
            .mobilePlayerGlassSurface()
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        visibleRows.forEach { value ->
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
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
