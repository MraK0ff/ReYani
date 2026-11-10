package su.afk.yummy.tv.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import su.afk.yummy.tv.core.preferences.device.DeviceTypeDetector
import su.afk.yummy.tv.core.preferences.interface_language.AppInterfaceLanguageContext
import javax.inject.Inject

/**
 * Единая точка входа для обычного launcher, Android TV launcher и диплинков.
 *
 * Автоматически определяет тип устройства и направляет исходный Intent в соответствующий интерфейс.
 */
@AndroidEntryPoint
class InterfaceRouterActivity : ComponentActivity() {

    @Inject
    lateinit var deviceTypeDetector: DeviceTypeDetector

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppInterfaceLanguageContext.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeInterfaceLanguageChanges()

        openDetectedInterface()
    }

    private fun openDetectedInterface() {
        val target = if (deviceTypeDetector.isTv) TvActivity::class.java else MobileActivity::class.java
        val sourceIntent = intent
        val targetIntent = Intent(this, target).apply {
            action = sourceIntent.action
            data = sourceIntent.data
            putExtras(sourceIntent)
            clipData = sourceIntent.clipData
            flags = (sourceIntent.flags and FORWARDED_URI_PERMISSION_FLAGS) or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(targetIntent)
        finish()
    }

    private companion object {
        const val FORWARDED_URI_PERMISSION_FLAGS =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
    }

    private fun observeInterfaceLanguageChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppInterfaceLanguageContext.languageChanges.collect {
                    // Обновляется только Activity: процесс и состояние навигации сохраняются.
                    recreate()
                }
            }
        }
    }
}
