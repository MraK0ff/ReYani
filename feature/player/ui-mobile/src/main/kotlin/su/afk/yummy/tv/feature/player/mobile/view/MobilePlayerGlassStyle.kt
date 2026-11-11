package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared shape tokens: interactive surfaces are rounder than passive labels. */
internal val MobilePlayerInteractiveShape = RoundedCornerShape(18.dp)
internal val MobilePlayerStaticShape = RoundedCornerShape(8.dp)

/** Shared frosted-glass treatment for player chips and controls. */
internal val MobilePlayerGlassFill = Color(0xFF17191D).copy(alpha = 0.29f)
internal val MobilePlayerGlassBorder = Color.White.copy(alpha = 0.38f)

internal fun Modifier.mobilePlayerGlassSurface(
    shape: Shape = MobilePlayerStaticShape,
    fill: Color = MobilePlayerGlassFill,
    borderColor: Color = MobilePlayerGlassBorder,
    elevation: Dp = 12.dp,
): Modifier = shadow(elevation = elevation, shape = shape, clip = false)
    .clip(shape)
    .background(fill)
    .border(1.dp, borderColor, shape)

@androidx.compose.runtime.Composable
internal fun mobilePlayerControlButtonSize(primary: Boolean = false): Dp {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp >= 440 -> if (primary) 66.dp else 48.dp
        widthDp >= 392 -> if (primary) 62.dp else 46.dp
        else -> if (primary) 58.dp else 42.dp
    }
}
