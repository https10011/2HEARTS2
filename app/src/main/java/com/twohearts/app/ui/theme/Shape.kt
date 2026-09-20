package com.twohearts.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * TwoHearts shape system.
 *
 * The directive's shape guidance is explicit: rounded is welcome, but "a
 * screen full of identical rounded rectangles is not a finished design".
 * The migrated app leaned on one radius for everything, which is a large
 * part of why it read as generic.
 *
 * This scale gives each component family a distinct corner treatment while
 * keeping the whole system visibly related:
 *
 *  - extraSmall — chips, tags, badges
 *  - small      — text fields, small controls
 *  - medium     — buttons, banners, inner surfaces
 *  - large      — cards and grouped list containers
 *  - extraLarge — sheets, dialogs, hero panels
 *
 * Material's [Shapes] only exposes five slots, but components in this app
 * mostly take a radius from [TwoHeartsTokens.Radius] directly so they can
 * be more specific. This exists so any stock Material component that is
 * still used inherits TwoHearts' corners instead of Material's.
 */
val TwoHeartsShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(TwoHeartsTokens.Radius.sm),
    medium = RoundedCornerShape(TwoHeartsTokens.Radius.md),
    large = RoundedCornerShape(TwoHeartsTokens.Radius.lg),
    extraLarge = RoundedCornerShape(TwoHeartsTokens.Radius.xl),
)