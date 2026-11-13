package su.afk.yummy.tv.core.model.settings

/**
 * Visibility policy for one timestamp in the mobile video player.
 * CONTROLS means visible only while the control panel is open.
 */
enum class PlayerTimeDisplayMode {
    ALWAYS,
    CONTROLS,
}

data class PlayerTimeDisplaySettings(
    val systemClock: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val elapsed: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val duration: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val finishAt: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
) {
    fun encode(): String = listOf(systemClock, elapsed, duration, finishAt).joinToString("|")

    companion object {
        fun decode(value: String?): PlayerTimeDisplaySettings {
            val parts = value.orEmpty().split('|')
            fun mode(index: Int): PlayerTimeDisplayMode =
                parts.getOrNull(index)
                    ?.let { runCatching { PlayerTimeDisplayMode.valueOf(it) }.getOrNull() }
                    ?: PlayerTimeDisplayMode.CONTROLS
            // Старый формат содержал три поля: systemClock|elapsed|finishAt.
            // Старое значение NEVER переводим в CONTROLS, так как отключение времени больше не поддерживается.
            val legacy = parts.size == 3
            return PlayerTimeDisplaySettings(
                systemClock = mode(0),
                elapsed = mode(1),
                duration = if (legacy) mode(1) else mode(2),
                finishAt = mode(if (legacy) 2 else 3),
            )
        }
    }
}
