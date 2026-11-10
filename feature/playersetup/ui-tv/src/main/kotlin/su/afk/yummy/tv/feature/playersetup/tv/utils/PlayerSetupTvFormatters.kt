package su.afk.yummy.tv.feature.playersetup.tv.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.model.settings.AppInterfaceLanguage
import su.afk.yummy.tv.core.model.settings.AppTheme
import su.afk.yummy.tv.core.model.settings.BackgroundStyle
import su.afk.yummy.tv.core.model.settings.PreferredVideoQuality
import su.afk.yummy.tv.core.model.settings.YaniContentLanguage
import su.afk.yummy.tv.feature.playersetup.presentation.R

@Composable
internal fun PreferredVideoQuality.setupLabel(): String =
    height?.let { "${it}p" } ?: stringResource(R.string.player_setup_quality_best)

internal fun PreferredVideoQuality.next(): PreferredVideoQuality =
    PreferredVideoQuality.entries[(ordinal + 1) % PreferredVideoQuality.entries.size]

@Composable
internal fun YaniContentLanguage.setupLabel(): String = stringResource(
    when (this) {
        YaniContentLanguage.RUSSIAN -> R.string.player_setup_language_russian
        YaniContentLanguage.ENGLISH -> R.string.player_setup_language_english
        YaniContentLanguage.UKRAINIAN -> R.string.player_setup_language_ukrainian
    },
)

internal fun YaniContentLanguage.next(): YaniContentLanguage =
    YaniContentLanguage.entries[(ordinal + 1) % YaniContentLanguage.entries.size]

@Composable
internal fun AppInterfaceLanguage.setupLabel(): String = stringResource(when (this) {
    AppInterfaceLanguage.RUSSIAN -> R.string.player_setup_language_russian
    AppInterfaceLanguage.ENGLISH -> R.string.player_setup_language_english
    AppInterfaceLanguage.UKRAINIAN -> R.string.player_setup_language_ukrainian
})
internal fun AppInterfaceLanguage.next(): AppInterfaceLanguage =
    AppInterfaceLanguage.entries[(ordinal + 1) % AppInterfaceLanguage.entries.size]
@Composable
internal fun AppTheme.setupLabel(): String = stringResource(when (this) {
    AppTheme.GRAPHITE -> R.string.player_setup_theme_graphite
    AppTheme.WARM_AMBER -> R.string.player_setup_theme_amber
    AppTheme.SAKURA -> R.string.player_setup_theme_sakura
    AppTheme.MINT -> R.string.player_setup_theme_mint
    AppTheme.OCEAN -> R.string.player_setup_theme_ocean
    AppTheme.DYNAMIC -> R.string.player_setup_theme_dynamic
})
internal fun AppTheme.next(): AppTheme = AppTheme.entries.filterNot { it == AppTheme.DYNAMIC }[(AppTheme.entries.filterNot { it == AppTheme.DYNAMIC }.indexOf(this) + 1) % AppTheme.entries.count { it != AppTheme.DYNAMIC }]
@Composable
internal fun BackgroundStyle.setupLabel(): String = stringResource(when (this) {
    BackgroundStyle.SYSTEM -> R.string.player_setup_background_system
    BackgroundStyle.LIGHT -> R.string.player_setup_background_light
    BackgroundStyle.DARK -> R.string.player_setup_background_dark
    BackgroundStyle.AMOLED -> R.string.player_setup_background_amoled
})
internal fun BackgroundStyle.next(): BackgroundStyle =
    BackgroundStyle.entries[(ordinal + 1) % BackgroundStyle.entries.size]
