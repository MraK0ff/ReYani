package su.afk.yummy.tv.feature.player.common.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Рисует поверх дорожки слайдера буфер (за проигранной частью) и метку опенинга.
 * Доли — `0f..1f` от длительности; в RTL-раскладке направление зеркалится.
 */
fun Modifier.bufferedTrackOverlay(
    activeProgress: Float,
    bufferedProgress: Float,
    color: Color,
    openingRange: ClosedFloatingPointRange<Float>?,
    openingColor: Color,
    playedColor: Color? = null,
    gap: Dp = 6.dp,
): Modifier =
    drawWithContent {
        drawContent()

        val trackY = size.height / 2f
        val strokeWidth = 4.dp.toPx()

        // Рисуем просмотренную часть с плоским внутренним краем: скругление
        // на стыке двух участков создаёт заметные выступы сверху и снизу.
        if (playedColor != null && activeProgress > 0f) {
            val playedEndX = if (layoutDirection == LayoutDirection.Rtl) {
                size.width * (1f - activeProgress)
            } else {
                size.width * activeProgress
            }
            drawLine(
                color = playedColor,
                start = Offset(if (layoutDirection == LayoutDirection.Rtl) size.width else 0f, trackY),
                end = Offset(playedEndX, trackY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        // Метка опенинга: рисуем поверх дорожки, чтобы отрезок был виден и под проигранной частью.
        if (openingRange != null) {
            val oStartX: Float
            val oEndX: Float
            if (layoutDirection == LayoutDirection.Rtl) {
                oStartX = (size.width * (1f - openingRange.start)).coerceIn(0f, size.width)
                oEndX = (size.width * (1f - openingRange.endInclusive)).coerceIn(0f, size.width)
            } else {
                oStartX = (size.width * openingRange.start).coerceIn(0f, size.width)
                oEndX = (size.width * openingRange.endInclusive).coerceIn(0f, size.width)
            }
            if (oStartX != oEndX) {
                drawLine(
                    color = openingColor,
                    start = Offset(oStartX, trackY),
                    end = Offset(oEndX, trackY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }
        }

        if (bufferedProgress <= activeProgress) return@drawWithContent

        val gapPx = gap.toPx()
        val startX: Float
        val endX: Float
        if (layoutDirection == LayoutDirection.Rtl) {
            startX = (size.width * (1f - activeProgress) - gapPx).coerceIn(0f, size.width)
            endX = (size.width * (1f - bufferedProgress)).coerceIn(0f, size.width)
        } else {
            startX = (size.width * activeProgress + gapPx).coerceIn(0f, size.width)
            endX = (size.width * bufferedProgress).coerceIn(0f, size.width)
        }
        if (startX == endX) return@drawWithContent
        drawLine(
            color = color,
            start = Offset(startX, trackY),
            end = Offset(endX, trackY),
            strokeWidth = strokeWidth,
            // Плоский край у стыка с просмотренной частью; внешнее скругление
            // всей дорожки остаётся у базового Slider.Track.
            cap = StrokeCap.Butt,
        )
    }
