package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime
import su.afk.yummy.tv.feature.player.common.view.bufferedTrackOverlay

@Composable
internal fun MobilePlayerProgressRow(
    displayTime: Long,
    duration: Long,
    seekProgress: Float,
    bufferedProgress: Float,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    videoWidthFraction: Float = 1f,
    videoBottomInset: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val safeHorizontalInset = with(density) {
        maxOf(
            WindowInsets.safeDrawing.getLeft(this, layoutDirection),
            WindowInsets.safeDrawing.getRight(this, layoutDirection),
        ).toDp()
    }
    Box(
        modifier = modifier
            .fillMaxWidth(videoWidthFraction.coerceIn(0.1f, 1f))
            .padding(horizontal = safeHorizontalInset),
    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = -videoBottomInset)
                .padding(horizontal = 20.dp, vertical = 6.dp),
        ) {
            MobileBufferedSlider(
                value = seekProgress.coerceIn(0f, 1f), bufferedProgress = bufferedProgress,
                // На мобильном плеере оставляем одну непрерывную дорожку без отдельной метки опенинга.
                openingRange = null,
                onValueChange = onSeekChange, onValueChangeFinished = onSeekFinished,
                enabled = duration > 0, modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "${displayTime.millisToClockTime()}/${duration.millisToClockTime()}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp, end = 10.dp),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MobileBufferedSlider(
    value: Float,
    bufferedProgress: Float,
    openingRange: ClosedFloatingPointRange<Float>?,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeProgress = value.coerceIn(0f, 1f)
    val buffered = bufferedProgress.coerceIn(activeProgress, 1f)
    val trackColor = Color.Black.copy(alpha = 0.48f)
    val playedColor = MaterialTheme.colorScheme.primary
    val colors = SliderDefaults.colors(
        thumbColor = Color.Transparent,
        // Единая нейтральная основа: просмотренная часть выделяется серым, не белым.
        activeTrackColor = trackColor,
        inactiveTrackColor = trackColor,
        disabledThumbColor = Color.Transparent,
        disabledActiveTrackColor = Color(0xFF777B7D),
        disabledInactiveTrackColor = Color.Black.copy(alpha = 0.24f),
        activeTickColor = Color.Transparent, inactiveTickColor = Color.Transparent,
    )
    val sliderState = remember { SliderState(value = activeProgress) }
    sliderState.value = activeProgress
    Slider(
        state = sliderState, onValueChange = onValueChange, onValueChangeFinished = onValueChangeFinished,
        enabled = enabled, modifier = modifier, colors = colors,
        thumb = {},
        track = { state ->
            SliderDefaults.Track(
                sliderState = state,
                enabled = enabled,
                colors = colors,
                // The invisible thumb must not cut a visual gap into the continuous timeline.
                thumbTrackGapSize = 0.dp,
                modifier = Modifier.bufferedTrackOverlay(
                    activeProgress = activeProgress,
                    bufferedProgress = buffered,
                    color = trackColor,
                    openingRange = openingRange,
                    openingColor = Color(0xFFD7D1C6),
                    playedColor = playedColor,
                    gap = 0.dp,
                ),
            )
        },
    )
}
