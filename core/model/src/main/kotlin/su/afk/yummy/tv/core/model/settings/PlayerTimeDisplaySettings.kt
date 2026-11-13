package su.afk.yummy.tv.core.model.settings

/**
 * Visibility policy for one timestamp in the mobile video player.
 * CONTROLS means visible only while the control panel is open.
 */
enum class PlayerTimeDisplayMode {
    ALWAYS,
    CONTROLS,
    NEVER,
}

data class PlayerTimeDisplaySettings(
    val systemClock: PlayerTimeDisplayMode = PlayerTimeDisplayMode.NEVER,
    val elapsed: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val duration: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val finishAt: PlayerTimeDisplayMode = PlayerTimeDisplayMode.NEVER,
    val separateTimecodes: Boolean = false,
) {
    fun encode(): String = listOf(systemClock, elapsed, duration, finishAt, separateTimecodes).joinToString("|")

    companion object {
        fun decode(value: String?): PlayerTimeDisplaySettings {
            if (value.isNullOrBlank()) return PlayerTimeDisplaySettings()
            val parts = value.split('|')
            fun mode(index: Int, default: PlayerTimeDisplayMode): PlayerTimeDisplayMode =
                parts.getOrNull(index)
                    ?.let { runCatching { PlayerTimeDisplayMode.valueOf(it) }.getOrNull() }
                    ?: default
            // Старый формат содержал три поля; текущий до этого изменения — четыре.
            val legacy = parts.size == 3
            return PlayerTimeDisplaySettings(
                systemClock = mode(0, PlayerTimeDisplayMode.NEVER),
                elapsed = mode(1, PlayerTimeDisplayMode.CONTROLS),
                duration = if (legacy) mode(1, PlayerTimeDisplayMode.CONTROLS)
                    else mode(2, PlayerTimeDisplayMode.CONTROLS),
                finishAt = mode(if (legacy) 2 else 3, PlayerTimeDisplayMode.NEVER),
                separateTimecodes = parts.getOrNull(4)?.toBooleanStrictOrNull()
                    ?: (!legacy &&
                        mode(1, PlayerTimeDisplayMode.CONTROLS) == PlayerTimeDisplayMode.ALWAYS &&
                        mode(2, PlayerTimeDisplayMode.CONTROLS) == PlayerTimeDisplayMode.ALWAYS),
            )
        }
    }
}
