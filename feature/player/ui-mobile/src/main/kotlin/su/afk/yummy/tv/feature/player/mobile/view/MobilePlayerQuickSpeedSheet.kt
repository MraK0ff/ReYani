package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheetCustom
import su.afk.yummy.tv.core.designsystem.baseScreen.HideSheetWindowSystemBars
import su.afk.yummy.tv.core.model.settings.PlayerResizeMode
import su.afk.yummy.tv.feature.player.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerQuickSpeedSheet(
    selectedSpeed: Float,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    BaseBottomSheetCustom(onDismissRequest = onDismiss) { maxHeight ->
        HideSheetWindowSystemBars()
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
        ) {
            Text(
                stringResource(R.string.player_speed_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            MobilePlayerPlaybackSection(
                qualities = emptyList(),
                selectedQuality = null,
                onQualitySelected = {},
                adaptiveQuality = false,
                adaptiveLimitQuality = null,
                adaptiveLimitBest = false,
                onAdaptiveLimitSelected = {},
                onAdaptiveLimitBestSelected = {},
                onAdaptiveQualityChange = {},
                selectedSpeed = selectedSpeed,
                onSpeedSelected = onSpeedSelected,
                selectedResizeMode = PlayerResizeMode.FIT,
                onResizeModeSelected = {},
                showQuality = false,
                showSpeed = true,
                showResize = false,
            )
        }
    }
}
