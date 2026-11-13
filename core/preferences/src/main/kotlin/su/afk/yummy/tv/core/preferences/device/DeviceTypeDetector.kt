package su.afk.yummy.tv.core.preferences.device

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import su.afk.yummy.tv.core.preferences.interface_mode.AppInterfaceMode
import su.afk.yummy.tv.core.preferences.interface_mode.AppInterfaceModePreferences
import javax.inject.Inject
import javax.inject.Singleton

enum class DeviceType { ANDROID, ANDROID_TV }

/** Единый источник определения платформы приложения. */
@Singleton
class DeviceTypeDetector @Inject constructor(
    @ApplicationContext context: Context,
    private val interfaceModePreferences: AppInterfaceModePreferences,
) {
    val hasLeanbackFeature: Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)

    val deviceType: DeviceType
    val isTv: Boolean get() = deviceType == DeviceType.ANDROID_TV

    init {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        val modeType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            uiModeManager?.currentModeType
                ?: (context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK)
        } else {
            context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        }
        val isTelevision = modeType == Configuration.UI_MODE_TYPE_TELEVISION
        if (isTelevision != hasLeanbackFeature) {
            Log.d(TAG, "UI mode / LEANBACK mismatch (modeType=$modeType, leanback=$hasLeanbackFeature); using UI mode")
        }
        val detectedType = if (isTelevision) DeviceType.ANDROID_TV else DeviceType.ANDROID
        deviceType = when (interfaceModePreferences.selectedMode) {
            AppInterfaceMode.MOBILE -> DeviceType.ANDROID
            AppInterfaceMode.TV -> DeviceType.ANDROID_TV
            AppInterfaceMode.AUTO, null -> detectedType
        }
    }

    private companion object { const val TAG = "DeviceTypeDetector" }
}
