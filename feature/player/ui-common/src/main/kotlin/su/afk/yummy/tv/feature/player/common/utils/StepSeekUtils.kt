package su.afk.yummy.tv.feature.player.common.utils

import kotlin.math.abs

fun Long.formatSignedSeconds(): String {
    val totalSeconds = abs(this / 1_000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    val sign = when {
        this > 0L -> "+"
        this < 0L -> "−"
        else -> ""
    }
    val value = if (minutes > 0L) "${minutes}м ${seconds}с" else "${seconds}с"
    return sign + value
}

const val STEP_SEEK_RESET_MS = 1_500L
val STEP_SEEK_OFFSETS_MS = longArrayOf(5_000L, 10_000L, 15_000L)
