package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import su.afk.yummy.tv.feature.player.presentation.R

/**
 * Единственный элемент, доступный при заблокированном управлении. Снимается только удержанием,
 * чтобы случайный тап/свайп не разблокировал плеер; короткий тап показывает подсказку.
 */
@Composable
internal fun MobilePlayerUnlockButton(
    visible: Boolean,
    onUnlock: () -> Unit,
    /** Требовать удержание (настройка «Удержание для блокировки»); иначе разблокировка обычным тапом. */
    holdRequired: Boolean,
    /** Любое касание кнопки: родитель продлевает время её показа. */
    onTouched: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = mobilePlayerControlButtonSize(),
    modifier: Modifier = Modifier,
) {
    val holdProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var hintVisible by remember { mutableStateOf(false) }
    val unlockLabel = stringResource(R.string.player_mobile_unlock)

    LaunchedEffect(hintVisible) {
        if (hintVisible) {
            delay(UNLOCK_HINT_DURATION_MS)
            hintVisible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(buttonSize)
                    .semantics {
                        role = Role.Button
                        contentDescription = unlockLabel
                    }
                    .pointerInput(holdRequired) {
                        detectTapGestures(
                            onTap = { if (!holdRequired) onUnlock() },
                            onPress = {
                                onTouched()
                                if (!holdRequired) return@detectTapGestures
                                val hold = scope.launch {
                                    holdProgress.snapTo(0f)
                                    holdProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = HOLD_TO_CONFIRM_MS,
                                            easing = LinearEasing,
                                        ),
                                    )
                                    onUnlock()
                                }
                                val released = tryAwaitRelease()
                                if (hold.isActive) {
                                    hold.cancel()
                                    scope.launch { holdProgress.snapTo(0f) }
                                    if (released) hintVisible = true
                                }
                            },
                        )
                    },
            ) {
                CircularProgressIndicator(
                    progress = { holdProgress.value },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(buttonSize),
                )
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(minOf(24.dp, buttonSize * 0.56f)),
                )
            }
            AnimatedVisibility(
                visible = hintVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = -(buttonSize + 12.dp)),
            ) {
                Text(
                    text = stringResource(R.string.player_mobile_unlock_hint),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .mobilePlayerGlassSurface()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}


/** Кнопка блокировки расположена в той же точке, что и кнопка разблокировки. */
@Composable
internal fun MobilePlayerLockButton(
    visible: Boolean,
    onLock: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = mobilePlayerControlButtonSize(),
    modifier: Modifier = Modifier,
) {
    val lockLabel = stringResource(R.string.player_mobile_lock)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(buttonSize)
                .pointerInput(Unit) { detectTapGestures(onTap = { onLock() }) }
                .semantics {
                    role = Role.Button
                    contentDescription = lockLabel
                },
        ) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(minOf(24.dp, buttonSize * 0.56f)),
            )
        }
    }
}

private const val UNLOCK_HINT_DURATION_MS = 1_800L
