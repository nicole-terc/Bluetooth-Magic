package nstv.bluetoothmagic.sheep

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import nstv.bluetoothmagic.sheep.model.FluffStyle
import nstv.bluetoothmagic.sheep.model.Sheep
import nstv.bluetoothmagic.sheep.parts.drawFluff
import nstv.bluetoothmagic.sheep.parts.drawHead
import nstv.bluetoothmagic.sheep.parts.drawLegs

@Composable
fun ComposableSheep(
    modifier: Modifier,
    sheep: Sheep,
    fluffColor: Color = sheep.fluffColor,
    headColor: Color = sheep.headColor,
    legColor: Color = sheep.legColor,
    eyeColor: Color = sheep.eyeColor,
    glassesColor: Color = sheep.glassesColor,
    glassesTranslation: Float = sheep.glassesTranslation,
    showGuidelines: Boolean = false,
) {
    ComposableSheep(
        modifier = modifier,
        sheep = sheep,
        fluffBrush = SolidColor(fluffColor),
        headColor = headColor,
        legColor = legColor,
        eyeColor = eyeColor,
        glassesColor = glassesColor,
        glassesTranslation = glassesTranslation,
        showGuidelines = showGuidelines
    )
}

@Composable
fun ComposableSheep(
    modifier: Modifier,
    sheep: Sheep,
    fluffBrush: Brush,
    headColor: Color = sheep.headColor,
    legColor: Color = sheep.legColor,
    eyeColor: Color = sheep.eyeColor,
    glassesColor: Color = sheep.glassesColor,
    glassesTranslation: Float = sheep.glassesTranslation,
    showGuidelines: Boolean = false,
) {
    Canvas(modifier = modifier) {
        val circleRadius = getDefaultSheepRadius()
        val circleCenterOffset = Offset(size.width / 2f, size.height / 2f)

        drawLegs(
            circleCenterOffset = circleCenterOffset,
            circleRadius = circleRadius,
            legs = sheep.legs,
            legColor = legColor,
            showGuidelines = showGuidelines
        )

        drawFluff(
            circleCenterOffset = circleCenterOffset,
            circleRadius = circleRadius,
            fluffStyle = sheep.fluffStyle,
            fluffBrush = fluffBrush,
            showGuidelines = showGuidelines
        )

        drawHead(
            circleCenterOffset = circleCenterOffset,
            circleRadius = circleRadius,
            headAngle = sheep.headAngle,
            headColor = headColor,
            eyeColor = eyeColor,
            glassesColor = glassesColor,
            glassesTranslation = glassesTranslation,
            showGuidelines = showGuidelines
        )
    }
}

fun DrawScope.drawComposableSheep(
    sheep: Sheep = Sheep(FluffStyle.Uniform(10)),
    fluffColor: Color = sheep.fluffColor,
    headColor: Color = sheep.headColor,
    legColor: Color = sheep.legColor,
    eyeColor: Color = sheep.eyeColor,
    glassesColor: Color = sheep.glassesColor,
    glassesTranslation: Float = sheep.glassesTranslation,
    showGuidelines: Boolean = false,
    circleRadius: Float = getDefaultSheepRadius(),
    circleCenterOffset: Offset = center,
) {
    drawComposableSheep(
        sheep = sheep,
        fluffBrush = SolidColor(fluffColor),
        headColor = headColor,
        legColor = legColor,
        eyeColor = eyeColor,
        glassesColor = glassesColor,
        glassesTranslation = glassesTranslation,
        showGuidelines = showGuidelines,
        circleRadius = circleRadius,
        circleCenterOffset = circleCenterOffset
    )
}

fun DrawScope.drawComposableSheep(
    sheep: Sheep = Sheep(FluffStyle.Uniform(10)),
    fluffBrush: Brush,
    headColor: Color = sheep.headColor,
    legColor: Color = sheep.legColor,
    eyeColor: Color = sheep.eyeColor,
    glassesColor: Color = sheep.glassesColor,
    glassesTranslation: Float = sheep.glassesTranslation,
    showGuidelines: Boolean = false,
    circleRadius: Float = getDefaultSheepRadius(),
    circleCenterOffset: Offset = center,
) {
    drawLegs(
        circleCenterOffset = circleCenterOffset,
        circleRadius = circleRadius,
        legs = sheep.legs,
        legColor = legColor,
        showGuidelines = showGuidelines
    )

    drawFluff(
        circleCenterOffset = circleCenterOffset,
        circleRadius = circleRadius,
        fluffStyle = sheep.fluffStyle,
        fluffBrush = fluffBrush,
        showGuidelines = showGuidelines
    )

    drawHead(
        circleCenterOffset = circleCenterOffset,
        circleRadius = circleRadius,
        headAngle = sheep.headAngle,
        headColor = headColor,
        eyeColor = eyeColor,
        glassesColor = glassesColor,
        glassesTranslation = glassesTranslation,
        showGuidelines = showGuidelines
    )
}

fun DrawScope.getDefaultSheepRadius() = size.width * 0.3f
