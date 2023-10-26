package nstv.bluetoothmagic.sheep.extra

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import nstv.bluetoothmagic.sheep.model.FluffStyle
import nstv.bluetoothmagic.sheep.parts.getFluffPath

fun getSheepPathEffect(
    miniFluffRadius: Float
) =
    PathEffect.stampedPathEffect(
        shape = getFluffPath(
            circleCenterOffset = Offset.Zero,
            circleRadius = miniFluffRadius,
            fluffStyle = FluffStyle.Random()
        ),
        advance = miniFluffRadius * 3f,
        phase = miniFluffRadius,
        style = StampedPathEffectStyle.Morph
    )
