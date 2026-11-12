package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun MobilePlayerActionButton(
    icon: ImageVector,
    /** Подпись для TalkBack: у кнопки нет текста, иначе она объявляется просто «кнопкой». */
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
    pauseButton: Boolean = false,
    /**
     * Режим удержания, как у разблокировки: действие срабатывает только после [HOLD_TO_CONFIRM_MS]
     * с кольцом прогресса, короткий тап лишь вызывает [onHoldHint]. [onClick] в этом режиме не используется.
     */
    onHold: (() -> Unit)? = null,
    onHoldHint: () -> Unit = {},
    onLongPress: (() -> Unit)? = null,
    sizeMultiplier: Float = 1f,
    iconScale: Float = 0.55f,
) {
    val shape = CircleShape
    val contentColor = when {
        !enabled -> Color.White.copy(alpha = 0.26f)
        primary -> MaterialTheme.colorScheme.onPrimary
        else -> Color.White
    }
    val holdProgress = remember { Animatable(0f) }
    val buttonSize = mobilePlayerControlButtonSize(primary) * sizeMultiplier
    val borderColor = Color.White.copy(alpha = 0.10f)
    val surfaceFill = when {
        !enabled -> Color.Black.copy(alpha = 0.10f)
        primary -> MaterialTheme.colorScheme.primary
        else -> Color.Black.copy(alpha = 0.16f)
    }

    val baseViewConfiguration = LocalViewConfiguration.current
    val quickLongPressConfiguration = remember(baseViewConfiguration) {
        object : ViewConfiguration by baseViewConfiguration {
            override val longPressTimeoutMillis: Long = 320L
        }
    }
    CompositionLocalProvider(LocalViewConfiguration provides quickLongPressConfiguration) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize)
            .mobilePlayerGlassSurface(shape = shape, fill = surfaceFill, borderColor = borderColor, elevation = 0.dp)
            .then(
                if (onHold == null && onLongPress != null) {
                    Modifier.pointerInput(enabled, onLongPress, onClick) {
                        detectTapGestures(
                            onTap = { if (enabled) onClick() },
                            onLongPress = { if (enabled) onLongPress() },
                        )
                    }
                } else if (onHold == null) {
                    Modifier.clickable(
                        enabled = enabled,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClickLabel = contentDescription,
                        onClick = onClick,
                    )
                } else {
                    Modifier.holdToConfirm(
                        enabled = enabled,
                        progress = holdProgress,
                        contentDescription = contentDescription,
                        onHold = onHold,
                        onHoldHint = onHoldHint,
                    )
                },
            ),
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = contentColor, modifier = Modifier.size(buttonSize * iconScale.coerceIn(0.1f, 1f)))
        if (onHold != null) {
            CircularProgressIndicator(
                progress = { holdProgress.value },
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent,
                strokeWidth = 3.dp,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
    }
}

/** Общая длительность удержания для подтверждения действия (разблокировка, смена серии). */
internal const val HOLD_TO_CONFIRM_MS = 350

private fun Modifier.holdToConfirm(
    enabled: Boolean,
    progress: Animatable<Float, AnimationVector1D>,
    contentDescription: String,
    onHold: () -> Unit,
    onHoldHint: () -> Unit,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val currentOnHold by rememberUpdatedState(onHold)
    val currentOnHoldHint by rememberUpdatedState(onHoldHint)
    this
        .semantics {
            role = Role.Button
            this.contentDescription = contentDescription
            // TalkBack не умеет удерживать: двойной тап выполняет действие сразу.
            if (enabled) {
                onClick {
                    currentOnHold()
                    true
                }
            }
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    val hold = scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(1f, tween(HOLD_TO_CONFIRM_MS, easing = LinearEasing))
                        currentOnHold()
                        progress.snapTo(0f)
                    }
                    val released = tryAwaitRelease()
                    if (hold.isActive) {
                        hold.cancel()
                        scope.launch { progress.snapTo(0f) }
                        if (released) currentOnHoldHint()
                    }
                },
            )
        }
}
