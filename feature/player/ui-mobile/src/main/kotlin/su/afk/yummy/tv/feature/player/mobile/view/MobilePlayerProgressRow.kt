package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.model.settings.PlayerTimeDisplaySettings
import su.afk.yummy.tv.core.utils.formatting.millisToClockTime

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
    timeDisplaySettings: PlayerTimeDisplaySettings = PlayerTimeDisplaySettings(),
    showRemainingTime: Boolean = false,
    onTimeCodeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(videoWidthFraction.coerceIn(0.1f, 1f))

    ) {
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset(y = -videoBottomInset)
                .padding(horizontal = 20.dp, vertical = 6.dp),
        ) {
            MobilePlayerTimeline(
                progress = seekProgress.coerceIn(0f, 1f),
                bufferedProgress = bufferedProgress.coerceIn(seekProgress.coerceIn(0f, 1f), 1f),
                enabled = duration > 0L,
                onValueChange = onSeekChange,
                onValueChangeFinished = onSeekFinished,
                modifier = Modifier.fillMaxWidth(),
            )
            val remaining = (duration - displayTime).coerceAtLeast(0L)
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .mobilePlayerGlassSurface()
                    .clickable(onClick = onTimeCodeClick)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = (if (showRemainingTime) "-${remaining.millisToClockTime().padStart(5, '0')}" else displayTime.millisToClockTime().padStart(5, '0')),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.96f),
                )
                Text(
                    text = "/",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.58f),
                )
                Text(
                    text = duration.millisToClockTime().padStart(5, '0'),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.62f),
                )
            }
        }
    }
}

/** Своя дорожка вместо Material Slider: тонкая линия, цвет темы, белый бегунок и буферизация. */
@Composable
private fun MobilePlayerTimeline(
    progress: Float,
    bufferedProgress: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(24.dp)
            .pointerInput(enabled, onValueChange) {
                if (enabled) {
                    detectTapGestures { position ->
                        val fraction = (position.x / size.width.toFloat()).coerceIn(0f, 1f)
                        onValueChange(fraction)
                        onValueChangeFinished()
                    }
                }
            }
            .pointerInput(enabled, onValueChange, onValueChangeFinished) {
                if (enabled) {
                    detectDragGestures(
                        onDragStart = { position ->
                            onValueChange((position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            onValueChange((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        },
                        onDragEnd = onValueChangeFinished,
                        onDragCancel = onValueChangeFinished,
                    )
                }
            },
    ) {
        Canvas(Modifier.fillMaxWidth().height(24.dp)) {
            val centerY = size.height / 2f
            val stroke = 3.5.dp.toPx()
            val radius = stroke / 2f
            val endX = size.width
            val playedX = endX * safeProgress
            val bufferedX = endX * bufferedProgress.coerceIn(safeProgress, 1f)
            drawRoundRect(
                color = Color.White.copy(alpha = 0.54f),
                topLeft = Offset(0f, centerY - radius),
                size = Size(endX, stroke),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            )
            if (bufferedX > playedX) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.82f),
                    topLeft = Offset(playedX, centerY - radius),
                    size = Size(bufferedX - playedX, stroke),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
            }
            if (playedX > 0f) {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(0f, centerY - radius),
                    size = Size(playedX, stroke),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                )
            }
            if (enabled) {
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(playedX.coerceIn(0f, endX), centerY),
                )
            }
        }
    }
}
