package su.afk.yummy.tv.core.model.settings

/**
 * Visibility policy for one timestamp in the mobile video player.
 * CONTROLS means visible only while the control panel is open.
 */
enum class PlayerTimeDisplayMode {
    ALWAYS,
    NEVER,
    CONTROLS,
}

data class PlayerTimeDisplaySettings(
    val systemClock: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val elapsed: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
    val finishAt: PlayerTimeDisplayMode = PlayerTimeDisplayMode.CONTROLS,
) {
    fun encode(): String = listOf(systemClock, elapsed, finishAt).joinToString("|")

    companion object {
        fun decode(value: String?): PlayerTimeDisplaySettings {
            val parts = value.orEmpty().split('|')
            fun mode(index: Int): PlayerTimeDisplayMode =
                parts.getOrNull(index)?.let { runCatching { PlayerTimeDisplayMode.valueOf(it) }.getOrNull() }
                    ?: PlayerTimeDisplayMode.CONTROLS
            return PlayerTimeDisplaySettings(
                systemClock = mode(0),
                elapsed = mode(1),
                finishAt = mode(3).takeIf { parts.size > 3 } ?: mode(2),
            )
        }
    }
}
