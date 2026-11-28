package com.twohearts.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twohearts.app.ui.components.BrandLogo
import com.twohearts.app.ui.components.BrandLogoTone
import com.twohearts.app.ui.components.BrandLogoVariant
import com.twohearts.app.ui.theme.LocalTwoHeartsColors
import com.twohearts.app.ui.theme.LocalTwoHeartsMotion
import com.twohearts.app.ui.theme.TwoHeartsTokens

@Composable
internal fun bottomNavLabelStyle() = if (LocalDensity.current.fontScale > 1.2f) {
    MaterialTheme.typography.labelSmall.copy(
        fontFamily = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)),
        letterSpacing = 0.sp,
    )
} else MaterialTheme.typography.labelMedium

/** Five page tabs and a bound center seal. Content reserves the dock's measured height. */
@Composable
fun BottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBodyHeight: (Dp) -> Unit = {},
) {
    val paper = MaterialTheme.colorScheme.surface
    val colors = LocalTwoHeartsColors.current
    val density = LocalDensity.current
    Column(modifier.fillMaxWidth().background(paper).windowInsetsPadding(WindowInsets.navigationBars)) {
        Column(Modifier.onSizeChanged { onBodyHeight(with(density) { it.height.toDp() }) }) {
            HorizontalDivider(color = colors.borderStrong, thickness = Dp.Hairline)
            Row(
                Modifier.fillMaxWidth().heightIn(min = TwoHeartsTokens.Dimensions.bottomNavHeight)
                    .padding(horizontal = 2.dp, vertical = 3.dp).selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavConfig.bottomNavItems.forEach { item ->
                    val active = currentRoute == item.route
                    val ink by animateColorAsState(
                        if (active) colors.burgundy else colors.textSecondary,
                        if (LocalTwoHeartsMotion.current.reduced) snap() else tween(120), label = "tab ink",
                    )
                    Column(
                        Modifier.weight(1f).heightIn(min = 48.dp)
                            .semantics { contentDescription = item.accessibilityLabel }
                            .selectable(active, role = Role.Tab, onClick = { onNavigate(item.route) }),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(Modifier.height(36.dp), contentAlignment = Alignment.Center) {
                            if (item.isBrand) {
                                Box(
                                    Modifier.size(36.dp).clip(CircleShape)
                                        .background(TwoHeartsTokens.Color.burgundyDark)
                                        .then(if (active) Modifier.border(2.dp, colors.gold, CircleShape) else Modifier),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    BrandLogo(Modifier.clearAndSetSemantics {}, BrandLogoVariant.MARK, 24, BrandLogoTone.LIGHT)
                                }
                            } else Icon(item.icon, null, Modifier.size(22.dp), tint = ink)
                        }
                        Text(item.label, Modifier.clearAndSetSemantics {}, style = bottomNavLabelStyle(),
                            color = ink, textAlign = TextAlign.Center)
                        Spacer(Modifier.padding(top = 2.dp).width(18.dp).height(2.dp)
                            .background(if (active) ink else Color.Transparent, CircleShape))
                    }
                }
            }
        }
    }
}
