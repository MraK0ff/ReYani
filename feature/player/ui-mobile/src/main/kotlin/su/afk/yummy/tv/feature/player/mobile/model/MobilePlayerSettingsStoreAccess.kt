package su.afk.yummy.tv.feature.player.mobile.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import su.afk.yummy.tv.core.preferences.settings.PlayerSettingsStore

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface MobilePlayerSettingsStoreEntryPoint {
    fun playerSettingsStore(): PlayerSettingsStore
}

@Composable
internal fun rememberMobilePlayerSettingsStore(): PlayerSettingsStore {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            MobilePlayerSettingsStoreEntryPoint::class.java,
        ).playerSettingsStore()
    }
}
