package su.afk.yummy.tv.feature.playersetup.mobile.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ScreenRotation
import androidx.compose.material.icons.rounded.StayCurrentLandscape
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.model.settings.AppInterfaceLanguage
import su.afk.yummy.tv.core.model.settings.AppTheme
import su.afk.yummy.tv.core.model.settings.BackgroundStyle
import su.afk.yummy.tv.core.model.settings.PlayerOrientationMode
import su.afk.yummy.tv.core.model.settings.PreferredVideoQuality
import su.afk.yummy.tv.core.model.settings.YaniContentLanguage
import su.afk.yummy.tv.feature.playersetup.presentation.R

@Composable
internal fun PreferredVideoQuality.setupLabel(): String =
    height?.let { "${it}p" } ?: stringResource(R.string.player_setup_quality_best)

@Composable
internal fun PlayerOrientationMode.setupLabel(): String = stringResource(
    when (this) {
        PlayerOrientationMode.SYSTEM -> R.string.player_setup_mobile_orientation_system
        PlayerOrientationMode.SENSOR_LANDSCAPE -> R.string.player_setup_mobile_orientation_sensor_landscape
        PlayerOrientationMode.PORTRAIT -> R.string.player_setup_mobile_orientation_portrait
    },
)

internal fun PlayerOrientationMode.setupIcon(): ImageVector = when (this) {
    PlayerOrientationMode.SYSTEM -> Icons.Rounded.ScreenRotation
    PlayerOrientationMode.SENSOR_LANDSCAPE -> Icons.Rounded.StayCurrentLandscape
    PlayerOrientationMode.PORTRAIT -> Icons.Rounded.StayCurrentPortrait
}

@Composable
internal fun YaniContentLanguage.setupLabel(): String = stringResource(
    when (this) {
        YaniContentLanguage.RUSSIAN -> R.string.player_setup_language_russian
        YaniContentLanguage.ENGLISH -> R.string.player_setup_language_english
        YaniContentLanguage.UKRAINIAN -> R.string.player_setup_language_ukrainian
    },
)

@Composable
internal fun PlayerOrientationMode.setupHint(): String = stringResource(
    when (this) {
        PlayerOrientationMode.SYSTEM -> R.string.player_setup_mobile_orientation_system_hint
        PlayerOrientationMode.SENSOR_LANDSCAPE -> R.string.player_setup_mobile_orientation_sensor_landscape_hint
        PlayerOrientationMode.PORTRAIT -> R.string.player_setup_mobile_orientation_portrait_hint
    },
)

@Composable
internal fun PreferredVideoQuality.setupHint(): String = stringResource(
    if (this == PreferredVideoQuality.BEST) {
        R.string.player_setup_mobile_quality_best_hint
    } else {
        R.string.player_setup_quality_hint
    },
)

@Composable
internal fun AppInterfaceLanguage.setupLabel(): String = stringResource(when (this) {
    AppInterfaceLanguage.RUSSIAN -> R.string.player_setup_language_russian
    AppInterfaceLanguage.ENGLISH -> R.string.player_setup_language_english
    AppInterfaceLanguage.UKRAINIAN -> R.string.player_setup_language_ukrainian
})
@Composable
internal fun AppTheme.setupLabel(): String = stringResource(when (this) {
    AppTheme.GRAPHITE -> R.string.player_setup_theme_graphite
    AppTheme.WARM_AMBER -> R.string.player_setup_theme_amber
    AppTheme.SAKURA -> R.string.player_setup_theme_sakura
    AppTheme.MINT -> R.string.player_setup_theme_mint
    AppTheme.OCEAN -> R.string.player_setup_theme_ocean
    AppTheme.DYNAMIC -> R.string.player_setup_theme_dynamic
})
@Composable
internal fun BackgroundStyle.setupLabel(): String = stringResource(when (this) {
    BackgroundStyle.SYSTEM -> R.string.player_setup_background_system
    BackgroundStyle.LIGHT -> R.string.player_setup_background_light
    BackgroundStyle.DARK -> R.string.player_setup_background_dark
    BackgroundStyle.AMOLED -> R.string.player_setup_background_amoled
})
