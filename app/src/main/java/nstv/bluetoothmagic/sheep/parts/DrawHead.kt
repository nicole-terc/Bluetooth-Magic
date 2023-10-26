package nstv.bluetoothmagic.sheep.parts

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import nstv.bluetoothmagic.sheep.SheepColor
import nstv.bluetoothmagic.sheep.canvasExtensions.guidelines.GuidelineAlpha
import nstv.bluetoothmagic.sheep.canvasExtensions.guidelines.drawPoint
import nstv.bluetoothmagic.sheep.canvasExtensions.guidelines.drawRectGuideline
import nstv.bluetoothmagic.sheep.getDefaultSheepRadius
import nstv.bluetoothmagic.sheep.model.DefaultHeadRotationAngle

fun DrawScope.drawHead(
    circleCenterOffset: Offset = this.center,
    circleRadius: Float = this.getDefaultSheepRadius(),
    headAngle: Float = DefaultHeadRotationAngle,
    headColor: Color = SheepColor.Skin,
    glassesColor: Color = Color.Black,
    eyeColor: Color = SheepColor.Eye,
    glassesTranslation: Float = 0f,
    showGuidelines: Boolean = false,
) {

    // head aspect ration 1:2/3 => height = 0.66 * headWidth = 0.66 * radius
    val headSize = Size(
        width = circleRadius,
        height = circleRadius.times(2f / 3f)
    )

    // Head is 1/3 out of the fluff and 1/4 above the center of the circle
    val headTopLeft = Offset(
        x = circleCenterOffset.x - circleRadius - headSize.width.div(3f),
        y = circleCenterOffset.y - headSize.height.div(4f)
    )

    val headCenter = headTopLeft + headSize.center

    // Head
    rotate(
        degrees = headAngle,
        pivot = headCenter
    ) {
        drawArc(
            color = headColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = true,
            topLeft = headTopLeft,
            size = headSize,
        )
    }

    if (showGuidelines) {
        drawRectGuideline(
            topLeft = headTopLeft,
            size = headSize,
            degrees = headAngle
        )

        drawPoint(
            color = Color.Red.copy(alpha = GuidelineAlpha.low),
            offset = headCenter
        )
    }

    drawEyes(
        headTopLeft = headTopLeft,
        headSize = headSize,
        headAngle = headAngle,
        headCenter = headCenter,
        color = eyeColor,
        showGuidelines = showGuidelines
    )

    translate(top = glassesTranslation) {
        drawGlasses(
            headTopLeft = headTopLeft,
            headSize = headSize,
            headAngle = headAngle,
            headCenter = headCenter,
            color = glassesColor,
            showGuidelines = showGuidelines
        )
    }
}
