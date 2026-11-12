package su.afk.yummy.tv.feature.player.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import su.afk.yummy.tv.core.preferences.settings.PlayerSettingsStore
import su.afk.yummy.tv.core.utils.coroutines.di.DefaultApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Яркость мобильного плеера, выбранная пользователем жестом: доля 0.01–1.0 или `null`,
 * пока яркость не менялась и действует системная.
 *
 * Синглтон на весь процесс: восстанавливается из DataStore при старте и сохраняется обратно
 * (с дебаунсом), чтобы переживать выход из плеера и перезапуск приложения.
 */
@OptIn(FlowPreview::class)
@Singleton
class PlayerBrightnessController @Inject constructor(
    settingsStore: PlayerSettingsStore,
    @DefaultApplicationScope private val scope: CoroutineScope,
) {
    private val _brightness = MutableStateFlow<Float?>(null)

    /** Сохранённая яркость как доля 0.01f..1f; `null` — использовать системную. */
    val brightness: StateFlow<Float?> = _brightness.asStateFlow()

    // До ответа DataStore null в _brightness ещё ничего не значит, поэтому сброс в хранилище не пишем.
    @Volatile
    private var restored = false

    init {
        scope.launch {
            val autoEnabled = settingsStore.mobilePlayerAutoBrightnessEnabled.first()
            val storedPercent = settingsStore.mobilePlayerBrightnessPercent.first()
            // Жест мог успеть сработать раньше, чем DataStore ответил: его значение главнее.
            if (_brightness.value == null) {
                _brightness.value = if (autoEnabled) null else
                    ((storedPercent ?: 100) / 100f).coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
            }
            restored = true
        }
        scope.launch {
            settingsStore.mobilePlayerAutoBrightnessEnabled.distinctUntilChanged().collect { enabled ->
                if (enabled) {
                    _brightness.value = null
                } else if (_brightness.value == null) {
                    val percent = settingsStore.mobilePlayerBrightnessPercent.first() ?: 100
                    _brightness.value = (percent / 100f).coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
                }
            }
        }
        scope.launch {
            _brightness
                .map { fraction -> fraction?.let { (it * 100f).roundToInt() } }
                .distinctUntilChanged()
                .debounce(PERSIST_DEBOUNCE_MS)
                .collect { percent ->
                    when {
                        percent != null -> settingsStore.setMobilePlayerBrightnessPercent(percent)
                        restored -> settingsStore.clearMobilePlayerBrightness()
                    }
                }
        }
    }

    fun setFraction(value: Float) {
        _brightness.value = value.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
        scope.launch { settingsStore.setMobilePlayerAutoBrightnessEnabled(false) }
    }

    /** Возвращает автояркость: ручное значение снимается и в памяти, и в хранилище. */
    fun setAuto() {
        _brightness.value = null
        scope.launch { settingsStore.setMobilePlayerAutoBrightnessEnabled(true) }
    }

    companion object {
        const val MIN_BRIGHTNESS = 0.01f
        const val MAX_BRIGHTNESS = 1f
        private const val PERSIST_DEBOUNCE_MS = 500L
    }
}
